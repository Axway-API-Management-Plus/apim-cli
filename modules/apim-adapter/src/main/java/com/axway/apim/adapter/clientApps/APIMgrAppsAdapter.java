package com.axway.apim.adapter.clientApps;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.CacheType;
import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter.Type;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.User;
import com.axway.apim.api.model.apps.*;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class APIMgrAppsAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(APIMgrAppsAdapter.class);

    Map<ClientAppFilter, String> apiManagerResponse = new HashMap<>();

    Map<String, String> subscribedAppAPIManagerResponse = new HashMap<>();

    CoreParameters cmd = CoreParameters.getInstance();

    ObjectMapper mapper = APIManagerAdapter.mapper;

    Cache<String, String> applicationsCache;
    Cache<String, String> applicationsSubscriptionCache;
    Cache<String, String> applicationsCredentialCache;
    Cache<String, String> applicationsQuotaCache;

    public APIMgrAppsAdapter() {
        applicationsCache = APIManagerAdapter.getCache(CacheType.APPLICATIONS_CACHE, String.class, String.class);
        applicationsSubscriptionCache = APIManagerAdapter.getCache(CacheType.APPLICATIONS_SUBSCRIPTION_CACHE, String.class, String.class);
        applicationsCredentialCache = APIManagerAdapter.getCache(CacheType.APPLICATIONS_CREDENTIAL_CACHE, String.class, String.class);
        // Must be refactored to use Quota-Adapter instead of doing this in
        applicationsQuotaCache = APIManagerAdapter.getCache(CacheType.APPLICATIONS_QUOTA_CACHE, String.class, String.class);
    }

    /**
     * Returns a list of applications.
     *
     * @throws AppException if applications cannot be retrieved
     */
    private void readApplicationsFromAPIManager(ClientAppFilter filter) throws AppException {
        if (this.apiManagerResponse != null && this.apiManagerResponse.get(filter) != null) return;
        HttpResponse httpResponse = null;
        try {
            String requestedId = "";
            if (filter.getApplicationId() != null) {
                if (applicationsCache.containsKey(filter.getApplicationId())) {
                    this.apiManagerResponse.put(filter, applicationsCache.get(filter.getApplicationId()));
                    return;
                }
                requestedId = "/" + filter.getApplicationId();
            }
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications" + requestedId)
                    .addParameters(filter.getFilters())
                    .build();
            LOG.debug("Sending request to find existing applications: " + uri);
            RestAPICall getRequest = new GETRequest(uri, APIManagerAdapter.hasAdminAccount());
            httpResponse = getRequest.execute();
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 404) {
                // Nothing found - Simulate an empty response
                this.apiManagerResponse.put(filter, "[]");
                return;
            }
            String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            if (response.startsWith("{")) { // Got a single response!
                response = "[" + response + "]";
            }
            this.apiManagerResponse.put(filter, response);
            if (filter.getApplicationId() != null) {
                applicationsCache.put(filter.getApplicationId(), response);
            }
        } catch (Exception e) {
            throw new AppException("Can't initialize API-Manager App-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
        } finally {
            try {
                if (httpResponse != null)
                    ((CloseableHttpResponse) httpResponse).close();
            } catch (Exception ignore) {
            }
        }
    }

    URI getApplicationsUri(ClientAppFilter filter) throws URISyntaxException, AppException {
        String requestedId = "";
        if (filter == null) filter = new ClientAppFilter.Builder().build();
        if (filter.getApplicationId() != null) {
            requestedId = "/" + filter.getApplicationId();
        }
        return new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications" + requestedId)
                .addParameters(filter.getFilters())
                .build();
    }

    public List<ClientApplication> getApplications(ClientAppFilter filter, boolean logProgress) throws AppException {
        readApplicationsFromAPIManager(filter);
        List<ClientApplication> apps;
        try {
            if (this.apiManagerResponse.get(filter) == null) return null;
            apps = mapper.readValue(this.apiManagerResponse.get(filter), new TypeReference<List<ClientApplication>>() {
            });
            LOG.debug("Found: " + apps.size() + " applications");
            for (int i = 0; i < apps.size(); i++) {
                ClientApplication app = apps.get(i);
                addImage(app, filter.isIncludeImage());
                if (filter.isIncludeQuota()) {
                    app.setAppQuota(APIManagerAdapter.getInstance().quotaAdapter.getQuota(app.getId(), null, true, true));
                }
                addApplicationCredentials(app, filter.isIncludeCredentials());
                addOauthResources(app, filter.isIncludeOauthResources());
                addApplicationPermissions(app, filter.isIncludeAppPermissions());
                addAPIAccess(app, filter.isIncludeAPIAccess());
                if (logProgress && apps.size() > 5)
                    Utils.progressPercentage(i, apps.size(), "Loading details of " + apps.size() + " applications");
            }
            apps.removeIf(filter::filter);
            Utils.addCustomPropertiesForEntity(apps, this.apiManagerResponse.get(filter), filter);
            if (logProgress && apps.size() > 5) System.out.print("\n");
        } catch (Exception e) {
            throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
        return apps;
    }

    public List<ClientApplication> getAllApplications(boolean logProgress) throws AppException {
        return getApplications(new ClientAppFilter.Builder().build(), logProgress);
    }

    public List<ClientApplication> getAppsSubscribedWithAPI(String apiId) throws AppException {
        readAppsSubscribedFromAPIManager(apiId);
        List<ClientApplication> subscribedApps;
        try {
            subscribedApps = mapper.readValue(this.subscribedAppAPIManagerResponse.get(apiId), new TypeReference<List<ClientApplication>>() {
            });
        } catch (IOException e) {
            LOG.error("Error cant load subscribes applications from API-Manager. Can't parse response: " + this.subscribedAppAPIManagerResponse.get(apiId), e);
            throw new AppException("Error cant load subscribes applications from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
        return subscribedApps;
    }

    private void readAppsSubscribedFromAPIManager(String apiId) throws AppException {
        if (this.subscribedAppAPIManagerResponse.get(apiId) != null) return;
        if (applicationsSubscriptionCache.containsKey(apiId)) {
            subscribedAppAPIManagerResponse.put(apiId, applicationsSubscriptionCache.get(apiId));
            return;
        }

        String response = null;
        URI uri;
        HttpResponse httpResponse = null;
        if (!APIManagerAdapter.hasAPIManagerVersion("7.7")) {
            throw new AppException("API-Manager: " + APIManagerAdapter.apiManagerVersion + " doesn't support /proxies/<apiId>/applications", ErrorCode.UNXPECTED_ERROR);
        }
        try {
            uri = new URIBuilder(CoreParameters.getInstance().getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/proxies/" + apiId + "/applications").build();
            RestAPICall getRequest = new GETRequest(uri, APIManagerAdapter.hasAdminAccount());
            LOG.debug("Load subscribed applications for API-ID: " + apiId + " from API-Manager");
            httpResponse = getRequest.execute();
            response = EntityUtils.toString(httpResponse.getEntity());
            subscribedAppAPIManagerResponse.put(apiId, response);
            applicationsSubscriptionCache.put(apiId, response);
        } catch (Exception e) {
            LOG.error("Error cant load subscribes applications from API-Manager. Can't parse response: " + response, e);
            throw new AppException("Error cant load subscribes applications from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
        } finally {
            try {
                if (httpResponse != null)
                    ((CloseableHttpResponse) httpResponse).close();
            } catch (Exception ignore) {
            }
        }
    }


    public ClientApplication getApplication(ClientAppFilter filter) throws AppException {
        List<ClientApplication> apps = getApplications(filter, false);
        return uniqueApplication(apps);
    }

    private ClientApplication uniqueApplication(List<ClientApplication> apps) throws AppException {
        if (apps.size() > 1) {
            throw new AppException("No unique application found", ErrorCode.APP_NAME_IS_NOT_UNIQUE);
        }
        if (apps.size() == 0) return null;
        return apps.get(0);
    }

    void addApplicationCredentials(ClientApplication app, boolean addCredentials) throws Exception {
        if (!addCredentials) return;
        String response;
        List<ClientAppCredential> credentials;
        String[] types = new String[]{"extclients", "oauth", "apikeys"};
        TypeReference[] classTypes = new TypeReference[]{new TypeReference<List<ExtClients>>() {
        }, new TypeReference<List<OAuth>>() {
        }, new TypeReference<List<APIKey>>() {
        }};
        for (int i = 0; i < types.length; i++) {
            try {
                String type = types[i];
                TypeReference<List<ClientAppCredential>> classType = classTypes[i];
                if (!applicationsCredentialCache.containsKey(app.getId() + "|" + type)) {
                    URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + app.getId() + "/" + type)
                            .build();
                    RestAPICall getRequest = new GETRequest(uri);
                    try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                        response = EntityUtils.toString(httpResponse.getEntity());
                        int statusCode = httpResponse.getStatusLine().getStatusCode();
                        if (statusCode != 200) {
                            LOG.error("Error reading application credentials. Response-Code: " + statusCode + ". Got response: '" + response + "'");
                            throw new AppException("Error creating application' Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                        }
                        applicationsCredentialCache.put(app.getId() + "|" + type, response);
                    }
                } else {
                    response = applicationsCredentialCache.get(app.getId() + "|" + type);
                }
                credentials = mapper.readValue(response, classType);
                app.getCredentials().addAll(credentials);
            } catch (Exception e) {
                throw new AppException("Error reading application credentials.", ErrorCode.CANT_CREATE_API_PROXY, e);
            }
        }
    }

    private void addOauthResources(ClientApplication app, boolean includeOauthResources) throws AppException {
        if (!includeOauthResources) return;
        String endpoint = "oauthresource";
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + app.getId() + "/" + endpoint)
                    .build();
            RestAPICall getRequest = new GETRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                String response = EntityUtils.toString(httpResponse.getEntity());
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    LOG.error("Error reading application oauth resources. Response-Code: " + statusCode + ". Got response: '" + response + "'");
                    throw new AppException("Error reading application oauth resources' Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                }
                TypeReference<List<ClientAppOauthResource>> classType = new TypeReference<List<ClientAppOauthResource>>() {
                };
                List<ClientAppOauthResource> oauthResources = mapper.readValue(response, classType);
                app.getOauthResources().addAll(oauthResources);
            }
        } catch (Exception e) {
            throw new AppException("Error reading application oauth resources. Error: " + e.getMessage(), ErrorCode.CANT_CREATE_API_PROXY, e);
        }

    }

    private void addApplicationPermissions(ClientApplication app, boolean includeApplicationPermissions) throws AppException {
        if (!includeApplicationPermissions) return;
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + app.getId() + "/permissions")
                    .build();
            RestAPICall getRequest = new GETRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                String response = EntityUtils.toString(httpResponse.getEntity());
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    LOG.error("Error reading application permissions. Response-Code: " + statusCode + ". Got response: '" + response + "'");
                    throw new AppException("Error reading application permissions' Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                }
                TypeReference<List<ApplicationPermission>> classType = new TypeReference<List<ApplicationPermission>>() {
                };
                List<ApplicationPermission> appPermissions = mapper.readValue(response, classType);
                for (ApplicationPermission permission : appPermissions) {
                    User user = APIManagerAdapter.getInstance().userAdapter.getUserForId(permission.getUserId());
                    if (user == null)
                        throw new AppException("No user found for ID: " + permission.getUserId() + " assigned to application: " + app.getName(), ErrorCode.UNXPECTED_ERROR);
                    permission.setUser(user);
                }
                app.getPermissions().addAll(appPermissions);
            }
        } catch (Exception e) {
            throw new AppException("Error reading application permissions. Error: " + e.getMessage(), ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }

    void addAPIAccess(ClientApplication app, boolean addAPIAccess) throws Exception {
        if (!addAPIAccess) return;
        try {
            List<APIAccess> apiAccess = APIManagerAdapter.getInstance().accessAdapter.getAPIAccess(app, Type.APPLICATIONS, true);
            app.getApiAccess().addAll(apiAccess);
        } catch (Exception e) {
            throw new AppException("Error reading application API Access.", ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }

    void addImage(ClientApplication app, boolean addImage) throws Exception {
        if (!addImage) return;
        URI uri;
        if (app.getImageUrl() == null) return;
        uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + app.getId() + "/image")
                .build();
        Image image = APIManagerAdapter.getImageFromAPIM(uri, "app-image");
        app.setImage(image);
    }

    public void updateApplication(ClientApplication desiredApp, ClientApplication actualApp) throws AppException {
        createOrUpdateApplication(desiredApp, actualApp);
    }

    public void createApplication(ClientApplication desiredApp) throws AppException {
        createOrUpdateApplication(desiredApp, null);
    }

    public void createOrUpdateApplication(ClientApplication desiredApp, ClientApplication actualApp) throws AppException {
        createOrUpdateApplication(desiredApp, actualApp, false);
    }

    private void createOrUpdateApplication(ClientApplication desiredApp, ClientApplication actualApp, boolean baseAppOnly) throws AppException {
        ClientApplication createdApp;
        try {
            CoreParameters cmd = CoreParameters.getInstance();
            URI uri;
            if (actualApp == null) {
                uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications").build();
            } else {
                if (desiredApp.getApiAccess() != null && desiredApp.getApiAccess().size() == 0)
                    desiredApp.setApiAccess(null);
                desiredApp.setId(actualApp.getId());
                uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + actualApp.getId()).build();
            }
            mapper.setSerializationInclusion(Include.NON_NULL);
            try {
                RestAPICall request;
                if (actualApp == null) {
                    FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
                            SimpleBeanPropertyFilter.serializeAllExcept("credentials", "appQuota", "organization", "image", "appScopes", "permissions"));
                    mapper.setFilterProvider(filter);
                    String json = mapper.writeValueAsString(desiredApp);
                    HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                    request = new POSTRequest(entity, uri);
                } else {
                    FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
                            SimpleBeanPropertyFilter.serializeAllExcept("credentials", "appQuota", "organization", "image", "apis", "appScopes", "permissions"));
                    mapper.setFilterProvider(filter);
                    String json = mapper.writeValueAsString(desiredApp);
                    HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                    request = new PUTRequest(entity, uri);
                }
                try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode < 200 || statusCode > 299) {
                        LOG.error("Error creating/updating application. Response-Code: " + statusCode + ". Got response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
                        throw new AppException("Error creating/updating application. Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                    }
                    createdApp = mapper.readValue(httpResponse.getEntity().getContent(), ClientApplication.class);
                    // enabled=false for a new application is ignored during initial creation, hence another update of the just created app is required
                    if (actualApp == null && !desiredApp.isEnabled()) {
                        createOrUpdateApplication(desiredApp, createdApp, true);
                    }
                }
            } catch (Exception e) {
                throw new AppException("Error creating/updating application. Error: " + e.getMessage(), ErrorCode.CANT_CREATE_API_PROXY, e);
            }
            // Remove application from cache to force reload next time
            applicationsCache.remove(createdApp.getId());
            if (baseAppOnly) return;

            desiredApp.setId(createdApp.getId());
            saveImage(desiredApp, actualApp);
            saveAPIAccess(desiredApp, actualApp);
            saveCredentials(desiredApp, actualApp);
            manageOAuthResources(desiredApp, actualApp);
            manageAppPermissions(desiredApp, actualApp);
            saveQuota(desiredApp, actualApp);

        } catch (Exception e) {
            throw new AppException("Error creating/updating application. Error: " + e.getMessage(), ErrorCode.ERR_CREATING_APPLICATION, e);
        }
    }

    private void saveImage(ClientApplication app, ClientApplication actualApp) throws URISyntaxException, AppException {
        if (app.getImage() == null) return;
        if (actualApp != null && app.getImage().equals(actualApp.getImage())) return;
        URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + app.getId() + "/image").build();
        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", app.getImage().getInputStream(), ContentType.create("image/jpeg"), app.getImage().getBaseFilename())
                .build();
        RestAPICall apiCall = new POSTRequest(entity, uri);
        try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) apiCall.execute()) {

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode > 299) {
                LOG.error("Error saving/updating application image. Response-Code: " + statusCode + ". Got response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
            }
        } catch (Exception e) {
            throw new AppException("Error uploading application image. Error: " + e.getMessage(), ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }

    private void saveCredentials(ClientApplication app, ClientApplication actualApp) throws JsonProcessingException {
        if (app.getCredentials() == null || app.getCredentials().size() == 0) return;
        String endpoint;
        for (ClientAppCredential cred : app.getCredentials()) {

            if (actualApp != null && actualApp.getCredentials().contains(cred))
                continue; //nothing to do

            boolean update = false;
            FilterProvider filter;
            if (cred instanceof OAuth) {

                endpoint = "oauth";
                filter = new SimpleFilterProvider().setDefaultFilter(
                        SimpleBeanPropertyFilter.serializeAllExcept("credentialType", "clientId", "apiKey"));
                final String credentialId = ((OAuth) cred).getClientId();
                Optional<ClientAppCredential> opt = searchForExistingCredential(actualApp, credentialId);
                if (opt.isPresent()) {
                    LOG.info("Found oauth credential with same ID for application {}", actualApp.getId());
                    //I found a credential with same id name but different in some properties, I have to update it
                    endpoint += "/" + credentialId;
                    update = true;
                    cred.setId(credentialId);
                    cred.setApplicationId(actualApp.getId());
                    cred.setCreatedBy(opt.get().getCreatedBy());
                    cred.setCreatedOn(opt.get().getCreatedOn());
                }
            } else if (cred instanceof ExtClients) {
                final String credentialId = ((ExtClients) cred).getClientId();
                endpoint = "extclients";
                filter = new SimpleFilterProvider().setDefaultFilter(
                        SimpleBeanPropertyFilter.serializeAllExcept("credentialType", "apiKey", "applicationId"));
                Optional<ClientAppCredential> opt = searchForExistingCredential(actualApp, credentialId);
                if (opt.isPresent()) {
                    LOG.info("Found extclients credential with same ID");
                    //I found a credential with same id name but different in some properties, I have to update it
                    endpoint += "/" + cred.getId();
                    update = true;
                    cred.setId(credentialId);
                    cred.setCreatedBy(opt.get().getCreatedBy());
                    cred.setCreatedOn(opt.get().getCreatedOn());
                }
            } else if (cred instanceof APIKey) {
                final String credentialId = ((APIKey) cred).getApiKey();
                endpoint = "apikeys";
                filter = new SimpleFilterProvider().setDefaultFilter(
                        SimpleBeanPropertyFilter.serializeAllExcept("credentialType", "clientId", "apiKey"));
                Optional<ClientAppCredential> opt = searchForExistingCredential(actualApp, credentialId);
                if (opt.isPresent()) {
                    LOG.info("Found apikey credential with same ID");
                    //I found a credential with same id name but different in some properties, I have to update it
                    endpoint += "/" + ((APIKey) cred).getApiKey();
                    update = true;
                    cred.setId(opt.get().getId());
                    cred.setApplicationId(opt.get().getApplicationId());
                    cred.setSecret(opt.get().getSecret());
                    cred.setCreatedBy(opt.get().getCreatedBy());
                    cred.setCreatedOn(opt.get().getCreatedOn());
                }
            } else {
                throw new AppException("Unsupported credential: " + cred.getClass().getName(), ErrorCode.UNXPECTED_ERROR);
            }

            try {
                URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + app.getId() + "/" + endpoint).build();
                mapper.setFilterProvider(filter);
                mapper.setSerializationInclusion(Include.NON_NULL);
                String json = mapper.writeValueAsString(cred);
                HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);

                RestAPICall request = (update ? new PUTRequest(entity, uri) : new POSTRequest(entity, uri));
                try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode < 200 || statusCode > 299) {
                        LOG.error("Error saving/updating application credentials. Response-Code: " + statusCode + ". Got response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
                        throw new AppException("Error creating application' Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                    }
                }
            } catch (Exception e) {
                throw new AppException("Error creating application. Error: " + e.getMessage(), ErrorCode.CANT_CREATE_API_PROXY, e);
            }
        }
    }

    protected Optional<ClientAppCredential> searchForExistingCredential(ClientApplication actualApp,
                                                                        final String credentialId) {
        if (actualApp != null) {
            return actualApp.getCredentials().stream().filter(o -> {
                if (o instanceof OAuth)
                    return ((OAuth) o).getClientId().equals(credentialId);
                if (o instanceof ExtClients)
                    return o.getId().equals(credentialId);
                if (o instanceof APIKey)
                    return o.getId().equals(credentialId);
                return false;
            }).findFirst();
        } else {
            return Optional.empty();
        }

    }

    private void saveQuota(ClientApplication app, ClientApplication actualApp) throws AppException {
        if (app.getAppQuota() == null || app.getAppQuota().getRestrictions().size() == 0) return;
        if (actualApp != null && app.getAppQuota().equals(actualApp.getAppQuota())) return;
        if (!APIManagerAdapter.hasAdminAccount()) {
            LOG.warn("Ignoring quota, as no admin account is given");
            return;
        }
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + app.getId() + "/quota").build();
            FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept("apiId", "apiName", "apiVersion", "apiPath", "vhost", "queryVersion"));
            mapper.setFilterProvider(filter);
            mapper.setSerializationInclusion(Include.NON_NULL);
            String json = mapper.writeValueAsString(app.getAppQuota());
            HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            // Use an admin account for this request
            RestAPICall request;
            if (actualApp == null) {
                request = new POSTRequest(entity, uri, true);
            } else {
                request = new PUTRequest(entity, uri, true);
            }
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode > 299) {
                    LOG.error("Error creating/updating application quota. Response-Code: " + statusCode + ". Got response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
                    throw new AppException("Error creating application' Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                }
                // Force reload of this quota next time
                applicationsQuotaCache.remove(app.getId());
            }
        } catch (Exception e) {
            throw new AppException("Error creating application quota. Error: " + e.getMessage(), ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }

    private void saveAPIAccess(ClientApplication app, ClientApplication actualApp) throws AppException {
        if (app.getApiAccess() == null || app.getApiAccess().size() == 0) return;
        if (actualApp != null && app.getApiAccess().equals(actualApp.getApiAccess())) return;
        if (!APIManagerAdapter.hasAdminAccount()) {
            LOG.warn("Ignoring API-Access, as no admin account is given");
            return;
        }
        APIManagerAPIAccessAdapter accessAdapter = APIManagerAdapter.getInstance().accessAdapter;
        accessAdapter.saveAPIAccess(app.getApiAccess(), app, Type.APPLICATIONS);
    }

    private void manageOAuthResources(ClientApplication desiredApp, ClientApplication actualApp) throws AppException {
        List<ClientAppOauthResource> scopes2Update = new ArrayList<>();
        List<ClientAppOauthResource> scopes2Create = new ArrayList<>();
        List<ClientAppOauthResource> scopes2Delete = new ArrayList<>();
        getScopes2AddOrUpdate(actualApp, desiredApp, scopes2Update, scopes2Create, scopes2Delete);
        saveOrUpdateOAuthResources(desiredApp, scopes2Create, false);
        saveOrUpdateOAuthResources(desiredApp, scopes2Update, true);
        deleteOAuthResources(desiredApp, scopes2Delete);
    }

    private void saveOrUpdateOAuthResources(ClientApplication desiredApp, List<ClientAppOauthResource> scopes2Create, boolean update) throws AppException {
        if (scopes2Create == null || scopes2Create.size() == 0) return;
        for (ClientAppOauthResource res : scopes2Create) {
            String endpoint = "oauthresource";
            try {
                if (update) {
                    endpoint += "/" + res.getId();
                    LOG.debug("Oauth resource already exists, updating");
                } else {
                    LOG.debug("Oauth resource not found, creating");
                }
                FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
                        SimpleBeanPropertyFilter.serializeAllExcept("scopes", "enabled"));
                mapper.setFilterProvider(filter);
                mapper.setSerializationInclusion(Include.NON_NULL);
                String json = mapper.writeValueAsString(res);
                HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + desiredApp.getId() + "/" + endpoint).build();
                RestAPICall request = (update ? new PUTRequest(entity, uri) : new POSTRequest(entity, uri));
                try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode < 200 || statusCode > 299) {
                        LOG.error("Error saving/updating application oauth resource. Response-Code: " + statusCode + ". Got response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
                        throw new AppException("Error creating application' Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                    }
                }
            } catch (Exception e) {
                throw new AppException("Error creating application. Error: " + e.getMessage(), ErrorCode.CANT_CREATE_API_PROXY, e);
            }
        }
    }

    private void deleteOAuthResources(ClientApplication desiredApp, List<ClientAppOauthResource> scopes2Delete) throws AppException {
        if (scopes2Delete == null || scopes2Delete.size() == 0) return;
        for (ClientAppOauthResource res : scopes2Delete) {
            try {
                URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + desiredApp.getId() + "/oauthresource/" + res.getId()).build();
                RestAPICall request = new DELRequest(uri, true);
                try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode != 204) {
                        LOG.error("Error deleting application scope. Response-Code: " + statusCode + ". Got response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
                        throw new AppException("Error deleting application scope. Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                    }
                    LOG.info("Application scope: " + res.getScope() + " for application: " + desiredApp.getName() + " successfully deleted");
                }
            } catch (Exception e) {
                throw new AppException("Error deleting application scope. Error: " + e.getMessage(), ErrorCode.API_MANAGER_COMMUNICATION, e);
            }
        }
    }

    private void getScopes2AddOrUpdate(ClientApplication actualApp, ClientApplication desiredApp,
                                       List<ClientAppOauthResource> scopes2Update, List<ClientAppOauthResource> scopes2Create, List<ClientAppOauthResource> scopes2Delete) {
        List<ClientAppOauthResource> existingScopes = null;
        List<ClientAppOauthResource> desiredScopes = desiredApp.getOauthResources();
        if (actualApp != null) {
            existingScopes = actualApp.getOauthResources();
        }
        if (existingScopes == null) existingScopes = new ArrayList<>();
        if (desiredScopes != null) {
            // Iterate over the given desired scopes
            for (ClientAppOauthResource scope : desiredScopes) {
                boolean existingScopeFound = false;
                // Compare each desired scope, if it is already included in the existing scopes
                for (ClientAppOauthResource existingScope : existingScopes) {
                    // If it is the same scope (based on the name) ...
                    if (scope.getScope().equals(existingScope.getScope())) {
                        // Check, if the scopes are not equal and if so take over the configuration and put it to the list of scopes to update
                        if (!scope.equals(existingScope)) {
                            existingScope.setDefaultScope(scope.isDefaultScope());
                            existingScope.setEnabled(scope.isEnabled());
                            scopes2Update.add(existingScope);
                        }
                        existingScopeFound = true;
                        break;
                    }
                }
                if (!existingScopeFound) scopes2Create.add(scope);
            }
        }
        // finally iterate over all existing scopes and check if they are still desired
        for (ClientAppOauthResource existingScope : existingScopes) {
            boolean actualScopeFound = false;
            for (ClientAppOauthResource desiredScope : desiredScopes) {
                if (existingScope.getScope().equals(desiredScope.getScope())) {
                    actualScopeFound = true;
                    break; // As the actual scope is still desired
                }
            }
            if (!actualScopeFound) scopes2Delete.add(existingScope);
        }
    }

    private void manageAppPermissions(ClientApplication desiredApp, ClientApplication actualApp) throws AppException {
        List<ApplicationPermission> appPermissions2Update = new ArrayList<>();
        List<ApplicationPermission> appPermissions2Create = new ArrayList<>();
        List<ApplicationPermission> appPermissions2Delete = new ArrayList<>();

        getApplicationPermissions2AddOrUpdate(actualApp, desiredApp, appPermissions2Update, appPermissions2Create, appPermissions2Delete);
        saveOrUpdateApplicationPermissions(desiredApp, appPermissions2Create, false);
        saveOrUpdateApplicationPermissions(desiredApp, appPermissions2Update, true);
        deleteApplicationPermissions(desiredApp, appPermissions2Delete);
    }

    private void getApplicationPermissions2AddOrUpdate(ClientApplication actualApp, ClientApplication desiredApp,
                                                       List<ApplicationPermission> appPermissions2Update, List<ApplicationPermission> appPermissions2Create, List<ApplicationPermission> appPermissions2Delete) {
        List<ApplicationPermission> existingPermissions = null;
        List<ApplicationPermission> desiredPermissions = desiredApp.getPermissions();
        if (actualApp != null) {
            existingPermissions = actualApp.getPermissions();
        }
        if (existingPermissions == null) existingPermissions = new ArrayList<>();
        if (desiredPermissions != null) {
            // Iterate over the given desired permissions
            for (ApplicationPermission permission : desiredPermissions) {
                boolean existingPermissionFound = false;
                // Compare each desired permissions, if it is already included in the existing permissions
                for (ApplicationPermission existingPermission : existingPermissions) {
                    // Check if the user already has permission
                    if (permission.getUsername().equals(existingPermission.getUsername())) {
                        // Check is the application permission is correct (View vs. Modify), if not it must be updated
                        if (!permission.getPermission().equals(existingPermission.getPermission())) {
                            existingPermission.setPermission(permission.getPermission());
                            appPermissions2Update.add(existingPermission);
                        }
                        existingPermissionFound = true;
                        break;
                    }
                }
                // If user no access yet, it must be created
                if (!existingPermissionFound) appPermissions2Create.add(permission);
            }
        }
        // finally iterate over all existing permissions and check if they are still desired
        for (ApplicationPermission existingPermission : existingPermissions) {
            boolean actualPermissionFound = false;
            for (ApplicationPermission desiredPermission : desiredPermissions) {
                if (existingPermission.getUsername().equals(desiredPermission.getUsername())) {
                    actualPermissionFound = true;
                    break; // As the actual scope is still desired
                }
            }
            if (!actualPermissionFound) appPermissions2Delete.add(existingPermission);
        }
    }

    private void saveOrUpdateApplicationPermissions(ClientApplication desiredApp, List<ApplicationPermission> permissions2Create, boolean update) throws AppException {
        if (permissions2Create == null || permissions2Create.size() == 0) return;
        HttpResponse httpResponse = null;
        for (ApplicationPermission appPerm : permissions2Create) {
            String endpoint = "permissions";
            try {
                if (update) {
                    endpoint += "/" + appPerm.getId();
                    LOG.debug("Application permissions resource already exists, updating it.");
                } else {
                    LOG.debug("Application permission not found, creating it.");
                }
                FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
                        SimpleBeanPropertyFilter.serializeAllExcept("user"));
                mapper.setFilterProvider(filter);
                mapper.setSerializationInclusion(Include.NON_NULL);
                String json = mapper.writeValueAsString(appPerm);
                HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + desiredApp.getId() + "/" + endpoint).build();
                RestAPICall request = (update ? new PUTRequest(entity, uri) : new POSTRequest(entity, uri));
                httpResponse = request.execute();
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode > 299) {
                    LOG.error("Error saving/updating application permission. Response-Code: " + statusCode + ". Got response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
                    throw new AppException("Error saving/updating application permission' Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                }
            } catch (Exception e) {
                throw new AppException("Error creating application. Error: " + e.getMessage(), ErrorCode.CANT_CREATE_API_PROXY, e);
            } finally {
                try {
                    ((CloseableHttpResponse) httpResponse).close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void deleteApplicationPermissions(ClientApplication desiredApp, List<ApplicationPermission> permissions2Delete) throws AppException {
        if (permissions2Delete == null || permissions2Delete.size() == 0) return;
        for (ApplicationPermission appPerm : permissions2Delete) {
            try {
                URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + desiredApp.getId() + "/permissions/" + appPerm.getId()).build();
                RestAPICall request = new DELRequest(uri, true);
                try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode != 204) {
                        LOG.error("Error deleting application permission. Response-Code: " + statusCode + ". Got response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
                        throw new AppException("Error deleting application permission. Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                    }
                    LOG.info("Application permission for user: " + appPerm.getUsername() + " for application: " + desiredApp.getName() + " successfully deleted");
                }
            } catch (Exception e) {
                throw new AppException("Error deleting application permission. Error: " + e.getMessage(), ErrorCode.API_MANAGER_COMMUNICATION, e);
            }
        }
    }

    public void deleteApplication(ClientApplication app) throws AppException {
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + app.getId()).build();
            RestAPICall request = new DELRequest(uri, true);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != 204) {
                    LOG.error("Error deleting application. Response-Code: " + statusCode + ". Got response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
                    throw new AppException("Error deleting application. Response-Code: " + statusCode + "", ErrorCode.API_MANAGER_COMMUNICATION);
                }
                LOG.info("Application: " + app.getName() + " (" + app.getId() + ")" + " successfully deleted");
            }
        } catch (Exception e) {
            throw new AppException("Error deleting application", ErrorCode.ACCESS_ORGANIZATION_ERR, e);
        }
    }


    public void setTestApiManagerResponse(ClientAppFilter filter, String apiManagerResponse) {
        this.apiManagerResponse.put(filter, apiManagerResponse);
    }

    public void setTestSubscribedAppAPIManagerResponse(String apiId, String subscribedAppAPIManagerResponse) {
        this.subscribedAppAPIManagerResponse.put(apiId, subscribedAppAPIManagerResponse);
    }
}
