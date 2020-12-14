package com.axway.apim.adapter.clientApps;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIManagerAdapter.CacheType;
import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter.Type;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.apps.APIKey;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.ClientAppOauthResource;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.api.model.apps.ExtClients;
import com.axway.apim.api.model.apps.OAuth;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.CustomPropertiesFilter;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.PUTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class APIMgrAppsAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIMgrAppsAdapter.class);

	Map<ClientAppFilter, String> apiManagerResponse = new HashMap<ClientAppFilter, String>();
	
	Map<String, String> subscribedAppAPIManagerResponse = new HashMap<String, String>();
	
	CoreParameters cmd  = CoreParameters.getInstance();
	
	ObjectMapper mapper = APIManagerAdapter.mapper;
	
	Cache<String, String> applicationsCache;
	Cache<String, String> applicationsSubscriptionCache;
	Cache<String, String> applicationsCredentialCache;

	public APIMgrAppsAdapter() throws AppException {
		applicationsCache = APIManagerAdapter.getCache(CacheType.applicationsCache, String.class, String.class);
		applicationsSubscriptionCache = APIManagerAdapter.getCache(CacheType.applicationsSubscriptionCache, String.class, String.class);
		applicationsCredentialCache = APIManagerAdapter.getCache(CacheType.applicationsCredentialCache, String.class, String.class);
	}

	/**
	 * Returns a list of applications.
	 * @throws AppException if applications cannot be retrieved
	 */
	private void readApplicationsFromAPIManager(ClientAppFilter filter) throws AppException {
		if(this.apiManagerResponse !=null && this.apiManagerResponse.get(filter)!=null) return;
		HttpResponse httpResponse = null;
		try {
			String requestedId = "";
			if(filter.getApplicationId()!=null) {
				if(applicationsCache.containsKey(filter.getApplicationId())) {
					this.apiManagerResponse.put(filter, applicationsCache.get(filter.getApplicationId()));
					return;
				}
				requestedId = "/"+filter.getApplicationId();
			}
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications" + requestedId)
					.addParameters(filter.getFilters())
					.build();
			LOG.debug("Sending request to find existing applications: " + uri);
			RestAPICall getRequest = new GETRequest(uri, APIManagerAdapter.hasAdminAccount());
			httpResponse = getRequest.execute();
			String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			if(response.startsWith("{")) { // Got a single response!
				response = "["+response+"]";
			}
			this.apiManagerResponse.put(filter,response);
			if(filter.getApplicationId()!=null) {
				applicationsCache.put(filter.getApplicationId(), response);
			}
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	URI getApplicationsUri(ClientAppFilter filter) throws URISyntaxException {		
		String requestedId = "";
		if(filter==null) filter = new ClientAppFilter.Builder().build();
		if(filter.getApplicationId()!=null) {
			requestedId = "/"+filter.getApplicationId();
		}
		URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications" + requestedId)
				.addParameters(filter.getFilters())
				.build();
		return uri;
	}
	
	public List<ClientApplication> getApplications(ClientAppFilter filter, boolean logProgress) throws AppException {
		readApplicationsFromAPIManager(filter);
		List<ClientApplication> apps = null;
		try {
			apps = mapper.readValue(this.apiManagerResponse.get(filter), new TypeReference<List<ClientApplication>>(){});
			LOG.debug("Found: "+apps.size() + " applications");
			for(int i=0; i<apps.size();i++) {
				ClientApplication app = apps.get(i);
				addImage(app, filter.isIncludeImage());
				if(filter.isIncludeQuota()) {
					app.setAppQuota(APIManagerAdapter.getInstance().quotaAdapter.getQuotaForAPI(app.getId(), null));
				}
				addApplicationCredentials(app, filter.isIncludeCredentials());
				addOauthResources(app,filter.isIncludeOauthResources());
				addAPIAccess(app, filter.isIncludeAPIAccess());
				if(logProgress && apps.size()>5) Utils.progressPercentage(i, apps.size(), "Loading details of "+apps.size()+" applications");
			}
			apps.removeIf(app -> filter.filter(app));
			Utils.addCustomPropertiesForEntity(apps, this.apiManagerResponse.get(filter), (CustomPropertiesFilter)filter);
			if(logProgress && apps.size()>5) System.out.print("\n");
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
			subscribedApps = mapper.readValue(this.subscribedAppAPIManagerResponse.get(apiId), new TypeReference<List<ClientApplication>>(){});
		} catch (IOException e) {
			LOG.error("Error cant load subscribes applications from API-Manager. Can't parse response: " + this.subscribedAppAPIManagerResponse.get(apiId), e);
			throw new AppException("Error cant load subscribes applications from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		return subscribedApps;
	}
	
	private void readAppsSubscribedFromAPIManager(String apiId) throws AppException {
		if(this.subscribedAppAPIManagerResponse.get(apiId) !=null) return;
		if(applicationsSubscriptionCache.containsKey(apiId)) {
			subscribedAppAPIManagerResponse.put(apiId, applicationsSubscriptionCache.get(apiId));
			return;
		}
		
		String response = null;
		URI uri;
		HttpResponse httpResponse = null;
		if(!APIManagerAdapter.hasAPIManagerVersion("7.7")) {
			throw new AppException("API-Manager: " + APIManagerAdapter.apiManagerVersion + " doesn't support /proxies/<apiId>/applications", ErrorCode.UNXPECTED_ERROR);
		}
		try {
			uri = new URIBuilder(CoreParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/proxies/"+apiId+"/applications").build();
			RestAPICall getRequest = new GETRequest(uri, APIManagerAdapter.hasAdminAccount());
			httpResponse = getRequest.execute();
			response = EntityUtils.toString(httpResponse.getEntity());
			subscribedAppAPIManagerResponse.put(apiId, response);
			applicationsSubscriptionCache.put(apiId, response);
		} catch (Exception e) {
			LOG.error("Error cant load subscribes applications from API-Manager. Can't parse response: " + response, e);
			throw new AppException("Error cant load subscribes applications from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	
	
	public ClientApplication getApplication(ClientAppFilter filter) throws AppException {
		List<ClientApplication> apps = getApplications(filter, false);
		return uniqueApplication(apps);
	}
	
	private ClientApplication uniqueApplication(List<ClientApplication> apps) throws AppException {
		if(apps.size()>1) {
			ErrorState.getInstance().setError("No unique application found", ErrorCode.APP_NAME_IS_NOT_UNIQUE, false);
			throw new AppException("No unique application found", ErrorCode.APP_NAME_IS_NOT_UNIQUE);
		}
		if(apps.size()==0) return null;
		return apps.get(0);
	}
	
	void addApplicationCredentials(ClientApplication app, boolean addCredentials) throws Exception {
		if(!addCredentials) return;
		URI uri;
		HttpResponse httpResponse = null;
		String response;
		List<ClientAppCredential> credentials;
		String[] types = new String[] {"extclients", "oauth", "apikeys"};
		TypeReference[] classTypes = new TypeReference[] {new TypeReference<List<ExtClients>>(){}, new TypeReference<List<OAuth>>(){}, new TypeReference<List<APIKey>>(){}};
		for(int i=0; i<types.length; i++) {
			try {
				String type = types[i];
				TypeReference<List<ClientAppCredential>> classType = classTypes[i];
				if(!applicationsCredentialCache.containsKey(app.getId()+"|"+type)) {
					uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications/"+app.getId()+"/"+type)
							.build();
					RestAPICall getRequest = new GETRequest(uri);
					httpResponse = getRequest.execute();
					response = EntityUtils.toString(httpResponse.getEntity());
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					if(statusCode != 200){
						LOG.error("Error reading application credentials. Response-Code: "+statusCode+". Got response: '"+response+"'");
						throw new AppException("Error creating application' Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
					}
					applicationsCredentialCache.put(app.getId()+"|"+type, response);
				} else {
					response = applicationsCredentialCache.get(app.getId()+"|"+type);
				}
				credentials = mapper.readValue(response, classType);
				app.getCredentials().addAll(credentials);
			} catch (Exception e) {
				throw new AppException("Error reading application credentials.", ErrorCode.CANT_CREATE_API_PROXY, e);
			} finally {
				try {
					((CloseableHttpResponse)httpResponse).close();
				} catch (Exception ignore) { }
			}
		}
	}
	
	private void addOauthResources(ClientApplication app, boolean includeOauthResources) throws AppException {
		if(!includeOauthResources) return;
		URI uri;
		HttpResponse httpResponse = null;
		String response;
		List<ClientAppOauthResource> oauthResources;
		String endpoint = "oauthresource";

			try {
					uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications/"+app.getId()+"/"+endpoint)
							.build();
					RestAPICall getRequest = new GETRequest(uri);
					httpResponse = getRequest.execute();
					response = EntityUtils.toString(httpResponse.getEntity());
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					if(statusCode != 200){
						LOG.error("Error reading application oauth resources. Response-Code: "+statusCode+". Got response: '"+response+"'");
						throw new AppException("Error creating application' Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
					}
					TypeReference<List<ClientAppOauthResource>> classType = new TypeReference<List<ClientAppOauthResource>>() {};
					oauthResources = mapper.readValue(response, classType);
					app.getOauthResources().addAll(oauthResources);
			} catch (Exception e) {
				throw new AppException("Error reading application oauth resources.", ErrorCode.CANT_CREATE_API_PROXY, e);
			} finally {
				try {
					((CloseableHttpResponse)httpResponse).close();
				} catch (Exception ignore) { }
			}
		
	}
	
	void addAPIAccess(ClientApplication app, boolean addAPIAccess) throws Exception {
		if(!addAPIAccess) return;
		try {
			List<APIAccess> apiAccess = APIManagerAdapter.getInstance().accessAdapter.getAPIAccess(app, Type.applications, true);
			app.getApiAccess().addAll(apiAccess);
		} catch (Exception e) {
			throw new AppException("Error reading application API Access.", ErrorCode.CANT_CREATE_API_PROXY, e);
		}
	}
	
	void addImage(ClientApplication app, boolean addImage) throws Exception {
		if(!addImage) return;
		URI uri;
		if(app.getImageUrl()==null) return;
		uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications/"+app.getId()+"/image")
				.build();
		Image image = APIManagerAdapter.getImageFromAPIM(uri, "app-image");
		app.setImage(image);
	}
	
	void addQuota(ClientApplication app, boolean addQuota) {
		
	}
	
	public ClientApplication updateApplication(ClientApplication desiredApp, ClientApplication actualApp) throws AppException {
		return createOrUpdateApplication(desiredApp, actualApp);
	}
	
	public ClientApplication createApplication(ClientApplication desiredApp) throws AppException {
		return createOrUpdateApplication(desiredApp, null);
	}
	
	public ClientApplication createOrUpdateApplication(ClientApplication desiredApp, ClientApplication actualApp) throws AppException {
		HttpResponse httpResponse = null;
		ClientApplication createdApp;
		try {
			CoreParameters cmd = CoreParameters.getInstance();
			URI uri;
			if(actualApp==null) {
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications").build();
			} else {
				if(desiredApp.getApiAccess()!=null && desiredApp.getApiAccess().size()==0) desiredApp.setApiAccess(null);
				desiredApp.setId(actualApp.getId());
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/"+actualApp.getId()).build();
			}
			mapper.setSerializationInclusion(Include.NON_NULL);
			try {
				RestAPICall request;
				if(actualApp==null) {
					FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
							SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"credentials", "appQuota", "organization", "image","oauthResources"}));
					mapper.setFilterProvider(filter);
					String json = mapper.writeValueAsString(desiredApp);
					HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
					request = new POSTRequest(entity, uri);
				} else {
					FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
							SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"credentials", "appQuota", "organization", "image", "apis","oauthResources"}));
					mapper.setFilterProvider(filter);
					String json = mapper.writeValueAsString(desiredApp);
					HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
					request = new PUTRequest(entity, uri);
				}
				request.setContentType("application/json");
				httpResponse = request.execute();
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if(statusCode < 200 || statusCode > 299){
					LOG.error("Error creating/updating application. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
					throw new AppException("Error creating/updating application. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
				}
				createdApp = mapper.readValue(httpResponse.getEntity().getContent(), ClientApplication.class);
				// enabled=false for a new application is ignore during initial creation, hence an update is required
				if(actualApp==null && !desiredApp.isEnabled()) {
					createOrUpdateApplication(desiredApp, createdApp);
				}
			} catch (Exception e) {
				throw new AppException("Error creating/updating application.", ErrorCode.CANT_CREATE_API_PROXY, e);
			} finally {
				try {
					((CloseableHttpResponse)httpResponse).close();
				} catch (Exception ignore) { }
			}
			
			desiredApp.setId(createdApp.getId());
			saveImage(desiredApp, actualApp);
			saveAPIAccess(desiredApp, actualApp);
			saveCredentials(desiredApp, actualApp);
			saveOauthResources(desiredApp, actualApp);
			saveQuota(desiredApp, actualApp);
			return createdApp;

		} catch (Exception e) {
			throw new AppException("Error creating/updating application", ErrorCode.CANT_CREATE_API_PROXY, e);
		}
	}
	
	private void saveImage(ClientApplication app, ClientApplication actualApp) throws URISyntaxException, AppException {
		if(app.getImage()==null) return;
		if(actualApp!=null && app.getImage().equals(actualApp.getImage())) return;
		HttpResponse httpResponse = null;
		URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/"+app.getId()+"/image").build();
		HttpEntity entity = MultipartEntityBuilder.create()
			.addBinaryBody("file", app.getImage().getInputStream(), ContentType.create("image/jpeg"), app.getImage().getBaseFilename())
			.build();
		try {
			RestAPICall apiCall = new POSTRequest(entity, uri);
			apiCall.setContentType(null);
			httpResponse = apiCall.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				LOG.error("Error saving/updating application image. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
			}
		} catch (Exception e) {
			throw new AppException("Error uploading application image", ErrorCode.CANT_CREATE_API_PROXY, e);
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
	}
	
	private void saveCredentials(ClientApplication app, ClientApplication actualApp) throws AppException, URISyntaxException, JsonProcessingException, UnsupportedEncodingException {
		if(app.getCredentials()==null || app.getCredentials().size()==0) return;
		String endpoint = "";
		HttpResponse httpResponse = null;
		for(ClientAppCredential cred : app.getCredentials()) {
			
			if(actualApp!=null && actualApp.getCredentials().contains(cred)) 
				continue; //nothing to do
			
			boolean update = false;
			FilterProvider filter;
			if(cred instanceof OAuth) {
				
				endpoint = "oauth";
				filter = new SimpleFilterProvider().setDefaultFilter(
						SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"credentialType", "clientId", "apiKey"}));
				final String credentialId = ((OAuth)cred).getClientId();
				Optional<ClientAppCredential> opt = searchForExistingCredential(actualApp, credentialId);
				if (opt.isPresent()) {
					LOG.info("Found oauth credential with same ID for application {}",actualApp.getId());
					//I found a credential with same id name but different in some properties, I have to update it		
					endpoint += "/"+credentialId;
					update = true;
					cred.setId(credentialId);
					cred.setApplicationId(actualApp.getId());
				}
			} else if (cred instanceof ExtClients) {
				final String credentialId = ((ExtClients)cred).getClientId();
				endpoint = "extclients";
				filter = new SimpleFilterProvider().setDefaultFilter(
						SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"credentialType", "apiKey","applicationId"}));
				Optional<ClientAppCredential> opt = searchForExistingCredential(actualApp, credentialId);
				if (opt.isPresent()) {
					LOG.info("Found extclients credential with same ID");
					//I found a credential with same id name but different in some properties, I have to update it		
					endpoint += "/"+((ExtClients)cred).getId();
					update = true;
					cred.setId(credentialId);
				}
			} else if (cred instanceof APIKey) {
				final String credentialId = ((APIKey)cred).getApiKey();
				endpoint = "apikeys";
				filter = new SimpleFilterProvider().setDefaultFilter(
						SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"credentialType", "clientId", "apiKey"}));
				Optional<ClientAppCredential> opt = searchForExistingCredential(actualApp, credentialId);
				if (opt.isPresent()) {
					LOG.info("Found apikey credential with same ID");
					//I found a credential with same id name but different in some properties, I have to update it		
					endpoint += "/"+((APIKey)cred).getApiKey();
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
				URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/"+app.getId()+"/"+endpoint).build();
				mapper.setFilterProvider(filter);
				mapper.setSerializationInclusion(Include.NON_NULL);
				String json = mapper.writeValueAsString(cred);
				HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
				
				RestAPICall request = (update ? new PUTRequest(entity,uri) : new POSTRequest(entity, uri));
				request.setContentType("application/json");
				httpResponse = request.execute();
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if(statusCode < 200 || statusCode > 299){
					LOG.error("Error saving/updating application credentials. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
					throw new AppException("Error creating application' Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
				}
			} catch (Exception e) {
				throw new AppException("Error creating application", ErrorCode.CANT_CREATE_API_PROXY, e);
			} finally {
				try {
					((CloseableHttpResponse)httpResponse).close();
				} catch (Exception ignore) { }
			}
		}
	}

	protected Optional<ClientAppCredential> searchForExistingCredential(ClientApplication actualApp,
			final String credentialId) {
		if (actualApp!=null) {
			Optional<ClientAppCredential> opt = actualApp.getCredentials().stream().filter(o -> {
				if (o instanceof OAuth) 
					return ((OAuth)o).getClientId().equals(credentialId);
				if (o instanceof ExtClients) 
					return ((ExtClients)o).getId().equals(credentialId);
				if (o instanceof APIKey) 
					return ((APIKey)o).getId().equals(credentialId);
				return false;
			}).findFirst();
			return opt;
		} else {
			return Optional.empty();
		}
		
	}
	
	private void saveQuota(ClientApplication app, ClientApplication actualApp) throws AppException {
		if(app.getAppQuota()==null || app.getAppQuota().getRestrictions().size()==0) return;
		if(actualApp!=null && app.getAppQuota().equals(actualApp.getAppQuota())) return;
		if(!APIManagerAdapter.hasAdminAccount()) {
			LOG.warn("Ignoring quota, as no admin account is given");
			return;
		}
		HttpResponse httpResponse = null;
		try {
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/"+app.getId()+"/quota").build();
			mapper.setSerializationInclusion(Include.NON_NULL);
			String json = mapper.writeValueAsString(app.getAppQuota());
			HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
			// Use an admin account for this request
			RestAPICall request;
			if(actualApp==null) {
				request = new POSTRequest(entity, uri, true);
			} else {
				request = new PUTRequest(entity, uri, true);
			}
			request.setContentType("application/json");
			httpResponse = request.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				LOG.error("Error creating/updating application quota. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
				throw new AppException("Error creating application' Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
		} catch (Exception e) {
			throw new AppException("Error creating application quota", ErrorCode.CANT_CREATE_API_PROXY, e);
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
	}
	
	private void saveAPIAccess(ClientApplication app, ClientApplication actualApp) throws AppException {
		if(app.getApiAccess()==null || app.getApiAccess().size()==0) return;
		if(actualApp!=null && app.getApiAccess().equals(actualApp.getApiAccess())) return;
		if(!APIManagerAdapter.hasAdminAccount()) {
			LOG.warn("Ignoring API-Access, as no admin account is given");
			return;
		}
		APIManagerAPIAccessAdapter accessAdapter = APIManagerAdapter.getInstance().accessAdapter;
		accessAdapter.saveAPIAccess(app.getApiAccess(), app, Type.applications);
	}
	
	private void saveOauthResources(ClientApplication desiredApp, ClientApplication actualApp) throws AppException {
		if(desiredApp.getOauthResources()==null || desiredApp.getOauthResources().size()==0) return;

		HttpResponse httpResponse = null;
		for(ClientAppOauthResource res : desiredApp.getOauthResources()) {
			String endpoint = "oauthresource";
			if(actualApp!=null && actualApp.getOauthResources().contains(res)) //nothing to do
				continue;
			
			try {
				boolean update = false;
				if (actualApp!=null && actualApp.getOauthResources()!=null) {
					Optional<ClientAppOauthResource> opt = actualApp.getOauthResources().stream().filter(o -> o.getScope().equals(res.getScope())).findFirst();
					if (opt.isPresent()) {
					//I found an oauth resource with same scope name but different in some properties, I have to update it		
						String oauthResourceId = opt.get().getId();
						endpoint += "/"+oauthResourceId;
						res.setId(oauthResourceId);
						res.setApplicationId(opt.get().getApplicationId());
						res.setUriprefix(opt.get().getUriprefix());
						update = true;
						LOG.debug("Oauth resource already exists, updating");
					} else {
						LOG.debug("Oauth resource not found, creating");
					}
				} else {
					LOG.debug("Oauth resource not found, creating");
				}
				FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
						SimpleBeanPropertyFilter.serializeAllExcept(new String[] { "scopes","enabled" }));
				mapper.setFilterProvider(filter);
				mapper.setSerializationInclusion(Include.NON_NULL);
				String json = mapper.writeValueAsString(res);
				HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
				URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/"+desiredApp.getId()+"/"+endpoint).build();
				RestAPICall request = (update ? new PUTRequest(entity,uri) : new POSTRequest(entity, uri));
				request.setContentType("application/json");
				httpResponse = request.execute();
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if(statusCode < 200 || statusCode > 299){
					LOG.error("Error saving/updating application oauth resource. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
					throw new AppException("Error creating application' Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
				}
			} catch (Exception e) {
				throw new AppException("Error creating application", ErrorCode.CANT_CREATE_API_PROXY, e);
			} finally {
				try {
					((CloseableHttpResponse)httpResponse).close();
				} catch (Exception ignore) { }
			}
		}
	}	
	
	
	public void setTestApiManagerResponse(ClientAppFilter filter, String apiManagerResponse) {
		this.apiManagerResponse.put(filter, apiManagerResponse);
	}

	public void setTestSubscribedAppAPIManagerResponse(String apiId, String subscribedAppAPIManagerResponse) {
		this.subscribedAppAPIManagerResponse.put(apiId, subscribedAppAPIManagerResponse);
	}
}
