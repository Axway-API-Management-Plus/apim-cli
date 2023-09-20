package com.axway.apim.adapter.client.apps;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.CacheType;
import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter.Type;
import com.axway.apim.api.model.*;
import com.axway.apim.api.model.apps.*;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
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
    public static final String APPLICATIONS = "/applications";
    public static final String ERROR_CREATING_APPLICATION_ERROR = "Error creating application. Error: ";
    public static final String ERROR_CREATING_APPLICATION_RESPONSE_CODE = "Error creating application Response-Code: ";
    public static final String PERMISSIONS = "permissions";
    public static final String API_KEY = "apiKey";
    public static final String CREDENTIAL_TYPE = "credentialType";

    Map<ClientAppFilter, String> apiManagerResponse = new HashMap<>();
    Map<String, String> subscribedAppAPIManagerResponse = new HashMap<>();
    CoreParameters cmd = CoreParameters.getInstance();
    ObjectMapper mapper = APIManagerAdapter.mapper;
    Cache<String, String> applicationsCache;
    Cache<String, String> applicationsSubscriptionCache;
    Cache<String, String> applicationsCredentialCache;
    Cache<String, String> applicationsQuotaCache;

    public APIMgrAppsAdapter() {
        applicationsCache = APIManagerAdapter.getCache(CacheType.applicationsCache, String.class, String.class);
        applicationsSubscriptionCache = APIManagerAdapter.getCache(CacheType.applicationsSubscriptionCache, String.class, String.class);
        applicationsCredentialCache = APIManagerAdapter.getCache(CacheType.applicationsCredentialCache, String.class, String.class);
        // Must be refactored to use Quota-Adapter instead of doing this in
        applicationsQuotaCache = APIManagerAdapter.getCache(CacheType.applicationsQuotaCache, String.class, String.class);
    }

    /**
     * Returns a list of applications.
     *
     * @throws AppException if applications cannot be retrieved
     */
    private void readApplicationsFromAPIManager(ClientAppFilter filter) throws AppException {
        if (this.apiManagerResponse.get(filter) != null) return;
        try {
            String requestedId = "";
            if (filter.getApplicationId() != null) {
                if (applicationsCache.containsKey(filter.getApplicationId())) {
                    this.apiManagerResponse.put(filter, applicationsCache.get(filter.getApplicationId()));
                    return;
                }
                requestedId = "/" + filter.getApplicationId();
            }
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS + requestedId)
                .addParameters(filter.getFilters())
                .build();
            LOG.debug("Sending request to find existing applications: {}", uri);
            RestAPICall getRequest = new GETRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
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
            }
        } catch (Exception e) {
            throw new AppException("Can't initialize API-Manager App-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    URI getApplicationsUri(ClientAppFilter filter) throws URISyntaxException, AppException {
        String requestedId = "";
        if (filter == null) filter = new ClientAppFilter.Builder().build();
        if (filter.getApplicationId() != null) {
            requestedId = "/" + filter.getApplicationId();
        }
        return new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS + requestedId)
            .addParameters(filter.getFilters())
            .build();
    }

    public List<ClientApplication> getApplications(ClientAppFilter filter, boolean logProgress) throws AppException {
        readApplicationsFromAPIManager(filter);
        List<ClientApplication> apps;
        try {
            if (this.apiManagerResponse.get(filter) == null) return Collections.emptyList();
            apps = mapper.readValue(this.apiManagerResponse.get(filter), new TypeReference<List<ClientApplication>>() {
            });
            LOG.debug("Found: {} applications", apps.size());
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
            if (logProgress && apps.size() > 5) Console.print("\n");
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
        try {
            URI uri = new URIBuilder(CoreParameters.getInstance().getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/proxies/" + apiId + APPLICATIONS).build();
            RestAPICall getRequest = new GETRequest(uri);
            LOG.debug("Load subscribed applications for API-ID: {} from API-Manager", apiId);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                String response = EntityUtils.toString(httpResponse.getEntity());
                subscribedAppAPIManagerResponse.put(apiId, response);
                applicationsSubscriptionCache.put(apiId, response);
            }
        } catch (Exception e) {
            throw new AppException("Error cant load subscribes applications from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
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
        if (apps.isEmpty()) return null;
        return apps.get(0);
    }

    void addApplicationCredentials(ClientApplication app, boolean addCredentials) throws AppException {
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
                    URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS + "/" + app.getId() + "/" + type)
                        .build();
                    RestAPICall getRequest = new GETRequest(uri);
                    try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                        response = EntityUtils.toString(httpResponse.getEntity());
                        int statusCode = httpResponse.getStatusLine().getStatusCode();
                        if (statusCode != 200) {
                            LOG.error("Error reading application credentials. Response-Code: {} Got response: {}", statusCode, response);
                            throw new AppException(ERROR_CREATING_APPLICATION_RESPONSE_CODE + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
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
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS + "/" + app.getId() + "/" + endpoint)
                .build();
            RestAPICall getRequest = new GETRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                String response = EntityUtils.toString(httpResponse.getEntity());
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    LOG.error("Error reading application oauth resources. Response-Code: {} Got response: {}", statusCode, response);
                    throw new AppException("Error reading application oauth resources' Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
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
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS + "/" + app.getId() + "/permissions")
                .build();
            RestAPICall getRequest = new GETRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) getRequest.execute()) {
                String response = EntityUtils.toString(httpResponse.getEntity());
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    LOG.error("Error reading application permissions. Response-Code: {} Got response: {}", statusCode, response);
                    throw new AppException("Error reading application permissions' Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
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

    void addAPIAccess(ClientApplication app, boolean addAPIAccess) throws AppException {
        if (!addAPIAccess) return;
        try {
            List<APIAccess> apiAccess = APIManagerAdapter.getInstance().accessAdapter.getAPIAccess(app, Type.applications, true);
            app.getApiAccess().addAll(apiAccess);
        } catch (AppException e) {
            throw new AppException("Error reading application API Access.", ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }

    void addImage(ClientApplication app, boolean addImage) throws AppException, URISyntaxException {
        if (!addImage) return;
        URI uri;
        if (app.getImageUrl() == null) return;
        uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS + "/" + app.getId() + "/image")
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
        LOG.debug("Actual Application : {} vs Desired Application : {}", actualApp, desiredApp);
        try {
            mapper.setSerializationInclusion(Include.NON_NULL);
            // Remove application from cache to force reload next time
            ClientApplication createdApp = upsertApplication(desiredApp, actualApp);
            applicationsCache.remove(createdApp.getId());
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

    public ClientApplication upsertApplication(ClientApplication desiredApp, ClientApplication actualApp) throws AppException {
        ClientApplication createdApp;
        try {
            if (actualApp == null) {
                LOG.info("Creating new application");
                URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS).build();
                FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
                    SimpleBeanPropertyFilter.serializeAllExcept("credentials", "appQuota", "organization", "image", "appScopes", PERMISSIONS));
                mapper.setFilterProvider(filter);
                String json = mapper.writeValueAsString(desiredApp);
                HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                RestAPICall request = new POSTRequest(entity, uri);
                try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode < 200 || statusCode > 299) {
                        LOG.error("Error creating application. Response-Code: {}", statusCode);
                        throw new AppException("Error creating application. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                    }
                    createdApp = mapper.readValue(httpResponse.getEntity().getContent(), ClientApplication.class);
                    // enabled=false for a new application is ignored during initial creation, hence another update of the just created app is required
                    if (!desiredApp.isEnabled()) {
                        uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS + "/" + createdApp.getId()).build();
                        desiredApp.setId(createdApp.getId());
                        createdApp = updateApplication(uri, desiredApp);
                    }
                }
            } else if (!actualApp.equals(desiredApp)) {
                LOG.info("Updating application : {}", desiredApp.getName());
                URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS + "/" + actualApp.getId()).build();
                desiredApp.setId(actualApp.getId());
                createdApp = updateApplication(uri, desiredApp);
            } else {
                createdApp = actualApp;
            }
        } catch (Exception e) {
            throw new AppException("Error creating/updating application. Error: " + e.getMessage(), ErrorCode.CANT_CREATE_API_PROXY, e);
        }
        return createdApp;
    }

    public ClientApplication updateApplication(URI uri, ClientApplication clientApplication) throws IOException {
        FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
            SimpleBeanPropertyFilter.serializeAllExcept("credentials", "appQuota", "organization", "image", "apis", "appScopes", PERMISSIONS));
        mapper.setFilterProvider(filter);
        String json = mapper.writeValueAsString(clientApplication);
        HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        RestAPICall request = new PUTRequest(entity, uri);
        try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode > 299) {
                LOG.error("Error updating application. Response-Code: {}", statusCode);
                throw new AppException("Error updating application. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
            }
            return mapper.readValue(httpResponse.getEntity().getContent(), ClientApplication.class);
        }
    }

    private void saveImage(ClientApplication app, ClientApplication actualApp) throws URISyntaxException, AppException {
        if (app.getImage() == null) return;
        if (actualApp != null && app.getImage().equals(actualApp.getImage())) return;
        URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS + "/" + app.getId() + "/image").build();
        HttpEntity entity = MultipartEntityBuilder.create()
            .addBinaryBody("file", app.getImage().getInputStream(), ContentType.create("image/jpeg"), app.getImage().getBaseFilename())
            .build();
        RestAPICall apiCall = new POSTRequest(entity, uri);
        try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) apiCall.execute()) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode > 299) {
                LOG.error("Error saving/updating application image. Response-Code: {}", statusCode);
                Utils.logPayload(LOG, httpResponse);
            }
        } catch (Exception e) {
            throw new AppException("Error uploading application image. Error: " + e.getMessage(), ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }

    private void saveCredentials(ClientApplication app, ClientApplication actualApp) throws JsonProcessingException {
        if (app.getCredentials() == null || app.getCredentials().isEmpty()) return;
        for (ClientAppCredential cred : app.getCredentials()) {
            if (actualApp != null && actualApp.getCredentials().contains(cred))
                continue; //nothing to do
            boolean update = false;
            StringBuilder endpoint = new StringBuilder();
            FilterProvider filter;
            if (cred instanceof OAuth) {
                endpoint.append("oauth");
                filter = new SimpleFilterProvider().setDefaultFilter(
                    SimpleBeanPropertyFilter.serializeAllExcept(CREDENTIAL_TYPE, "clientId", API_KEY));
                final String credentialId = ((OAuth) cred).getClientId();
                Optional<ClientAppCredential> opt = searchForExistingCredential(actualApp, credentialId);
                if (opt.isPresent()) {
                    LOG.info("Found oauth credential with same ID for application {}", actualApp != null ? actualApp.getId() : null);
                    //I found a credential with same id name but different in some properties, I have to update it
                    endpoint.append("/" + credentialId);
                    update = true;
                    cred.setId(credentialId);
                    cred.setApplicationId(actualApp.getId());
                    cred.setCreatedBy(opt.get().getCreatedBy());
                    cred.setCreatedOn(opt.get().getCreatedOn());
                }
            } else if (cred instanceof ExtClients) {
                final String credentialId = ((ExtClients) cred).getClientId();
                endpoint.append("extclients");
                filter = new SimpleFilterProvider().setDefaultFilter(
                    SimpleBeanPropertyFilter.serializeAllExcept(CREDENTIAL_TYPE, API_KEY, "applicationId"));
                Optional<ClientAppCredential> opt = searchForExistingCredential(actualApp, credentialId);
                if (opt.isPresent()) {
                    LOG.info("Found extclients credential with same ID");
                    //I found a credential with same id name but different in some properties, I have to update it
                    endpoint.append("/" + cred.getId());
                    update = true;
                    cred.setId(credentialId);
                    cred.setCreatedBy(opt.get().getCreatedBy());
                    cred.setCreatedOn(opt.get().getCreatedOn());
                }
            } else if (cred instanceof APIKey) {
                final String credentialId = ((APIKey) cred).getApiKey();
                endpoint.append("apikeys");
                filter = new SimpleFilterProvider().setDefaultFilter(
                    SimpleBeanPropertyFilter.serializeAllExcept(CREDENTIAL_TYPE, "clientId", API_KEY));
                Optional<ClientAppCredential> opt = searchForExistingCredential(actualApp, credentialId);
                if (opt.isPresent()) {
                    LOG.info("Found apikey credential with same ID");
                    //I found a credential with same id name but different in some properties, I have to update it
                    endpoint.append( "/" + ((APIKey) cred).getApiKey());
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
                URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS + "/" + app.getId() + "/" + endpoint).build();
                mapper.setFilterProvider(filter);
                mapper.setSerializationInclusion(Include.NON_NULL);
                String json = mapper.writeValueAsString(cred);
                HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                RestAPICall request = (update ? new PUTRequest(entity, uri) : new POSTRequest(entity, uri));
                try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode < 200 || statusCode > 299) {
                        LOG.error("Error saving/updating application credentials. Response-Code: {}", statusCode);
                        Utils.logPayload(LOG, httpResponse);
                        throw new AppException(ERROR_CREATING_APPLICATION_RESPONSE_CODE + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                    }
                }
            } catch (Exception e) {
                throw new AppException(ERROR_CREATING_APPLICATION_ERROR + e.getMessage(), ErrorCode.CANT_CREATE_API_PROXY, e);
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

    public RestAPICall createUpsertUri(HttpEntity entity, URI uri, ClientApplication actualApp) {
        RestAPICall request;
        if (actualApp == null) {
            request = new POSTRequest(entity, uri);
        } else {
            if (actualApp.getAppQuota() == null) { // fix #371
                request = new POSTRequest(entity, uri);
            } else {
                request = new PUTRequest(entity, uri);
            }
        }
        return request;
    }

    public void saveQuota(ClientApplication app, ClientApplication actualApp) throws AppException {
        if (app != null && actualApp != null && app.getAppQuota() != null && actualApp.getAppQuota() != null && app.getAppQuota().equals(actualApp.getAppQuota()))
            return;
        try {
            if (app != null) {
                URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS + "/" + app.getId() + "/quota").build();
                if (app.getAppQuota() != null && app.getAppQuota().getRestrictions().isEmpty()) {
                    // If source is empty and target has values, remove target to match source
                    deleteApplicationQuota(uri);
                } else if (app.getAppQuota() != null) {
                    // source and target has different values delete target and add it.
                    FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept("apiId", "apiName", "apiVersion", "apiPath", "vhost", "queryVersion"));
                    mapper.setFilterProvider(filter);
                    mapper.setSerializationInclusion(Include.NON_NULL);
                    String json = mapper.writeValueAsString(app.getAppQuota());
                    HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                    // Use an admin account for this request
                    RestAPICall request = createUpsertUri(entity, uri, actualApp);
                    try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                        int statusCode = httpResponse.getStatusLine().getStatusCode();
                        if (statusCode < 200 || statusCode > 299) {
                            LOG.error("Error creating/updating application quota. Response-Code: {}", statusCode);
                            Utils.logPayload(LOG, httpResponse);
                            throw new AppException(ERROR_CREATING_APPLICATION_RESPONSE_CODE + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                        }
                        // Force reload of this quota next time
                        applicationsQuotaCache.remove(app.getId());
                    }
                }
            }
        } catch (Exception e) {
            throw new AppException("Error creating application quota. Error: " + e.getMessage(), ErrorCode.CANT_CREATE_API_PROXY, e);
        }
    }

    private void saveAPIAccess(ClientApplication app, ClientApplication actualApp) throws AppException {
        if (app.getApiAccess() == null || app.getApiAccess().isEmpty()) return;
        if (actualApp != null && app.getApiAccess().equals(actualApp.getApiAccess())) return;
        if (!APIManagerAdapter.hasAdminAccount()) {
            LOG.warn("Ignoring API-Access, as no admin account is given");
            return;
        }
        APIManagerAPIAccessAdapter accessAdapter = APIManagerAdapter.getInstance().accessAdapter;
        accessAdapter.saveAPIAccess(app.getApiAccess(), app, Type.applications);
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

    public HttpEntity createHttpEntity(FilterProvider filter, Object object) throws JsonProcessingException {
        mapper.setFilterProvider(filter);
        mapper.setSerializationInclusion(Include.NON_NULL);
        String json = mapper.writeValueAsString(object);
        return new StringEntity(json, ContentType.APPLICATION_JSON);
    }

    private void saveOrUpdateOAuthResources(ClientApplication desiredApp, List<ClientAppOauthResource> scopes2Create, boolean update) throws AppException {
        if (scopes2Create == null || scopes2Create.isEmpty()) return;
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
                HttpEntity entity = createHttpEntity(filter, res);
                URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS + "/" + desiredApp.getId() + "/" + endpoint).build();
                RestAPICall request = (update ? new PUTRequest(entity, uri) : new POSTRequest(entity, uri));
                try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode < 200 || statusCode > 299) {
                        LOG.error("Error saving/updating application oauth resource. Response-Code: {}", statusCode);
                        Utils.logPayload(LOG, httpResponse);
                        throw new AppException(ERROR_CREATING_APPLICATION_RESPONSE_CODE + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                    }
                }
            } catch (Exception e) {
                throw new AppException(ERROR_CREATING_APPLICATION_ERROR + e.getMessage(), ErrorCode.CANT_CREATE_API_PROXY, e);
            }
        }
    }

    private void deleteOAuthResources(ClientApplication desiredApp, List<ClientAppOauthResource> scopes2Delete) throws AppException {
        if (scopes2Delete == null || scopes2Delete.isEmpty()) return;
        for (ClientAppOauthResource res : scopes2Delete) {
            try {
                URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS + "/" + desiredApp.getId() + "/oauthresource/" + res.getId()).build();
                RestAPICall request = new DELRequest(uri);
                try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode != 204) {
                        LOG.error("Error deleting application scope. Response-Code: {}", statusCode);
                        Utils.logPayload(LOG, httpResponse);
                        throw new AppException("Error deleting application scope. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                    }
                    LOG.info("Application scope: {} for application: {} successfully deleted", res.getScope(), desiredApp.getName());
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
            if (desiredScopes != null) {
                for (ClientAppOauthResource desiredScope : desiredScopes) {
                    if (existingScope.getScope().equals(desiredScope.getScope())) {
                        actualScopeFound = true;
                        break; // As the actual scope is still desired
                    }
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
            if (desiredPermissions != null) {
                for (ApplicationPermission desiredPermission : desiredPermissions) {
                    if (existingPermission.getUsername().equals(desiredPermission.getUsername())) {
                        actualPermissionFound = true;
                        break; // As the actual scope is still desired
                    }
                }
            }
            if (!actualPermissionFound) appPermissions2Delete.add(existingPermission);
        }
    }

    private void saveOrUpdateApplicationPermissions(ClientApplication desiredApp, List<ApplicationPermission> permissions2Create, boolean update) throws AppException {
        if (permissions2Create == null || permissions2Create.isEmpty()) return;
        for (ApplicationPermission appPerm : permissions2Create) {
            String endpoint = PERMISSIONS;
            try {
                if (update) {
                    endpoint += "/" + appPerm.getId();
                    LOG.debug("Application permissions resource already exists, updating it.");
                } else {
                    LOG.debug("Application permission not found, creating it.");
                }
                FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
                    SimpleBeanPropertyFilter.serializeAllExcept("user"));
                HttpEntity entity = createHttpEntity(filter, appPerm);
                URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + APPLICATIONS + "/" + desiredApp.getId() + "/" + endpoint).build();
                RestAPICall request = (update ? new PUTRequest(entity, uri) : new POSTRequest(entity, uri));
                try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode < 200 || statusCode > 299) {
                        LOG.error("Error saving/updating application permission. Response-Code: {}", statusCode);
                        Utils.logPayload(LOG, httpResponse);
                        throw new AppException("Error saving/updating application permission' Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                    }
                }
            } catch (Exception e) {
                throw new AppException(ERROR_CREATING_APPLICATION_ERROR + e.getMessage(), ErrorCode.CANT_CREATE_API_PROXY, e);
            }
        }
    }

    private void deleteApplicationPermissions(ClientApplication desiredApp, List<ApplicationPermission> permissions2Delete) throws AppException {
        if (permissions2Delete == null || permissions2Delete.isEmpty()) return;
        for (ApplicationPermission appPerm : permissions2Delete) {
            try {
                URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + desiredApp.getId() + "/permissions/" + appPerm.getId()).build();
                RestAPICall request = new DELRequest(uri);
                try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode != 204) {
                        LOG.error("Error deleting application permission. Response-Code: {}", statusCode);
                        Utils.logPayload(LOG, httpResponse);
                        throw new AppException("Error deleting application permission. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                    }
                    LOG.info("Application permission for user: {} for application: {} successfully deleted", appPerm.getUsername(), desiredApp.getName());
                }
            } catch (Exception e) {
                throw new AppException("Error deleting application permission. Error: " + e.getMessage(), ErrorCode.API_MANAGER_COMMUNICATION, e);
            }
        }
    }

    public void deleteApplicationQuota(URI uri) throws AppException {
        try {
            RestAPICall request = new DELRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != 204) {
                    LOG.error("Error deleting quota. Response-Code: {}", statusCode);
                    throw new AppException("Error deleting quota. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                }
            }
        } catch (Exception e) {
            throw new AppException("Error deleting application scope. Error: " + e.getMessage(), ErrorCode.API_MANAGER_COMMUNICATION, e);
        }
    }

    public void deleteApplication(ClientApplication app) throws AppException {
        try {
            URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/applications/" + app.getId()).build();
            RestAPICall request = new DELRequest(uri);
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) request.execute()) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != 204) {
                    LOG.error("Error deleting application. Response-Code: {}", statusCode);
                    Utils.logPayload(LOG, httpResponse);
                    throw new AppException("Error deleting application. Response-Code: " + statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
                }
                LOG.info("Application {} with id {} is successfully deleted", app.getName(), app.getId());
            }
        } catch (Exception e) {
            throw new AppException("Error deleting application", ErrorCode.ACCESS_ORGANIZATION_ERR, e);
        }
    }
}
