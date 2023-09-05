package com.axway.apim.adapter;

import com.axway.apim.adapter.apis.*;
import com.axway.apim.adapter.client.apps.APIMgrAppsAdapter;
import com.axway.apim.adapter.custom.properties.APIManagerCustomPropertiesAdapter;
import com.axway.apim.adapter.user.APIManagerUserAdapter;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.api.model.Config;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.User;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.APIMCLICacheManager;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.DoNothingCacheManager;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.StateTransitionException;
import org.ehcache.Status;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The APIContract reflects the actual existing API in the API-Manager.
 *
 * @author cwiechmann@axway.com
 */
public class APIManagerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(APIManagerAdapter.class);
    public static final String ADMIN = "admin";
    public static final String OADMIN = "oadmin";
    public static final String USER = "user";

    private static APIManagerAdapter instance;

    private String apiManagerVersion = null;
    public static String apiManagerName = null;

    public static boolean initialized = false;

    public static final ObjectMapper mapper = new ObjectMapper();

    private static final Map<String, ClientApplication> clientCredentialToAppMap = new HashMap<>();

    private boolean usingOrgAdmin = false;
    private boolean hasAdminAccount = false;

    public static final String CREDENTIAL_TYPE_API_KEY = "apikeys";
    public static final String CREDENTIAL_TYPE_EXT_CLIENTID = "extclients";
    public static final String CREDENTIAL_TYPE_OAUTH = "oauth";

    public static final String SYSTEM_API_QUOTA = "00000000-0000-0000-0000-000000000000";
    public static final String APPLICATION_DEFAULT_QUOTA = "00000000-0000-0000-0000-000000000001";

    public static final String TYPE_FRONT_END = "proxies";
    public static final String TYPE_BACK_END = "apirepo";

    private static CoreParameters cmd;

    public static APIMCLICacheManager cacheManager;
    public APIManagerConfigAdapter configAdapter;
    public APIManagerCustomPropertiesAdapter customPropertiesAdapter;
    public APIManagerAlertsAdapter alertsAdapter;
    public APIManagerRemoteHostsAdapter remoteHostsAdapter;
    public APIManagerAPIAdapter apiAdapter;
    public APIManagerAPIMethodAdapter methodAdapter;
    public APIManagerPoliciesAdapter policiesAdapter;
    public APIManagerQuotaAdapter quotaAdapter;
    public APIManagerOrganizationAdapter orgAdapter;
    public APIManagerAPIAccessAdapter accessAdapter;
    public APIManagerOAuthClientProfilesAdapter oauthClientAdapter;
    public APIMgrAppsAdapter appAdapter;
    public APIManagerUserAdapter userAdapter;

    private static final HttpHelper httpHelper = new HttpHelper();

    public static synchronized APIManagerAdapter getInstance() throws AppException {
        if (instance == null) {
            instance = new APIManagerAdapter();
            cmd = CoreParameters.getInstance();
            cmd.validateRequiredParameters();
            instance.loginToAPIManager();
            instance.setApiManagerVersion();
            initialized = true;
            LOG.info("Successfully connected to API-Manager ({}) on: {}", instance.getApiManagerVersion(), cmd.getAPIManagerURL());
        }
        return instance;
    }

    public static synchronized void deleteInstance()  {
        if (cacheManager != null && cacheManager.getStatus() == Status.AVAILABLE) {
            LOG.debug("Closing cache ...");
            cacheManager.close();
            LOG.trace("Cache Closed.");
        }
        if (instance != null) {
            try {
                instance.logoutFromAPIManager();
            } catch (AppException e) {
                LOG.debug("Unable to logout ...", e);
            }
            instance.apiManagerVersion = null;
            instance = null;
        }
        initialized = false;
    }

    private void setApiManagerVersion() throws AppException {
        Config config = configAdapter.getConfig(false);
        apiManagerVersion = config.getProductVersion();
        if (usingOrgAdmin)
            LOG.info("Organization Administrator Self Service Enabled : {}", config.getOadminSelfServiceEnabled());
    }

    public void setApiManagerVersion(String apiManagerVersion) {
        this.apiManagerVersion = apiManagerVersion;
    }

    private APIManagerAdapter() {
        // For now this okay, may be replaced with a Factory later
        this.configAdapter = new APIManagerConfigAdapter();
        this.customPropertiesAdapter = new APIManagerCustomPropertiesAdapter();
        this.alertsAdapter = new APIManagerAlertsAdapter();
        this.remoteHostsAdapter = new APIManagerRemoteHostsAdapter();
        this.apiAdapter = new APIManagerAPIAdapter();
        this.methodAdapter = new APIManagerAPIMethodAdapter();
        this.policiesAdapter = new APIManagerPoliciesAdapter();
        this.quotaAdapter = new APIManagerQuotaAdapter();
        this.orgAdapter = new APIManagerOrganizationAdapter();
        this.accessAdapter = new APIManagerAPIAccessAdapter();
        this.oauthClientAdapter = new APIManagerOAuthClientProfilesAdapter();
        this.appAdapter = new APIMgrAppsAdapter();
        this.userAdapter = new APIManagerUserAdapter();
    }

    public void loginToAPIManager() throws AppException {
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/login").build();
            List<NameValuePair> params = new ArrayList<>();
            String username = cmd.getUsername();
            String password = cmd.getPassword();
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            POSTRequest loginRequest = new POSTRequest(new UrlEncodedFormEntity(params), uri);
            int statusCode = httpHelper.execute(loginRequest, false).getStatusCode();
            if (statusCode != 303 && (statusCode < 200 || statusCode > 299)) {
                LOG.warn("Login failed with statusCode: {} ... Try again in {} milliseconds. (you may set -retryDelay <milliseconds>)", statusCode, cmd.getRetryDelay());
                Thread.sleep(cmd.getRetryDelay());
                statusCode = httpHelper.execute(loginRequest, false).getStatusCode();
                if (statusCode != 303) {
                    throw new AppException("Login finally failed with statusCode: " + statusCode, ErrorCode.API_MANAGER_LOGIN_FAILED);
                } else {
                    LOG.info("Successfully logged in on retry. Received Status-Code: {}", statusCode);
                }
            }
            User user = getCurrentUser();
            String role = getHigherRole(user);
            if (role.equals(ADMIN)) {
                this.hasAdminAccount = true;
                // Also register this client as an Admin-Client
            } else if (role.equals(OADMIN)) {
                this.usingOrgAdmin = true;
            }
        } catch (IOException | URISyntaxException e) {
            throw new AppException("Can't login to API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String getHigherRole(User user) {
        String role = user.getRole();
        Map<String, String> organizations2Role = user.getOrgs2Role();
        if (organizations2Role == null)
            return role;
        List<String> roles = new ArrayList<>();
        if (role.equals(USER)) {
            for (String multiOrgRole : organizations2Role.values()) {
                if (multiOrgRole.equals(USER))
                    continue;
                roles.add(multiOrgRole);
            }
        }
        if (roles.isEmpty())
            return role;
        if (roles.contains(ADMIN)) {
            return ADMIN;
        } else if (roles.contains(OADMIN)) {
            return OADMIN;
        }
        return role;
    }

    public void logoutFromAPIManager() throws AppException {
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/login").build();
            DELRequest logoutRequest = new DELRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) logoutRequest.execute()) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != 204) {
                    String response = EntityUtils.toString(httpResponse.getEntity());
                    LOG.warn("Logout failed with statusCode: {}  Got response: {}", statusCode, response);
                }
            }
        } catch (Exception e) {
            throw new AppException("Can't logout from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }


    public static User getCurrentUser() throws AppException {
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/currentuser").build();
            GETRequest currentUserRequest = new GETRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) currentUserRequest.execute()) {
                getCsrfToken(httpResponse);
                String currentUser = EntityUtils.toString(httpResponse.getEntity());
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new AppException("Status-Code: " + statusCode + ", Can't get current-user for user '" + currentUser + "'",
                            ErrorCode.API_MANAGER_COMMUNICATION);
                }
                User user = mapper.readValue(currentUser, User.class);
                if (user == null) {
                    throw new AppException("Can't get current-user information on response: '" + currentUser + "'",
                            ErrorCode.API_MANAGER_COMMUNICATION);
                }
                return user;
            }
        } catch (Exception e) {
            throw new AppException("Error: '" + e.getMessage() + "' while parsing current-user information on response",
                    ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    private static void getCsrfToken(HttpResponse response) throws AppException {
        for (Header header : response.getAllHeaders()) {
            if (header.getName().equalsIgnoreCase("csrf-token")) {
                APIMHttpClient.getInstance().setCsrfToken(header.getValue());
                break;
            }
        }
    }

    private static CacheManager getCacheManager() {
        if (APIManagerAdapter.cacheManager != null) {
            if (APIManagerAdapter.cacheManager.getStatus() == Status.UNINITIALIZED)
                APIManagerAdapter.cacheManager.init();
            return APIManagerAdapter.cacheManager;
        }
        if (CoreParameters.getInstance().isIgnoreCache()) {
            APIManagerAdapter.cacheManager = new APIMCLICacheManager(new DoNothingCacheManager());
        } else {
            URL cacheConfigUrl;
            File cacheConfigFile = null;
            try {
                cacheConfigFile = new File(Utils.getInstallFolder() + "/conf/cacheConfig.xml");
                if (cacheConfigFile.exists()) {
                    LOG.debug("Using customer cache configuration file: {}", cacheConfigFile);
                    cacheConfigUrl = cacheConfigFile.toURI().toURL();
                } else {
                    cacheConfigUrl = APIManagerAdapter.class.getResource("/cacheConfig.xml");
                }
            } catch (MalformedURLException e1) {
                LOG.trace("Error reading customer cache config file: {} Using default configuration.", cacheConfigFile);
                cacheConfigUrl = APIManagerAdapter.class.getResource("/cacheConfig.xml");
            }
            XmlConfiguration xmlConfig = new XmlConfiguration(cacheConfigUrl);
            // The Cache-Manager creates an exclusive lock on the Cache-Directory, which means only on APIM-CLI can initialize it at a time
            // When running in a CI/CD pipeline, multiple CPIM-CLIs might be executed
            int initAttempts = 1;
            int maxAttempts = 100;
            do {
                try {
                    CacheManager ehcacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);//NOSONAR
                    APIManagerAdapter.cacheManager = new APIMCLICacheManager(ehcacheManager);
                    APIManagerAdapter.cacheManager.init();
                } catch (StateTransitionException e) {
                    LOG.warn("Error initializing cache - Perhaps another APIM-CLI is running that locks the cache. Retry again in 3 seconds. Attempts: {}/{}", initAttempts, maxAttempts);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                    }
                }
                initAttempts++;
            } while (APIManagerAdapter.cacheManager.getStatus() == Status.UNINITIALIZED && initAttempts <= maxAttempts);
        }
        return cacheManager;
    }

    public static <K, V> Cache<K, V> getCache(CacheType cacheType, Class<K> key, Class<V> value) {
        getCacheManager();
        if (CoreParameters.getInstance() instanceof StandardImportParams) {
            List<CacheType> enabledCaches = StandardImportParams.getInstance().getEnabledCacheTypes();
            APIManagerAdapter.cacheManager.setEnabledCaches(enabledCaches);
        }
        Cache<K, V> cache = APIManagerAdapter.cacheManager.getCache(cacheType.name(), key, value);
        if (CoreParameters.getInstance().clearCaches() != null && CoreParameters.getInstance().clearCaches().contains(cacheType)) {
            cache.clear();
            LOG.info("Cache: {} successfully cleared.", cacheType);
        }
        return cache;
    }


    /**
     * Checks if the API-Manager has at least given version. If the given requested version is the same or lower
     * than the actual API-Manager version, true is returned otherwise false.
     * This helps to use features, that are introduced with a certain version or even service-pack.
     *
     * @param version has the API-Manager this version of higher?
     * @return false if API-Manager doesn't have this version otherwise true
     */
    public static boolean hasAPIManagerVersion(String version) {
        String apiGatewayVersion = null;
        try {
            apiGatewayVersion = getInstance().getApiManagerVersion();
            List<String> managerVersion = getMajorVersions(apiGatewayVersion);
            List<String> requestedVersion = getMajorVersions(version);
            Date datedManagerVersion = getDateVersion(managerVersion);
            Date datedRequestedVersion = getDateVersion(requestedVersion);
            int managerSP = getServicePackVersion(apiGatewayVersion);
            int requestedSP = getServicePackVersion(version);
            for (int i = 0; i < requestedVersion.size(); i++) {
                int managerVer = Integer.parseInt(managerVersion.get(i));
                if (managerVer > Integer.parseInt(requestedVersion.get(i))) return true;
                if (managerVer < Integer.parseInt(requestedVersion.get(i))) return false;
                if (datedManagerVersion != null && datedRequestedVersion != null && datedManagerVersion.before(datedRequestedVersion))
                    return false;
            }
            if (requestedSP != 0 && datedManagerVersion != null) return true;
            if (managerSP < requestedSP) return false;
        } catch (Exception e) {
            LOG.warn("Can't parse API-Manager version: {} Requested version was: {} Returning false!", apiGatewayVersion, version);
            return false;
        }
        return true;
    }

    private static Date getDateVersion(List<String> managerVersion) {
        if (managerVersion.size() == 3) {
            try {
                String dateVersion = managerVersion.get(2);
                return new SimpleDateFormat("yyyyMMdd").parse(dateVersion);
            } catch (Exception e) {
                LOG.trace("API-Manager version does not to contain a dated version");
            }
        }
        return null;
    }

    private static int getServicePackVersion(String version) {
        int spNumber = 0;
        if (version.contains(" SP")) {
            try {
                String spVersion = version.substring(version.indexOf(" SP") + 3);
                spNumber = Integer.parseInt(spVersion);
            } catch (Exception e) {
                LOG.trace("Can't parse service pack version in version: {}", version);
            }
        }
        return spNumber;
    }

    private static List<String> getMajorVersions(String version) {
        List<String> majorNumbers = new ArrayList<>();
        String versionWithoutSP = version;
        if (version.contains(" SP")) {
            versionWithoutSP = version.substring(0, version.indexOf(" SP"));
        }
        try {
            String[] versions = versionWithoutSP.split("\\.");
            majorNumbers.addAll(Arrays.asList(versions));
        } catch (Exception e) {
            LOG.trace("Can't parse major version numbers in: {}", version);
        }
        return majorNumbers;
    }

    /**
     * The actual App-ID based on the AppName. Lazy implementation.
     *
     * @param credential The credentials (API-Key, Client-ID) which is registered for an application
     * @param type       of the credential. See APIManagerAdapter for potential credential types
     * @return the id of the organization
     * @throws AppException if JSON response from API-Manager can't be parsed
     */
    public ClientApplication getAppIdForCredential(String credential, String type) throws AppException {
        if (clientCredentialToAppMap.containsKey(type + "_" + credential)) {
            ClientApplication app = clientCredentialToAppMap.get(type + "_" + credential);
            LOG.info("Found existing application (in cache): {} based on credential (Type: {}): {}", app.getName(), type, credential);
            return app;
        }
        List<ClientApplication> allApps = this.appAdapter.getAllApplications(false); // Make sure, we loaded all apps before!
        LOG.debug("Searching credential (Type: {}): {} in: {} apps.", type, credential, allApps.size());
        Collection<ClientApplication> appIds = clientCredentialToAppMap.values();
        for (ClientApplication app : allApps) {
            if (appIds.contains(app)) continue;
            String response;
            try {
                URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + app.getId() + "/" + type).build();
                LOG.debug("Loading credentials of type: {} for application: {} from API-Manager.", type, type);
                RestAPICall getRequest = new GETRequest(uri);
                try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                    response = EntityUtils.toString(httpResponse.getEntity());
                    JsonNode clientIds = mapper.readTree(response);
                    if (clientIds.isEmpty()) {
                        LOG.debug("No credentials (Type: {}) found for application: {}", type, app.getName());
                        continue;
                    }
                    for (JsonNode clientId : clientIds) {
                        String key;
                        if (type.equals(CREDENTIAL_TYPE_API_KEY)) {
                            key = clientId.get("id").asText();
                        } else if (type.equals(CREDENTIAL_TYPE_EXT_CLIENTID) || type.equals(CREDENTIAL_TYPE_OAUTH)) {
                            if (clientId.get("clientId") == null) {
                                key = "NOT_FOUND";
                            } else {
                                key = clientId.get("clientId").asText();
                            }
                        } else {
                            throw new AppException("Unknown credential type: " + type, ErrorCode.UNXPECTED_ERROR);
                        }
                        LOG.debug("Found credential (Type: {}): {} for application: {}", type, key, app.getName());
                        clientCredentialToAppMap.put(type + "_" + key, app);
                        if (key.equals(credential)) {
                            LOG.info("Found existing application: {} ( {} ) based on credential (Type: {}): {}", app.getName(), app.getId(), type, credential);
                            return app;
                        }
                    }
                }
            } catch (Exception e) {
                throw new AppException("Can't load applications credentials.", ErrorCode.API_MANAGER_COMMUNICATION, e);
            }
        }
        LOG.error("No application found for credential ( {} ): {}", type, credential);
        return null;
    }

    public static Image getImageFromAPIM(URI uri, String baseFilename) throws AppException {
        Image image = new Image();
        try {
            RestAPICall getRequest = new GETRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == 404) return null; // No Image found
                if (statusCode != 200) {
                    String response = EntityUtils.toString(httpResponse.getEntity());
                    LOG.error("Can't read Image from API-Manager.. Message: {} Response-Code: {}", response, statusCode);
                    throw new AppException("Can't read Image from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION);
                }
                if (httpResponse.getEntity() == null)
                    return null; // no Image found in API-Manager
                try (InputStream is = httpResponse.getEntity().getContent()) {
                    image.setImageContent(IOUtils.toByteArray(is));
                }
                if (httpResponse.containsHeader("Content-Type")) {
                    String contentType = httpResponse.getHeaders("Content-Type")[0].getValue();
                    image.setContentType(contentType);
                }
                image.setBaseFilename(baseFilename);
                return image;
            }
        } catch (Exception e) {
            throw new AppException("Can't read Image from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    public String getApiManagerVersion() throws AppException {
        if (apiManagerVersion != null) {
            return apiManagerVersion;
        }
        apiManagerVersion = APIManagerAdapter.getInstance().configAdapter.getConfig(false).getProductVersion();
        return apiManagerVersion;
    }

    public static String getApiManagerName() throws AppException {
        if (APIManagerAdapter.apiManagerName != null) {
            return apiManagerName;
        }
        APIManagerAdapter.apiManagerName = APIManagerAdapter.getInstance().configAdapter.getConfig(false).getPortalName();
        return APIManagerAdapter.apiManagerName;
    }

    public static String getCertInfo(InputStream certificate, String password, CaCert cert) throws AppException {
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/certinfo").build();
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addBinaryBody("file", certificate, ContentType.create("application/x-x509-ca-cert"), cert.getCertFile())
                    .addTextBody("inbound", cert.getInbound())
                    .addTextBody("outbound", cert.getOutbound())
                    .addTextBody("passphrase", password)
                    .build();
            POSTRequest postRequest = new POSTRequest(entity, uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) postRequest.execute()) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                String response = EntityUtils.toString(httpResponse.getEntity());
                if (statusCode != 200) {
                    if (response != null && response.contains("Bad password")) {
                        LOG.debug("API-Manager failed to read certificate information: {} Got response: {}", cert.getCertFile(), response);
                        throw new AppException("Password for keystore: '" + cert.getCertFile() + "' is wrong.", ErrorCode.WRONG_KEYSTORE_PASSWORD);
                    }
                    throw new AppException("API-Manager failed to read certificate information from file. Got response: '" + response + "'", ErrorCode.API_MANAGER_COMMUNICATION);
                }
                return response;
            }
        } catch (Exception e) {
            throw new AppException("API-Manager failed to read certificate information from file.", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    /**
     * Helper method to translate a Base64 encoded format
     * as it's needed by the API-Manager.
     *
     * @param fileFontent the certificate content
     * @param filename    the name of the certificate file used as a reference in the generated Json object
     * @param contentType the content type
     * @return a Json-Object structure as needed by the API-Manager
     * @throws AppException when the certificate information can't be created
     */
    public static JsonNode getFileData(byte[] fileFontent, String filename, ContentType contentType) throws AppException {
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/filedata/").build();
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addBinaryBody("file", fileFontent, contentType, filename)
                    .build();
            POSTRequest postRequest = new POSTRequest(entity, uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) postRequest.execute()) {
                return mapper.readTree(httpResponse.getEntity().getContent());
            }
        } catch (Exception e) {
            throw new AppException("Can't read certificate information from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    /**
     * @return true, when admin credentials are provided
     * @throws AppException when the API-Manager instance is not initialized
     */
    public static boolean hasAdminAccount() throws AppException {
        return APIManagerAdapter.getInstance().hasAdminAccount;
    }
}
