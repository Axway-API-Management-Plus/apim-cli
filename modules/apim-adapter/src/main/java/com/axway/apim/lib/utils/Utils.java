package com.axway.apim.lib.utils;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.custom.properties.APIManagerCustomPropertiesAdapter;
import com.axway.apim.adapter.jackson.CustomYamlFactory;
import com.axway.apim.api.API;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.CustomPropertiesEntity;
import com.axway.apim.api.model.CustomProperty;
import com.axway.apim.api.model.CustomProperty.Option;
import com.axway.apim.api.model.TagMap;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.CustomPropertiesFilter;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.error.ErrorCodeMapper;
import com.axway.apim.lib.utils.rest.Console;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.Permission;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    public static final String MASKED_VALUE = "********";

    public enum FedKeyType {
        FilterCircuit("<key type='FilterCircuit'>"), OAuthAppProfile("<key type='OAuthAppProfile'>");

        private final String keyType;

        FedKeyType(String keyType) {
            this.keyType = keyType;
        }

        public String getKeyType() {
            return keyType;
        }
    }

    public static String getAPIDefinitionUriFromFile(String pathToAPIDefinition) throws AppException {
        String uriToAPIDefinition;
        try (BufferedReader br = new BufferedReader(new FileReader(pathToAPIDefinition))) {
            uriToAPIDefinition = br.readLine();
            return uriToAPIDefinition;
        } catch (Exception e) {
            throw new AppException("Can't load file:" + pathToAPIDefinition, ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
        }
    }

    public static void progressPercentage(int remain, int total, String prefix) {
        if (remain > total) {
            throw new IllegalArgumentException();
        }
        int maxBareSize = 10; // 10unit for 100%
        int remainPercent = ((100 * remain) / total) / maxBareSize;
        char defaultChar = '-';
        String icon = "*";
        String bare = new String(new char[maxBareSize]).replace('\0', defaultChar) + "]";
        StringBuilder bareDone = new StringBuilder();
        bareDone.append(prefix).append(" [");
        for (int i = 0; i < remainPercent; i++) {
            bareDone.append(icon);
        }
        String bareRemain = bare.substring(remainPercent);
        Console.print("\r" + bareDone + bareRemain + " " + remainPercent * 10 + "%");
        if (remain == total) {
            Console.print("\n");
        }
    }

    public static boolean askYesNo(String question) {
        return Utils.askYesNo(question, "[Y]", "[N]");
    }

    public static boolean askYesNo(String question, String positive, String negative) {
        Scanner input = new Scanner(System.in);
        // Convert everything to upper case for simplicity...
        positive = positive.toUpperCase();
        negative = negative.toUpperCase();
        String answer;
        do {
            Console.print(question + " ");
            answer = input.next().trim().toUpperCase();
        } while (!answer.matches(positive) && !answer.matches(negative));
        input.close();
        // Assess if we match a positive response
        return answer.matches(positive);
    }

    public static String getExternalPolicyName(String policy) {
        return getExternalPolicyName(policy, null);
    }

    public static String getExternalPolicyName(String policy, FedKeyType keyType) {
        if (keyType == null) keyType = FedKeyType.FilterCircuit;
        if (policy.startsWith("<key")) {
            policy = policy.substring(policy.indexOf(keyType.getKeyType()));
            policy = policy.substring(policy.indexOf("value='") + 7, policy.lastIndexOf("'/></key>"));
        }
        return policy;
    }

    /**
     * This method is replacing variables such as ${TokenEndpoint} in the given file
     * with declared variables coming from either the Environment-Variables or
     * from system-properties.
     *
     * @param inputFile The API-Config file to be replaced and returned as String
     * @return a String representation of the API-Config-File
     * @throws IOException if the file can't be found
     */
    public static String substituteVariables(File inputFile) throws IOException {
        String givenConfig = new String(Files.readAllBytes(inputFile.toPath()), StandardCharsets.UTF_8);
        givenConfig = StringSubstitutor.replace(givenConfig, System.getenv());
        if (CoreParameters.getInstance().getProperties() == null) return givenConfig;
        StringSubstitutor stringSubstitutor = new StringSubstitutor(CoreParameters.getInstance().getProperties());
        return stringSubstitutor.replace(givenConfig);
    }

    /**
     * @param stage          defines a stage which identifies a prod or qa environment.
     * @param stageConfig    can be given to define the stage config file to be used
     * @param baseConfigFile the base configuration
     * @return the staged config file
     */
    public static File getStageConfig(String stage, String stageConfig, File baseConfigFile) {
        if (stage == null && stageConfig == null) return null;
        if (stage != null && new File(stage).exists()) { // This is to support testing with dynamically created files!
            return new File(stage);
        }
        String baseConfig;
        try {
            baseConfig = baseConfigFile.getCanonicalPath();
        } catch (IOException e) {
            LOG.error("Error reading stage config file based on the config file.", e);
            return null;
        }
        // If a stageConfig is given it used with preference over the stage
        if (stageConfig != null) {
            // Supporting a StageConfigFile with an absolute path
            if (new File(stageConfig).exists()) return new File(stageConfig);
            // Perhaps the stageConfigFile is relative to the main base config
            File stageFile = new File(baseConfigFile.getParent() + File.separator + stageConfig);
            if (stageFile.exists()) return stageFile;
            LOG.warn("No stage configuration file found with name: {} It must be either absolute or relative to the main config file.", stageConfig);
        } else {
            if (!stage.equals("NOT_SET")) {
                File stageFile = new File(baseConfig.substring(0, baseConfig.lastIndexOf(".") + 1) + stage + baseConfig.substring(baseConfig.lastIndexOf(".")));
                File subDirStageFile = new File(stageFile.getParentFile() + "/" + stage + "/" + stageFile.getName());
                if (stageFile.exists()) {
                    return stageFile;
                } else if (subDirStageFile.exists()) {
                    return subDirStageFile;
                }
            }
        }
        LOG.debug("No stage provided");
        return null;
    }

    public static File locateConfigFile(String configFileName) throws AppException {
        try {
            configFileName = URLDecoder.decode(configFileName, "UTF-8");
            File configFile = new File(configFileName);
            if (configFile.exists()) return configFile;
            // This is mainly to load the samples sitting inside the package!
            File installFolder = getInstallFolder();
            configFile = new File(installFolder + File.separator + configFileName);
            if (configFile.exists()) return configFile;
            throw new AppException("Unable to find given Config-File: '" + configFileName + "'", ErrorCode.CANT_READ_CONFIG_FILE);
        } catch (Exception e) {
            throw new AppException("Unable to find given Config-File: '" + configFileName + "'", ErrorCode.CANT_READ_CONFIG_FILE, e);
        }
    }

    public static File getInstallFolder() {
        Enumeration<Permission> permissions = Utils.class.getProtectionDomain().getPermissions().elements();
        File installFolder = null;
        while (permissions.hasMoreElements()) {
            Permission permission = permissions.nextElement();
            if (permission.getClass() == FilePermission.class) {
                String permName = permission.getName();
                // APIM-CLI runs compiled e.g. C:\Axway\Tools\apim-cli-1.12.0-SNAPSHOT\lib\apimcli-apim-adapter-1.12.0-SNAPSHOT.jar
                if (permName.endsWith(".jar")) {
                    installFolder = new File(permName).getParentFile().getParentFile();
                    break;
                    // APIM-CLI runs out of the classes folder
                } else if (permission.getName().endsWith("classes\\-") || permission.getName().endsWith("classes/-")) {
                    installFolder = new File(permName.substring(0, permName.length() - 2));
                    break;
                }
            }
        }
        if (installFolder == null) {
            LOG.error("Could not determine install folder.");
        } else {
            if (!installFolder.isDirectory()) {
                LOG.error("Determined install folder: {} is not a directory.", installFolder);
            }
        }
        return installFolder;
    }

    public static void validateCustomProperties(Map<String, String> customProperties, Type type) throws AppException {
        APIManagerCustomPropertiesAdapter propertiesAdapter = APIManagerAdapter.getInstance().getCustomPropertiesAdapter();
        Map<String, CustomProperty> configuredCustomProperties = propertiesAdapter.getCustomProperties(type);
        Map<String, CustomProperty> requiredConfiguredCustomProperties = propertiesAdapter.getRequiredCustomProperties(type);
        if (customProperties != null) {
            for (Map.Entry<String, String> entry : customProperties.entrySet()) {
                String desiredCustomPropertyValue = entry.getValue();
                String desiredCustomProperty = entry.getKey();
                CustomProperty configuredCustomProperty = configuredCustomProperties.get(desiredCustomProperty);
                if (configuredCustomProperty == null) {
                    throw new AppException("The custom-property: '" + desiredCustomProperty + "' is not configured in API-Manager.", ErrorCode.CANT_READ_CONFIG_FILE);
                }
                if (configuredCustomProperty.getType() != null && (configuredCustomProperty.getType().equals("select") || configuredCustomProperty.getType().equals("switch"))) {
                    boolean valueFound = false;
                    List<Option> knownOptions = configuredCustomProperty.getOptions();
                    if (knownOptions == null) {
                        LOG.warn("Skipping custom property validation, as the custom-property: {} with type: {} has no options configured. Please check your custom properties configuration.", desiredCustomProperty, configuredCustomProperty.getType());
                        break;
                    }
                    for (Option knownOption : knownOptions) {
                        if (knownOption.getValue().equals(desiredCustomPropertyValue)) {
                            valueFound = true;
                            break;
                        }
                    }
                    if (!valueFound) {
                        throw new AppException("The value: '" + desiredCustomPropertyValue + "' is not a valid option for custom property: '" + desiredCustomProperty + "'", ErrorCode.CANT_READ_CONFIG_FILE);
                    }
                }
                // Remove
                requiredConfiguredCustomProperties.remove(desiredCustomProperty);
            }
        }
        // Finally check, if missing custom properties are left
        if (requiredConfiguredCustomProperties != null) {
            if (requiredConfiguredCustomProperties.isEmpty()) return;
            String missingCustomProperties = StringUtils.join(requiredConfiguredCustomProperties.keySet(), ",");
            throw new AppException("Missing required custom properties : '" + missingCustomProperties + "'", ErrorCode.CANT_READ_CONFIG_FILE);
        }
    }

    public static void addCustomPropertiesForEntity(List<? extends CustomPropertiesEntity> entities, String json, CustomPropertiesFilter filter) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // Custom-Properties will be added depending on the given Properties in the filter
        if (filter.getCustomProperties() == null || entities.isEmpty()) {
            return;
        }
        Map<String, JsonNode> enitityAsJsonMappedWithId = new HashMap<>();
        JsonNode jsonPayload = mapper.readTree(json);
        // Create a map based on the API-ID containing the original JSON-Payload received from API-Manager
        for (JsonNode node : jsonPayload) {
            String apiId = node.get("id").asText();
            enitityAsJsonMappedWithId.put(apiId, node);
        }
        Map<String, String> customProperties = new LinkedHashMap<>();
        // Iterate over all APIs (at this point not yet having the custom-properties serialized)
        for (CustomPropertiesEntity entity : entities) {
            // Get the original JSON-Payload for the current API fetched from API-Manager
            JsonNode node = enitityAsJsonMappedWithId.get(entity.getId());
            // Iterate over all requested Custom-Properties that should be returned
            for (String customPropKey : filter.getCustomProperties()) {
                // Try to get the value for that custom property from the JSON-Payload
                JsonNode value = node.get(customPropKey);
                // If there is nothing found - skip it.
                if (value == null) continue;
                // Add it to the map of custom properties that will be attached to the API
                customProperties.put(customPropKey, value.asText());
            }
            entity.setCustomProperties((customProperties.isEmpty()) ? null : customProperties);
        }
    }

    public static Long getParsedDate(String date) throws AppException {
        List<String> dateFormats = Arrays.asList("dd.MM.yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy");
        SimpleDateFormat format;
        Date retDate = null;
        for (String dateFormat : dateFormats) {
            format = new SimpleDateFormat(dateFormat, Locale.US);
            format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Z")));
            try {
                retDate = format.parse(date);
            } catch (ParseException e) {
                LOG.error("Formatting error", e);
            }
            if (retDate != null && retDate.after(new Date())) {
                LOG.info("Parsed retirementDate: {} using format: {} to: {}", date, dateFormat, retDate);
                break;
            }
        }
        if (retDate == null || retDate.before(new Date())) {
            throw new AppException("Unable to parse the given retirementDate using the following formats: " + dateFormats + ". Please note the retirementDate must be in the future.", ErrorCode.CANT_READ_CONFIG_FILE);
        }
        return retDate.getTime();
    }

    public static String getAPILogString(API api) {
        if (api == null) return "N/A";
        return api.getName() + " " + api.getVersion() + " (" + api.getVersion() + ")";
    }

    public static boolean isHttpUri(String pathToAPIDefinition) {
        String httpUri = pathToAPIDefinition.substring(pathToAPIDefinition.indexOf("@") + 1);
        return (httpUri.startsWith("http://") || httpUri.startsWith("https://"));
    }

    public static String handleOpenAPIServerUrl(String serverUrl, String backendBasePath) throws MalformedURLException {
        String newBackendBasePath;
        if (isHttpUri(serverUrl)) {
            URL openApiUrl = new URL(serverUrl);
            String path = openApiUrl.getPath();
            if (backendBasePath.endsWith("/")) {
                newBackendBasePath = backendBasePath.substring(0, backendBasePath.length() - 1) + path;
            } else newBackendBasePath = backendBasePath + path;
        } else {
            if (serverUrl.startsWith("/") && !backendBasePath.endsWith("/"))
                newBackendBasePath = backendBasePath + serverUrl;
            else if (backendBasePath.endsWith("/"))
                newBackendBasePath = backendBasePath.substring(0, backendBasePath.length() - 1) + serverUrl;
            else newBackendBasePath = backendBasePath + "/" + serverUrl;
        }
        return newBackendBasePath;
    }

    public static String ignoreBasePath(String serverUrl) {
        if (isHttpUri(serverUrl)) {
            URI uri = URI.create(serverUrl);
            return serverUrl.substring(0, serverUrl.length() - uri.getPath().length());
        }
        return serverUrl;
    }

    public static boolean compareValues(Object actualValue, Object desiredValue) {
        if (actualValue instanceof List) {
            return ((List<?>) actualValue).size() == ((List<?>) desiredValue).size() && ((List<?>) actualValue).containsAll((List<?>) desiredValue) && ((List<?>) desiredValue).containsAll((List<?>) actualValue);
        } else {
            return actualValue.equals(desiredValue);
        }
    }

    public static String getEncryptedPassword() {
        return MASKED_VALUE;
    }

    public static String createFileName(String host, String stage, String prefix) throws AppException {
        DateFormat df = new SimpleDateFormat("ddMMyyyy-HHmm");
        String dateTime = df.format(new Date());
        if (stage != null) {
            host = stage;
        }
        return prefix + host + "_" + APIManagerAdapter.getCurrentUser().getLoginName() + "_" + dateTime + ".csv";
    }

    public static boolean equalsTagMap(TagMap source, TagMap target) {
        if (source == target) return true;
        if (source == null || target == null)
            return false;
        if (source.size() != target.size()) return false;
        for (Map.Entry<String, String[]> entry : target.entrySet()) {
            String tagName = entry.getKey();
            if (!source.containsKey(tagName)) return false;
            String[] myTags = entry.getValue();
            String[] otherTags = source.get(tagName);
            if (!Objects.deepEquals(myTags, otherTags)) return false;
        }
        return true;
    }

    public static int handleAppException(Exception e, Logger logger, ErrorCodeMapper errorCodeMapper) {
        if (e instanceof AppException) {
            ErrorCode errorCode = ((AppException) e).getError();
            if (errorCode == ErrorCode.SUCCESS) {
                return ErrorCode.SUCCESS.getCode();
            } else {
                logger.error("Unexpected error :", e);
                return errorCodeMapper.getMapedErrorCode(errorCode).getCode();
            }
        } else {
            logger.error("Unexpected error :", e);
            return errorCodeMapper.getMapedErrorCode(ErrorCode.UNXPECTED_ERROR).getCode();
        }
    }

    public static void logPayload(Logger logger, HttpResponse httpResponse) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("APIManager Response : {}", EntityUtils.toString(httpResponse.getEntity()));
        }
    }

    public static void logPayload(Logger logger, String httpResponse) {
        if (logger.isDebugEnabled()) {
            logger.debug("APIManager Response : {}", httpResponse);
        }
    }

    public static void sleep(int retryDelay) {
        try {
            Thread.sleep(retryDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void deleteInstance(APIManagerAdapter apiManagerAdapter) {
        if (apiManagerAdapter != null)
            apiManagerAdapter.deleteInstance();
    }

    public static ObjectMapper createObjectMapper(File configFile) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        try {
            // Check the config file is json
            mapper.readTree(configFile);
            LOG.debug("Handling JSON Configuration file: {}", configFile);
        } catch (IOException ioException) {
            mapper = new ObjectMapper(CustomYamlFactory.createYamlFactory());
            LOG.debug("Handling Yaml Configuration file: {}", configFile);
        }
        return mapper;
    }

    public static void deleteDirectory(File localFolder) throws AppException {
        LOG.debug("Existing local export folder: {} already exists and will be deleted.", localFolder);
        try {
            FileUtils.deleteDirectory(localFolder);
        } catch (IOException e) {
            throw new AppException("Error deleting local folder", ErrorCode.UNXPECTED_ERROR, e);
        }
    }
}
