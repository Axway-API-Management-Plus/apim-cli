package com.axway.apim.config;

import com.axway.apim.adapter.jackson.CustomYamlFactory;
import com.axway.apim.api.API;
import com.axway.apim.api.model.*;
import com.axway.apim.cli.APIMCLIServiceProvider;
import com.axway.apim.cli.CLIServiceMethod;
import com.axway.apim.config.model.APISecurity;
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
import io.swagger.v3.oas.models.info.Info;
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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.*;

public class GenerateTemplate implements APIMCLIServiceProvider {


    private static final Logger LOG = LoggerFactory.getLogger(GenerateTemplate.class);
    public static final String DEFAULT = "_default";

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
        System.setProperty("TRUST_ALL","true");
        HttpsURLConnection.setDefaultHostnameVerifier ((hostname, session) -> true);//NOSONAR
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
            APIConfig apiConfig = app.generateTemplate(params);
            try(FileWriter fileWriter = new FileWriter(params.getConfig())) {
                if(params.getOutputFormat().equals(StandardExportParams.OutputFormat.yaml))
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
        } catch (IOException | CertificateEncodingException | NoSuchAlgorithmException | KeyManagementException e) {
            LOG.error("Error in processing :", e);
            if (e instanceof AppException) {
                AppException appException = (AppException) e;
                LOG.error("{} : Error code {}", appException.getError().getDescription(), appException.getError().getCode());
                return appException.getError().getCode();
            }
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
        TagMap apiManagerTags = new TagMap();
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
        api.setDescriptionType("original");
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
        Map<String, Object> securityProfiles = addInboundSecurityToAPI(frontendAuthType);
        String backendAuthType = parameters.getBackendAuthType();
        addOutboundSecurityToAPI(api, backendAuthType);
        String apiSpecLocation;
        if (uri.startsWith("https")) {
            apiSpecLocation = downloadCertificatesAndContent(api, parameters.getConfig(), uri);
        }else if (uri.startsWith("http")){
            apiSpecLocation = downloadContent(parameters.getConfig(), uri);
        }else{
            apiSpecLocation = parameters.getApiDefinition();
        }

        return new APIConfig(api, apiSpecLocation, securityProfiles);
    }

    public AuthType matchAuthType(String backendAuthType){
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

    public DeviceType matchDeviceType(String frontendAuthType){
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

    private Map<String, Object> addInboundSecurityToAPI(String frontendAuthType) throws AppException {
        DeviceType deviceType = matchDeviceType(frontendAuthType);
        LOG.info("Frontend Authentication type : {}", frontendAuthType );
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
            properties.put("removeCredentialsOnSuccess", "true");
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
        securityProfile.put("name", DEFAULT);
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

    public String writeAPISpecification(String url, String configPath, InputStream inputStream) throws IOException {
        String filename;
        try {
            filename = new File(new URL(url).getPath()).getName();
            String content = IOUtils.toString(inputStream, "UTF-8");
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
        }finally {
            if(inputStream != null){
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
        LOG.debug("Response Code : {}", responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            filePath = writeAPISpecification(url, configPath, httpURLConnection.getInputStream());
        }
        return filePath;
    }


    public String downloadCertificatesAndContent(API api, String configPath, String url) throws IOException, CertificateEncodingException, NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {//NOSONAR
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {//NOSONAR
            }
        }};

        File file = new File(configPath);
        String parent = file.getParent();
        Base64.Encoder encoder = Base64.getMimeEncoder(64, System.getProperty("line.separator").getBytes());
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
                if (basicConstraints == -1) {
                    if(caCerts.size() > 1) // ignore for self signed certs
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
        String certAlias = certificate.getSubjectDN().getName();
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
            LOG.warn("Created a random filename: {}" , filename );
        } else {
            filename = filename.replace(" ", "");
            filename = filename.replace("*", "");
            if (filename.startsWith(".")) filename = filename.replaceFirst("\\.", "");
        }
        return filename + ".crt";
    }
}
