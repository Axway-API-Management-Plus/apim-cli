package com.axway.apim.api.specification;

import com.axway.apim.api.model.DesiredAPISpecification;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.HTTPClient;
import com.axway.apim.lib.utils.URLParser;
import com.axway.apim.lib.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;

public class APISpecificationFactory {

    private APISpecificationFactory() {
        throw new IllegalStateException("APISpecificationFactory class");
    }

    private static final Logger LOG = LoggerFactory.getLogger(APISpecificationFactory.class);

    private static final ArrayList<Class<?>> specificationTypes = new ArrayList<Class<?>>() {
        private static final long serialVersionUID = 1L;
        {
            add(Swagger2xSpecification.class);
            add(Swagger1xSpecification.class);
            add(OAS3xSpecification.class);
            add(WSDLSpecification.class);
            add(WADLSpecification.class);
            add(ODataV2Specification.class);
            add(ODataV3Specification.class);
            add(ODataV4Specification.class);
        }
    };

    public static APISpecification getAPISpecification(DesiredAPISpecification desiredAPISpec, String configBaseDir, String apiName) throws AppException {
        APISpecification spec = getAPISpecification(getAPIDefinitionContent(desiredAPISpec.getResource(), configBaseDir), desiredAPISpec.getResource(), apiName, true, true);
        spec.setFilterConfig(desiredAPISpec.getFilter()).filterAPISpecification();
        return spec;
    }

    public static APISpecification getAPISpecification(String apiDefinitionFile, String configBaseDir, String apiName) throws AppException {
        return getAPISpecification(getAPIDefinitionContent(apiDefinitionFile, configBaseDir), apiDefinitionFile, apiName, true, true);
    }

    public static APISpecification getAPISpecification(byte[] apiSpecificationContent, String apiDefinitionFile, String apiName) throws AppException {
        return getAPISpecification(apiSpecificationContent, apiDefinitionFile, apiName, true, true);
    }


    public static APISpecification getAPISpecification(byte[] apiSpecificationContent, String apiDefinitionFile, String apiName, boolean failOnError, boolean logDetectedVersion) throws AppException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handle API-Specification: {} , apiDefinitionFile: {} , API Name : {} ", getContentStart(apiSpecificationContent), apiDefinitionFile, apiName);
        }
        for (Class clazz : specificationTypes) {
            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                APISpecification spec = (APISpecification) constructor.newInstance();
                spec.setApiSpecificationFile(apiDefinitionFile);
                if (!spec.parse(apiSpecificationContent)) {
                    LOG.debug("Can't handle API specification with class: {} " , clazz.getName());
                } else {
                    String addNote = "";
                    if (spec.getAPIDefinitionType().getAdditionalNote() != null) {
                        addNote = "\n                                 | " + spec.getAPIDefinitionType().getAdditionalNote();
                    }
                    if (logDetectedVersion) {
                        LOG.info("Detected: {} specification. {}{}" , spec.getAPIDefinitionType().niceName, spec.getAPIDefinitionType().getNote(), addNote);
                    }
                    return spec;
                }
            } catch (AppException e) {
                throw e;
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.error("Can't handle API specification with class: " + clazz.getName(), e);
                }
            }
        }
        if (!failOnError) {
            LOG.error("API: {} has a unknown/invalid API-Specification: {}" , apiName, getContentStart(apiSpecificationContent));
            return new UnknownAPISpecification(apiName);
        }
        LOG.error("API: {} has a unknown/invalid API-Specification: {}" , apiName, getContentStart(apiSpecificationContent));
        throw new AppException("Can't handle API specification. No suitable API-Specification implementation available.", ErrorCode.UNSUPPORTED_API_SPECIFICATION);
    }

    static String getContentStart(byte[] apiSpecificationContent) {
        try {
            if (apiSpecificationContent == null) return "API-Specification is null";
            return (apiSpecificationContent.length < 200) ? new String(apiSpecificationContent, 0, apiSpecificationContent.length) : new String(apiSpecificationContent, 0, 200) + "...";
        } catch (Exception e) {
            return "Cannot get content from API-Specification. " + e.getMessage();
        }
    }

    private static byte[] getAPIDefinitionContent(String apiDefinitionFile, String configBaseDir) throws AppException {
        try (InputStream stream = getAPIDefinitionAsStream(apiDefinitionFile, configBaseDir)) {
            Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            return IOUtils.toByteArray(reader, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AppException("Can't read API-Definition from file", ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
        }
    }

    private static InputStream getAPIDefinitionAsStream(String apiDefinitionFile, String configBaseDir) throws AppException {
        InputStream is;
        if (apiDefinitionFile.endsWith(".url")) {
            return getAPIDefinitionFromURL(Utils.getAPIDefinitionUriFromFile(apiDefinitionFile));
        } else if (Utils.isHttpUri(apiDefinitionFile)) {
            return getAPIDefinitionFromURL(apiDefinitionFile);
        } else if(apiDefinitionFile.startsWith("data")){
            byte[] data = Base64.getDecoder().decode(apiDefinitionFile.replaceFirst("data:.+,", ""));
            return new ByteArrayInputStream(data);
        }else {
            try {
                File inputFile = new File(apiDefinitionFile);
                if (inputFile.exists()) {
                    LOG.info("Reading API-Definition (Swagger/WSDL) from file: {} (relative path)", apiDefinitionFile);
                    is = Files.newInputStream(Paths.get(apiDefinitionFile));
                } else {
                    inputFile = new File(configBaseDir + File.separator + apiDefinitionFile);
                    LOG.info("Reading API-Definition (Swagger/WSDL) from file: {} (absolute path)",  inputFile.getCanonicalFile());
                    if (inputFile.exists()) {
                        is = Files.newInputStream(inputFile.toPath());
                    } else {
                        // Have to remove leading slash (Read more: https://stackoverflow.com/questions/16570523/getresourceasstream-returns-null
                        is = APISpecificationFactory.class.getClassLoader().getResourceAsStream(apiDefinitionFile.replaceFirst("/", ""));
                    }
                }
                if (is == null) {
                    throw new AppException("Unable to read Swagger/WSDL file from: " + apiDefinitionFile, ErrorCode.CANT_READ_API_DEFINITION_FILE);
                }
            } catch (Exception e) {
                throw new AppException("Unable to read Swagger/WSDL file from: " + apiDefinitionFile, ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
            }

        }
        return is;
    }

    private static InputStream getAPIDefinitionFromURL(String urlToAPIDefinition) throws AppException {
        URLParser url = new URLParser(urlToAPIDefinition);
        String uri = url.getUri();
        String username = url.getUsername();
        String password = url.getPassword();
        try(HTTPClient httpClient = new HTTPClient(uri, username, password)){
            RequestConfig config = RequestConfig.custom()
                    .setRelativeRedirectsAllowed(true)
                    .setCircularRedirectsAllowed(true)
                    .build();
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(config);
            try(CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                String response = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                if (statusCode >= 200 && statusCode < 300) {
                    return new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new AppException("Cannot load API-Specification from URI. Received Status-Code: " + statusCode + ", Response: '" + response + "'",
                            ErrorCode.CANT_READ_API_DEFINITION_FILE);
                }
            }
        } catch (Exception e) {
            throw new AppException("Cannot load API-Specification from URI: " + uri, ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
        }
    }
}
