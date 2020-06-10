package com.axway.apim.adapter.clientApps;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.axway.apim.adapter.apis.jackson.JSONViews;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.apps.APIKey;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.api.model.apps.ExtClients;
import com.axway.apim.api.model.apps.OAuth;
import com.axway.apim.lib.CommandParameters;
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
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIMgrAppsAdapter extends ClientAppAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIMgrAppsAdapter.class);

	Map<ClientAppFilter, String> apiManagerResponse = new HashMap<ClientAppFilter, String>();
	
	Map<String, String> subscribedAppAPIManagerResponse = new HashMap<String, String>();
	
	CommandParameters cmd  = CommandParameters.getInstance();
	
	ObjectMapper mapper = APIManagerAdapter.mapper;
	
	Cache<String, String> applicationsCache;
	Cache<String, String> applicationsSubscriptionCache;
	Cache<String, String> applicationsCredentialCache;

	public APIMgrAppsAdapter() throws AppException {
		applicationsCache = APIManagerAdapter.getCache(CacheType.applicationsCache, String.class, String.class);
		applicationsSubscriptionCache = APIManagerAdapter.getCache(CacheType.applicationsSubscriptionCache, String.class, String.class);
		applicationsCredentialCache = APIManagerAdapter.getCache(CacheType.applicationsCredentialCache, String.class, String.class);
	}
	
	@Override
	public boolean readConfig(Object config) throws AppException {
		if(config instanceof APIManagerAdapter && CommandParameters.getInstance()!=null) return true;
		return false;
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
			RestAPICall getRequest = new GETRequest(uri, null, APIManagerAdapter.hasAdminAccount());
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
	
	@Override
	public List<ClientApplication> getApplications(ClientAppFilter filter, boolean logProgress) throws AppException {
		readApplicationsFromAPIManager(filter);
		List<ClientApplication> apps;
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
				addAPIAccess(app, filter.isIncludeAPIAccess());
				if(logProgress && apps.size()>5) Utils.progressPercentage(i, apps.size(), "Laoding "+apps.size()+" Applications");
			}
			if(logProgress && apps.size()>5) System.out.print("\n");
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}

		return apps;
	}
	
	@Override
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
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/proxies/"+apiId+"/applications").build();
			RestAPICall getRequest = new GETRequest(uri, null, APIManagerAdapter.hasAdminAccount());
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
				TypeReference classType = classTypes[i];
				if(!applicationsCredentialCache.containsKey(app.getId()+"|"+type)) {
					uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications/"+app.getId()+"/"+type)
							.build();
					RestAPICall getRequest = new GETRequest(uri, null);
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
	
	void addAPIAccess(ClientApplication app, boolean addAPIAccess) throws Exception {
		if(!addAPIAccess) return;
		try {
			List<APIAccess> apiAccess = APIManagerAdapter.getInstance().accessAdapter.getAPIAccess(app.getId(), Type.applications, true);
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
	
	@Override
	public ClientApplication updateApplication(ClientApplication desiredApp, ClientApplication actualApp) throws AppException {
		return createOrUpdateApplication(desiredApp, actualApp);
	}
	
	@Override
	public ClientApplication createApplication(ClientApplication desiredApp) throws AppException {
		return createOrUpdateApplication(desiredApp, null);
	}
	
	public ClientApplication createOrUpdateApplication(ClientApplication desiredApp, ClientApplication actualApp) throws AppException {
		HttpResponse httpResponse = null;
		ClientApplication createdApp;
		try {
			CommandParameters cmd = CommandParameters.getInstance();
			URI uri;
			if(actualApp==null) {
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/").build();
			} else {
				if(desiredApp.getApiAccess()!=null && desiredApp.getApiAccess().size()==0) desiredApp.setApiAccess(null);
				desiredApp.setId(actualApp.getId());
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/"+actualApp.getId()).build();
			}
			mapper.setSerializationInclusion(Include.NON_NULL);
			mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
			try {
				RestAPICall request;
				if(actualApp==null) {
					String json = mapper.writerWithView(JSONViews.ApplicationForAPIManager.class).writeValueAsString(desiredApp);
					HttpEntity entity = new StringEntity(json);
					request = new POSTRequest(entity, uri, null);
				} else {
					String json = mapper.writerWithView(JSONViews.ApplicationForAPIManagerOnUpdate.class).writeValueAsString(desiredApp);
					HttpEntity entity = new StringEntity(json);
					request = new PUTRequest(entity, uri, null);
				}
				request.setContentType("application/json");
				httpResponse = request.execute();
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if(statusCode < 200 || statusCode > 299){
					LOG.error("Error creating/updating application. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
					throw new AppException("Error creating/updating application. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
				}
				createdApp = mapper.readValue(httpResponse.getEntity().getContent(), ClientApplication.class);
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
			RestAPICall apiCall = new POSTRequest(entity, uri, null);
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
			if(actualApp!=null && actualApp.getCredentials().contains(cred)) continue;
			if(cred instanceof OAuth) {
				endpoint = "oauth";
			} else if (cred instanceof ExtClients) {
				endpoint = "extclients";
			} else if (cred instanceof APIKey) {
				endpoint = "apikeys";
			} else {
				throw new AppException("Unsupported credential: " + cred.getClass().getName(), ErrorCode.UNXPECTED_ERROR);
			}
			try {
				URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/"+app.getId()+"/"+endpoint).build();
				mapper.setSerializationInclusion(Include.NON_NULL);
				mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
				String json = mapper.writerWithView(JSONViews.CredentialsForAPIManager.class).writeValueAsString(cred);
				HttpEntity entity = new StringEntity(json);
				
				POSTRequest postRequest = new POSTRequest(entity, uri, null);
				postRequest.setContentType("application/json");
				httpResponse = postRequest.execute();
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
			mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
			String json = mapper.writeValueAsString(app.getAppQuota());
			HttpEntity entity = new StringEntity(json);
			// Use an admin account for this request
			RestAPICall request;
			if(actualApp==null) {
				request = new POSTRequest(entity, uri, null, true);
			} else {
				request = new PUTRequest(entity, uri, null, true);
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
		accessAdapter.saveAPIAccess(app.getApiAccess(), app.getId(), Type.applications);
	}
	
	
	
	public void setTestApiManagerResponse(ClientAppFilter filter, String apiManagerResponse) {
		this.apiManagerResponse.put(filter, apiManagerResponse);
	}

	public void setTestSubscribedAppAPIManagerResponse(String apiId, String subscribedAppAPIManagerResponse) {
		this.subscribedAppAPIManagerResponse.put(apiId, subscribedAppAPIManagerResponse);
	}
}
