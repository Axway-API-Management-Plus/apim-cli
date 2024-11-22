package com.axway.apim.config;

import com.axway.apim.adapter.jackson.CustomYamlFactory;
import com.axway.apim.api.API;
import com.axway.apim.api.model.*;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.config.model.GenerateTemplateParameters;
import com.axway.apim.lib.StandardExportParams;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.URLParser;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.*;

public class GenerateTemplate implements APIMCLIServiceProvider {


    private static final Logger LOG = LoggerFactory.getLogger(GenerateTemplate.class);
    public static final String DEFAULT = "_default";
    public static final String ORIGINAL = "original";
    public static final String PASS_THROUGH = "Pass Through";
    public static final String REMOVE_CREDENTIALS_ON_SUCCESS = "removeCredentialsOnSuccess";
    public static final String TOKEN_STORE = "tokenStore";

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
        ObjectMapper objectMapper;
        // Trust all certificate and hostname for openapi parser
        System.setProperty("TRUST_ALL", "true");
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);//NOSONAR
        LOG.info("Generating APIM CLI configuration file");
        GenerateTemplateParameters params;
        try {
            params = (GenerateTemplateParameters) GenerateTemplateCLIOptions.create(args).getParams();
        } catch (AppException e) {
            LOG.error("Error", e);
            return e.getError().getCode();
        }
        GenerateTemplate app = new GenerateTemplate();
        try {
            File file = new File(params.getConfig());
            if (file.getParentFile() != null && !file.getParentFile().exists() && (file.getParentFile().mkdirs())) {
                LOG.info("Created new Directory : {}", file.getParentFile());
            }
            APIConfig apiConfig = app.generateTemplate(params);
            try (FileWriter fileWriter = new FileWriter(params.getConfig())) {
                if (params.getOutputFormat().equals(StandardExportParams.OutputFormat.yaml))
                    objectMapper = new ObjectMapper(CustomYamlFactory.createYamlFactory());
                else
                    objectMapper = new ObjectMapper();
                String[] serializeAllExcept = new String[]{"useForInbound", "useForOutbound"};
                FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept(serializeAllExcept));
                objectMapper.setFilterProvider(filter);
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                JsonNode jsonNode = objectMapper.convertValue(apiConfig, JsonNode.class);
                objectMapper.writeValue(fileWriter, jsonNode);
                LOG.info("Writing APIM CLI configuration file to : {}", params.getConfig());
            }
        } catch (AppException e) {
            LOG.error("{} : Error code {}", e.getError().getDescription(), e.getError().getCode());
            return e.getError().getCode();
        } catch (Exception e) {
            LOG.error("Error in processing :", e);
            return 1;
        }
        return 0;
    }


    public APIConfig generateTemplate(GenerateTemplateParameters parameters) throws IOException, CertificateEncodingException, NoSuchAlgorithmException, KeyManagementException {
        List<AuthorizationValue> authorizationValues = new ArrayList<>();
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true); // implicit
        String apiDefinition = parameters.getApiDefinition();
        URLParser urlParser = new URLParser(apiDefinition);
        String uri = urlParser.getUri();
        String username = urlParser.getUsername();
        String password = urlParser.getPassword();

        if (username != null && password != null) {
            String credential = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            AuthorizationValue authorizationValue = new AuthorizationValue(HttpHeaders.AUTHORIZATION, credential, "header");
            authorizationValues.add(authorizationValue);
        }
        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(uri, authorizationValues, parseOptions);
        List<String> messages = result.getMessages();
        if (!messages.isEmpty()) {
            throw new AppException(messages.toString(), ErrorCode.UNSUPPORTED_API_SPECIFICATION);
        }
        OpenAPI openAPI = result.getOpenAPI();
        Info info = openAPI.getInfo();
        List<Server> servers = openAPI.getServers();
        if (servers == null || servers.isEmpty()) {
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
        API api = new API();
        addTags(api, openAPI);
        api.setState("published");
        api.setBackendResourcePath(urlString);
        api.setPath(basePath);
        api.setName(info.getTitle());
        api.setVersion(info.getVersion());
        api.setDescriptionType(ORIGINAL);
        CorsProfile corsProfile = new CorsProfile();
        corsProfile.setName("Custom CORS");
        corsProfile.setIsDefault(false);
        corsProfile.setSupportCredentials(true);
        corsProfile.setOrigins(new String[]{"*"});
        corsProfile.setAllowedHeaders(new String[]{"Authorization", "x-requested-with", "Bearer"});
        corsProfile.setExposedHeaders(new String[]{"Via", "X-CorrelationID"});
        corsProfile.setMaxAgeSeconds("0");

        CorsProfile corsProfileDefault = new CorsProfile();
        corsProfileDefault.setName(DEFAULT);
        corsProfileDefault.setIsDefault(true);
        corsProfileDefault.setOrigins(new String[]{"*"});
        corsProfileDefault.setAllowedHeaders(new String[]{});
        corsProfileDefault.setExposedHeaders(new String[]{"X-CorrelationID"});
        corsProfileDefault.setMaxAgeSeconds("0");

        List<CorsProfile> corsProfiles = new ArrayList<>();
        corsProfiles.add(corsProfileDefault);
        corsProfiles.add(corsProfile);
        api.setCorsProfiles(corsProfiles);

        Map<String, InboundProfile> inboundProfiles = new HashMap<>();
        InboundProfile profile = new InboundProfile();
        profile.setCorsProfile("Custom CORS");
        profile.setSecurityProfile(DEFAULT);
        profile.setMonitorAPI(true);
        profile.setMonitorSubject("authentication.subject.id");
        profile.setQueryStringPassThrough(false);
        inboundProfiles.put(DEFAULT, profile);
        api.setInboundProfiles(inboundProfiles);
        String frontendAuthType = parameters.getFrontendAuthType();
        // If frontendAuthType is null, use authentication from openapi spec. If none found, set it as pass through
        List<SecurityProfile> securityProfiles = addInboundSecurityToAPI(frontendAuthType);
        String backendAuthType = parameters.getBackendAuthType();
        addOutboundSecurityToAPI(api, backendAuthType);
        String apiSpecLocation;
        if (uri.startsWith("https")) {
            apiSpecLocation = downloadCertificatesAndContent(api, parameters.getConfig(), uri);
        } else if (uri.startsWith("http")) {
            apiSpecLocation = downloadContent(parameters.getConfig(), uri);
        } else {
            apiSpecLocation = parameters.getApiDefinition();
        }
        if (parameters.isIncludeMethods()) {
            List<APIMethod> methods = addMethods(openAPI);
            api.setApiMethods(methods);
        }

        if (parameters.isInboundPerMethodOverride()) {
            Map<String, InboundProfile> inboundProfileMap = addInboundPerMethodOverride(openAPI, api, securityProfiles);
            api.setInboundProfiles(inboundProfileMap);
        }

        return new APIConfig(api, apiSpecLocation);
    }

    public List<APIMethod> addMethods(OpenAPI openAPI) {
        List<APIMethod> methods = new ArrayList<>();
        Paths paths = openAPI.getPaths();
        for (Map.Entry<String, PathItem> pathItem : paths.entrySet()) {
            APIMethod method = new APIMethod();
            String key = pathItem.getKey();
            PathItem item = pathItem.getValue();
            Map<PathItem.HttpMethod, Operation> operationsMap = item.readOperationsMap();
            for (Map.Entry<PathItem.HttpMethod, Operation> operationEntry : operationsMap.entrySet()) {
                PathItem.HttpMethod httpMethod = operationEntry.getKey();
                Operation operation = operationEntry.getValue();
                List<String> tags = operation.getTags();
                List<Tag> globalTags = openAPI.getTags();
                TagMap apiManagerTags = new TagMap();
                for (String tag : tags) {
                    Tag globalTag = findTag(tag, globalTags);
                    if (globalTag == null) {
                        continue;
                    }
                    String[] value = new String[1];
                    value[0] = globalTag.getDescription();
                    apiManagerTags.put(globalTag.getName(), value);
                }
                String operationId = operation.getOperationId();
                if (operationId == null) {
                    operationId = httpMethod.name() + " " + key;
                }
                method.setName(operationId);
                method.setTags(apiManagerTags);
                method.setDescriptionType(ORIGINAL);
                methods.add(method);
            }
        }
        return methods;
    }

    public Map<String, InboundProfile> addInboundPerMethodOverride(OpenAPI openAPI, API api, List<SecurityProfile> securityProfiles) {
        Map<String, InboundProfile> inboundProfiles = new LinkedHashMap<>();
        inboundProfiles.put(DEFAULT, InboundProfile.getDefaultInboundProfile());
        Paths paths = openAPI.getPaths();
        for (Map.Entry<String, PathItem> pathItem : paths.entrySet()) {
            InboundProfile inboundProfile = new InboundProfile();
            inboundProfile.setMonitorAPI(true);
            inboundProfile.setQueryStringPassThrough(false);
            String key = pathItem.getKey();
            PathItem item = pathItem.getValue();
            Map<PathItem.HttpMethod, Operation> operationsMap = item.readOperationsMap();
            for (Map.Entry<PathItem.HttpMethod, Operation> operationEntry : operationsMap.entrySet()) {
                PathItem.HttpMethod httpMethod = operationEntry.getKey();
                Operation operation = operationEntry.getValue();
                String operationId = operation.getOperationId();
                if (operationId == null) {
                    operationId = httpMethod.name() + " " + key;
                }
                List<SecurityRequirement> securityRequirements = operation.getSecurity();
                if (securityRequirements == null) {
                    SecurityProfile passThroughProfile = createPassThroughSecurityProfile();
                    inboundProfile.setSecurityProfile(passThroughProfile.getName());
                    inboundProfiles.put(operationId, inboundProfile);
                    securityProfiles.add(passThroughProfile);
                } else {

                    for (SecurityRequirement securityRequirement : securityRequirements) {
                        Set<String> keys = securityRequirement.keySet();
                        for (String securityKey : keys) {
                            SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get(securityKey);
                            SecurityScheme.Type type = securityScheme.getType();

                            if (type == SecurityScheme.Type.OAUTH2) {
                                List<String> scopes = securityRequirement.get(securityKey);
                                SecurityProfile oauth2SecurityProfile = createOauthSecurityProfile(operationId, scopes);
                                inboundProfile.setSecurityProfile(oauth2SecurityProfile.getName());
                                inboundProfiles.put(operationId, inboundProfile);
                                securityProfiles.add(oauth2SecurityProfile);
                            } else if (type == SecurityScheme.Type.APIKEY) {
                                LOG.warn("API key is not handled");
                            } else if (type == SecurityScheme.Type.MUTUALTLS) {
                                LOG.warn("Mutual auth is not handled");
                            }
                        }
                    }
                }
            }
        }
        api.setSecurityProfiles(securityProfiles);
        return inboundProfiles;
    }

    public SecurityProfile createPassThroughSecurityProfile() {
        SecurityProfile profile = new SecurityProfile();
        profile.setName(PASS_THROUGH);
        profile.setIsDefault(false);
        SecurityDevice securityDevice = new SecurityDevice();
        securityDevice.setName(PASS_THROUGH);
        securityDevice.setType(DeviceType.passThrough);
        securityDevice.setOrder(0);
        Map<String, String> properties = new HashMap<>();
        properties.put("subjectIdFieldName", PASS_THROUGH);
        properties.put(REMOVE_CREDENTIALS_ON_SUCCESS, "true");
        securityDevice.setProperties(properties);
        List<SecurityDevice> securityDevices = new ArrayList<>();
        securityDevices.add(securityDevice);
        profile.setDevices(securityDevices);
        return profile;
    }

    public SecurityProfile createOauthSecurityProfile(String operationId, List<String> scopes) {
        SecurityProfile profile = new SecurityProfile();
        profile.setName("Oauth2");
        profile.setIsDefault(false);
        SecurityDevice securityDevice = new SecurityDevice();
        securityDevice.setName("Oauth2 " + operationId);
        securityDevice.setType(DeviceType.oauth);
        securityDevice.setOrder(0);
        Map<String, String> properties = new HashMap<>();
        properties.put(TOKEN_STORE, "OAuth Access Token Store");
        String scope = String.join(" ", scopes);
        setupOauthProperties(properties, scope);
        securityDevice.setProperties(properties);
        List<SecurityDevice> securityDevices = new ArrayList<>();
        securityDevices.add(securityDevice);
        profile.setDevices(securityDevices);
        return profile;
    }

    public Tag findTag(String tagName, List<Tag> tags) {
        if (tags == null || tags.isEmpty()) return null;
        for (Tag tag : tags) {
            if (tag.getName().equals(tagName)) {
                return tag;
            }
        }
        return null;
    }

    public TagMap parseTags(List<Tag> tags) {
        TagMap apiManagerTags = new TagMap();
        for (Tag tag : tags) {
            String[] value = new String[1];
            value[0] = tag.getDescription();
            apiManagerTags.put(tag.getName(), value);
        }
        return apiManagerTags;
    }

    public void addTags(API api, OpenAPI openAPI) {
        List<Tag> tags = openAPI.getTags();
        if (tags != null) {
            TagMap apiManagerTags = parseTags(tags);
            api.setTags(apiManagerTags);
        }
    }

    public AuthType matchAuthType(String backendAuthType) {
        AuthType authType = null;
        try {
            authType = AuthType.valueOf(backendAuthType);
        } catch (IllegalArgumentException e) {
            LOG.error("Invalid backend auth type", e);
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
        return authType;
    }

    private void addOutboundSecurityToAPI(API api, String backendAuthType) throws AppException {
        AuthType authType = matchAuthType(backendAuthType);
        if (authType == null) {
            throw new AppException("backendAuthType : " + backendAuthType + "  is invalid", ErrorCode.INVALID_PARAMETER);
        }
        List<AuthenticationProfile> authnProfiles = new ArrayList<>();
        AuthenticationProfile authNProfile = new AuthenticationProfile();
        authNProfile.setName(DEFAULT);
        authNProfile.setType(authType);
        authNProfile.setIsDefault(true);
        Map<String, Object> parameters = new LinkedHashMap<>();
        if (authType.equals(AuthType.apiKey)) {
            parameters.put("apiKey", "4249823490238490");
            parameters.put("apiKeyField", "KeyId");
            parameters.put("httpLocation", "QUERYSTRING_PARAMETER");
        } else if (authType.equals(AuthType.http_basic) || authType.equals(AuthType.http_digest)) {
            parameters.put("username", "user1");
            parameters.put("password", "password1");
        } else if (authType.equals(AuthType.oauth)) {
            parameters.put("providerProfile", "<Name-of-configured-OAuth-Profile>");
            parameters.put("ownerId", "${authentication.subject.id}");
        } else if (authType.equals(AuthType.ssl)) {
            parameters.put("source", "file");
            parameters.put("certFile", "../certificates/clientcert.pfx");
            parameters.put("password", Utils.getEncryptedPassword());
            parameters.put("trustAll", true);
        }
        authNProfile.setParameters(parameters);
        authnProfiles.add(authNProfile);
        api.setAuthenticationProfiles(authnProfiles);

    }

    public DeviceType matchDeviceType(String frontendAuthType) {
        DeviceType deviceType = null;
        try {
            deviceType = DeviceType.valueOf(frontendAuthType);
        } catch (IllegalArgumentException e) {
            LOG.debug("Invalid Frontend AuthType : {} going to try with alternate names", frontendAuthType);
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
        return deviceType;
    }

    private List<SecurityProfile> addInboundSecurityToAPI(String frontendAuthType) throws AppException {
        List<SecurityProfile> securityProfiles = new ArrayList<>();
        DeviceType deviceType = matchDeviceType(frontendAuthType);
        LOG.info("Frontend Authentication type : {}", frontendAuthType);
        if (deviceType == null) {
            throw new AppException("frontendAuthType : " + frontendAuthType + "  is invalid", ErrorCode.INVALID_PARAMETER);
        }
        SecurityProfile securityProfile = new SecurityProfile();
        securityProfile.setIsDefault(true);
        securityProfile.setName(DEFAULT);
        SecurityDevice securityDevice = new SecurityDevice();
        Map<String, String> properties = new HashMap<>();
        if (deviceType.equals(DeviceType.apiKey)) {
            properties.put("apiKeyFieldName", "KeyId");
            properties.put("takeFrom", "HEADER");
            properties.put(REMOVE_CREDENTIALS_ON_SUCCESS, "true");
        } else if (deviceType.equals(DeviceType.oauth)) {
            properties.put(TOKEN_STORE, "OAuth Access Token Store");
            setupOauthProperties(properties, "resource.WRITE, resource.READ");
        } else if (deviceType.equals(DeviceType.oauthExternal)) {
            properties.put(TOKEN_STORE, "Tokeninfo policy 1");
            properties.put("useClientRegistry", "true");
            properties.put("subjectSelector", "${oauth.token.client_id}");
            setupOauthProperties(properties, "resource.WRITE, resource.READ");
        } else if (deviceType.equals(DeviceType.authPolicy)) {
            properties.put("authenticationPolicy", "Custom authentication policy");
            properties.put("useClientRegistry", "true");
            properties.put("subjectSelector", "authentication.subject.id");
            properties.put("descriptionType", ORIGINAL);
            properties.put("descriptionUrl", "");
            properties.put("descriptionMarkdown", "");
            properties.put("description", "");
        }
        securityDevice.setProperties(properties);
        securityDevice.setOrder(1);
        securityDevice.setName(DEFAULT);
        securityDevice.setType(deviceType);
        List<SecurityDevice> securityDevices = new ArrayList<>();
        securityDevices.add(securityDevice);
        securityProfile.setDevices(securityDevices);
        securityProfiles.add(securityProfile);
        return securityProfiles;
    }

    private void setupOauthProperties(Map<String, String> properties, String scopes) {
        properties.put("accessTokenLocation", "HEADER");
        properties.put("authorizationHeaderPrefix", "Bearer");
        properties.put("accessTokenLocationQueryString", "");
        properties.put("scopesMustMatch", "All");
        properties.put("scopes", scopes);
        properties.put(REMOVE_CREDENTIALS_ON_SUCCESS, "true");
        properties.put("implicitGrantEnabled", "true");
        properties.put("implicitGrantLoginEndpointUrl", "https://localhost:8089/api/oauth/authorize");
        properties.put("implicitGrantLoginTokenName", "access_token");
        properties.put("authCodeGrantTypeEnabled", "true");
        properties.put("authCodeGrantTypeRequestEndpointUrl", "https://localhost:8089/api/oauth/authorize");
        properties.put("authCodeGrantTypeRequestClientIdName", "client_id");
        properties.put("authCodeGrantTypeRequestSecretName", "client_secret");
        properties.put("authCodeGrantTypeTokenEndpointUrl", "https://localhost:8089/api/oauth/token");
        properties.put("authCodeGrantTypeTokenEndpointTokenName", "access_code");
        properties.put("clientCredentialsGrantTypeEnabled", "true");
        properties.put("clientCredentialsGrantTypeTokenEndpointUrl", "https://localhost:8089/api/oauth/token");
        properties.put("clientCredentialsGrantTypeTokenName", "access_token");
    }

    public String writeAPISpecification(String url, String configPath, InputStream inputStream) throws IOException {
        String filename;
        try {
            filename = new File(new URL(url).getPath()).getName();
            String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            File file = new File(configPath);
            String parent = file.getParent();

            if (parent != null) {
                filename = file.toPath().getParent().toString() + File.separator + filename;
            }
            LOG.info("Writing API specification to : {}", filename);
            try (FileWriter fileWriter = new FileWriter(filename)) {
                fileWriter.write(content);
                fileWriter.flush();
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return filename;
    }

    public String downloadContent(String configPath, String url) throws IOException {
        URL httpURL = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) httpURL.openConnection();
        int responseCode = httpURLConnection.getResponseCode();
        String filePath = null;
        LOG.debug("Http Response Code : {}", responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            filePath = writeAPISpecification(url, configPath, httpURLConnection.getInputStream());
        }
        return filePath;
    }


    public String downloadCertificatesAndContent(API api, String configPath, String url) throws
        IOException, CertificateEncodingException, NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {//NOSONAR
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {//NOSONAR
            }
        }};

        File file = new File(configPath);
        String parent = file.getParent();
        Base64.Encoder encoder = Base64.getMimeEncoder(64, System.lineSeparator().getBytes());
        URL httpURL = new URL(url);
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) httpURL.openConnection();
        httpsURLConnection.connect();
        Certificate[] certificates = httpsURLConnection.getServerCertificates();
        List<CaCert> caCerts = new ArrayList<>();
        for (Certificate certificate : certificates) {
            if (certificate instanceof X509Certificate) {
                X509Certificate publicCert = (X509Certificate) certificate;
                int basicConstraints = publicCert.getBasicConstraints();
                if (basicConstraints == -1 && (caCerts.size() > 1)) { // ignore for self signed certs
                    continue;
                }
                CaCert caCert = new CaCert();
                String encodedCertText = new String(encoder.encode(publicCert.getEncoded()));
                byte[] certContent = ("-----BEGIN CERTIFICATE-----\n" + encodedCertText + "\n-----END CERTIFICATE-----").getBytes();
                String filename = createCertFileName(publicCert);
                if (parent != null) {
                    filename = file.toPath().getParent().toString() + File.separator + filename;
                }
                try (FileOutputStream fileOutputStream = new FileOutputStream(filename)) {//NOSONAR
                    fileOutputStream.write(certContent);
                } catch (IOException e) {
                    throw new AppException("Can't write file", ErrorCode.UNXPECTED_ERROR, e);
                }
                caCert.setCertFile(filename);
                caCert.setInbound("false");
                caCert.setOutbound("true");
                caCerts.add(caCert);
            }
        }
        int responseCode = httpsURLConnection.getResponseCode();
        String filePath = null;
        LOG.debug("Response Code : {}", responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            filePath = writeAPISpecification(url, configPath, httpsURLConnection.getInputStream());
        }
        api.setCaCerts(caCerts);
        return filePath;
    }

    public String createCertFileName(X509Certificate certificate) {
        String filename = null;
        String certAlias = certificate.getSubjectX500Principal().getName();
        String[] nameParts = certAlias.split(",");
        for (String namePart : nameParts) {
            if (namePart.trim().startsWith("CN=")) {
                filename = namePart.trim().substring(3);
                break;
            }
        }
        if (filename == null) {
            LOG.warn("No CN");
            filename = "UnknownCertificate_" + UUID.randomUUID();
            LOG.warn("Created a random filename: {}", filename);
        } else {
            filename = filename.replace(" ", "");
            filename = filename.replace("*", "");
            if (filename.startsWith(".")) filename = filename.replaceFirst("\\.", "");
        }
        return filename + ".crt";
    }
}
