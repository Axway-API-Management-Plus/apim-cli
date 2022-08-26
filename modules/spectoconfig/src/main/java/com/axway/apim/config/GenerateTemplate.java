package com.axway.apim.config;

import com.axway.apim.api.API;
import com.axway.apim.api.model.*;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.config.model.APISecurity;
import com.axway.apim.config.model.GenerateTemplateParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.URLParser;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class GenerateTemplate implements APIMCLIServiceProvider {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(GenerateTemplate.class);

    @Override
    public String getName() {
        return "Generate Config file template from Open API";
    }

    @Override
    public String getVersion() {
        return GenerateTemplate.class.getPackage().getImplementationVersion();
    }

    @Override
    public String getGroupId() {
        return "template";
    }

    @Override
    public String getGroupDescription() {
        return "Generate APIM CLI Config file template from Open API";
    }

    @CLIServiceMethod(name = "generate", description = "Generate APIM CLI Config file template from Open API")
    public static int generate(String[] args) {
        GenerateTemplateParameters params;
        try {
            params = (GenerateTemplateParameters) GenerateTemplateCLIOptions.create(args).getParams();
         //   params.validateRequiredParameters();
//            APIManagerAdapter.deleteInstance();
//            APIMHttpClient.deleteInstances();
//            APIManagerAdapter.getInstance();
        } catch (AppException e) {
            LOG.error("Error " + e.getMessage());
            return e.getError().getCode();
        }
        GenerateTemplate app = new GenerateTemplate();
        FileWriter fileWriter = null;
        try {
            APIConfig apiConfig = app.generateTemplate(params);
            fileWriter = new FileWriter(params.getConfig());
            FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAll());
            objectMapper.setFilterProvider(filter);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
//            objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            JsonNode jsonNode = objectMapper.convertValue(apiConfig, JsonNode.class);
            //  ((ObjectNode) jsonNode).put("apiDefinition", params.getApiDefinition());
            objectMapper.writeValue(fileWriter, jsonNode);
        } catch (IOException e) {
            LOG.error("Error in processing :", e);
            if (e instanceof AppException) {
                AppException appException = (AppException) e;
                LOG.error("{} : Error code {}", appException.getError().getDescription(), appException.getError().getCode());
                return appException.getError().getCode();
            }
            return 1;
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    LOG.error("Problem in closing the file");
                }
            }
        }
        return 0;
    }


    public APIConfig generateTemplate(GenerateTemplateParameters parameters) throws MalformedURLException, AppException {
        List<AuthorizationValue> authorizationValues = new ArrayList<>();
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true); // implicit
        String apiDefinition = parameters.getApiDefinition();
        URLParser urlParser = new URLParser(apiDefinition);
        String uri = urlParser.getUri();
        String username = urlParser.getUsername();
        String password = urlParser.getPassword();

        if (username != null & password != null) {
            String credential = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            AuthorizationValue authorizationValue = new AuthorizationValue(HttpHeaders.AUTHORIZATION, credential, "header");
            authorizationValues.add(authorizationValue);
        }
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(uri, authorizationValues, parseOptions);
        List<String> messages = result.getMessages();
        if (messages.size() > 0) {
            throw new AppException(messages.toString(), ErrorCode.UNSUPPORTED_API_SPECIFICATION);
        }
        OpenAPI openAPI = result.getOpenAPI();
        Info info = openAPI.getInfo();
        List<Server> servers = openAPI.getServers();
        if (servers == null || servers.size() == 0) {
            throw new AppException("servers element is not found", ErrorCode.UNSUPPORTED_API_SPECIFICATION);
        }
        Server server = servers.get(0);
        String strURL = server.getUrl();
        String basePath;
        String urlString;

        if (strURL.startsWith("http")) {
            URL url = new URL(strURL);
            basePath = url.getPath();
            String protocol = url.getProtocol();
            String host = url.getHost();
            int port = url.getPort();
            if (port == -1) {
                urlString = String.format("%s://%s", protocol, host);
            } else {
                urlString = String.format("%s://%s:%d", protocol, host, port);
            }
        } else {
            basePath = strURL;
            urlString = "https://localhost";
        }

        List<Tag> tags = openAPI.getTags();
        TagMap<String, String[]> apiManagerTags = new TagMap<>();
        for (Tag tag : tags) {
            String[] value = new String[1];
            value[0] = tag.getName();
            apiManagerTags.put(tag.getName(), value);
        }

        API api = new API();
        api.setState("published");
        api.setBackendResourcePath(urlString);
        api.setPath(basePath);
        api.setName(info.getTitle());
        api.setVersion(info.getVersion());
        api.setTags(apiManagerTags);
        api.setDescriptionType("original"); // Use description from openapi
        CorsProfile corsProfile = new CorsProfile();
        corsProfile.setName("CORS profile");
        corsProfile.setOrigins(new String[]{"*"});
        corsProfile.setAllowedHeaders(new String[]{"Authorization"});
        corsProfile.setExposedHeaders(new String[]{"Via"});
        corsProfile.setMaxAgeSeconds("0");
        List<CorsProfile> corsProfiles = new ArrayList<>();
        corsProfiles.add(corsProfile);
        api.setCorsProfiles(corsProfiles);
        String frontendAuthType = parameters.getFrontendAuthType();
        // If frontendAuthType is null, use authentication from openapi spec. If none found, set it as pass through
        Map<String, Object> securityProfiles = addInboundSecurityToAPI(api, frontendAuthType);
        String backendAuthType = parameters.getBackendAuthType();
        addOutboundSecurityToAPI(api, backendAuthType);
        APIConfig apiConfig = new APIConfig(api, parameters.getApiDefinition(), securityProfiles);
        return apiConfig;
    }


    public static void main(String[] args) {
        int rc = generate(args);
        System.exit(rc);
    }

    private void addOutboundSecurityToAPI(API api, String backendAuthType) throws AppException {
        AuthType authType = null;
        try {
            authType = AuthType.valueOf(backendAuthType);
        } catch (IllegalArgumentException e) {
        }
        if (authType == null) {
            for (AuthType authTypeEnum : AuthType.values()) {
                String name = authTypeEnum.name();
                String[] alternativeNames = authTypeEnum.getAlternativeNames();
                if (name.equals(backendAuthType)) {
                    authType = authTypeEnum;
                    break;
                }
                for (String alternativeName : alternativeNames) {
                    if (alternativeName.equals(backendAuthType)) {
                        authType = authTypeEnum;
                        break;
                    }
                }
            }
        }
        if (authType == null) {
            throw new AppException("backendAuthType : " + backendAuthType + "  is invalid", ErrorCode.INVALID_PARAMETER);
        }
        List<AuthenticationProfile> authnProfiles = new ArrayList<>();
        AuthenticationProfile authNProfile = new AuthenticationProfile();
        authNProfile.setName("_default");
        authNProfile.setType(authType);
        authnProfiles.add(authNProfile);
        Map<String, OutboundProfile> outboundProfiles = new HashMap<>();
        OutboundProfile outboundProfile = new OutboundProfile();
        outboundProfile.setAuthenticationProfile("_default");
        outboundProfiles.put("_default", outboundProfile);
        api.setAuthenticationProfiles(authnProfiles);
        api.setOutboundProfiles(outboundProfiles);
    }

    private Map<String, Object> addInboundSecurityToAPI(API api, String frontendAuthType) throws AppException {
        DeviceType deviceType = null;
        try {
            deviceType = DeviceType.valueOf(frontendAuthType);
        } catch (IllegalArgumentException e) {
        }

        if (deviceType == null) {
            for (DeviceType deviceTypeEnum : DeviceType.values()) {
                String name = deviceTypeEnum.name();
                String[] alternativeNames = deviceTypeEnum.getAlternativeNames();
                if (name.equals(frontendAuthType)) {
                    deviceType = deviceTypeEnum;
                    break;
                }
                for (String alternativeName : alternativeNames) {
                    if (alternativeName.equals(frontendAuthType)) {
                        deviceType = deviceTypeEnum;
                        break;
                    }
                }
            }
        }
        if (deviceType == null) {
            throw new AppException("frontendAuthType : " + frontendAuthType + "  is invalid", ErrorCode.INVALID_PARAMETER);
        }
        APISecurity apiSecurity = new APISecurity();
        apiSecurity.setType(deviceType.toString());
        apiSecurity.setName(deviceType.getName());
        Map<String, Object> properties = new HashMap<>();
        if (deviceType.equals(DeviceType.apiKey)) {
            properties.put("apiKeyFieldName", "KeyId");
            properties.put("takeFrom", "HEADER");
            properties.put("removeCredentialsOnSuccess", true);
        } else if (deviceType.equals(DeviceType.oauth)) {
            properties.put("tokenStore", "OAuth Access Token Store");
            properties.put("scopes", "resource.WRITE, resource.READ");
            setupOauthProperties(properties);
        } else if (deviceType.equals(DeviceType.oauthExternal)) {
            properties.put("tokenStore", "Tokeninfo policy 1");
            properties.put("useClientRegistry", true);
            properties.put("subjectSelector", "${oauth.token.client_id}");
            setupOauthProperties(properties);
        } else if (deviceType.equals(DeviceType.authPolicy)) {
            properties.put("authenticationPolicy", "Custom authentication policy");
            properties.put("useClientRegistry", true);
            properties.put("subjectSelector", "authentication.subject.id");
            properties.put("descriptionType", "original");
            properties.put("descriptionUrl", "");
            properties.put("descriptionMarkdown", "");
            properties.put("description", "");
        }
        apiSecurity.setProperties(properties);
        Map<String, Object> securityProfile = new LinkedHashMap<>();
        securityProfile.put("name", "_default");
        securityProfile.put("isDefault", true);
        List<APISecurity> apiSecurities = new ArrayList<>();
        apiSecurities.add(apiSecurity);
        securityProfile.put("devices", apiSecurities);
        return securityProfile;
    }

    private void setupOauthProperties(Map<String, Object> properties) {
        properties.put("accessTokenLocation", "HEADER");
        properties.put("authorizationHeaderPrefix", "Bearer");
        properties.put("accessTokenLocationQueryString", "");
        properties.put("scopesMustMatch", "Any");
        properties.put("scopes", "resource.WRITE, resource.READ");
        properties.put("removeCredentialsOnSuccess", true);
        properties.put("implicitGrantEnabled", true);
        properties.put("implicitGrantLoginEndpointUrl", "https://localhost:8089/api/oauth/authorize");
        properties.put("implicitGrantLoginTokenName", "access_token");
        properties.put("authCodeGrantTypeEnabled", true);
        properties.put("authCodeGrantTypeRequestEndpointUrl", "https://localhost:8089/api/oauth/authorize");
        properties.put("authCodeGrantTypeRequestClientIdName", "client_id");
        properties.put("authCodeGrantTypeRequestSecretName", "client_secret");
        properties.put("authCodeGrantTypeTokenEndpointUrl", "https://localhost:8089/api/oauth/token");
        properties.put("authCodeGrantTypeTokenEndpointTokenName", "access_code");
    }

}
