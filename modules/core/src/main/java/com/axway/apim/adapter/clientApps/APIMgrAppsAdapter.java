package com.axway.apim.adapter.clientApps;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerQuotaAdapter;
import com.axway.apim.adapter.apis.jackson.JSONViews;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.apps.APIKey;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.api.model.apps.ExtClients;
import com.axway.apim.api.model.apps.OAuth;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
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
	
	APIManagerQuotaAdapter quotaAdapter = new APIManagerQuotaAdapter();

	public APIMgrAppsAdapter() {}
	
	@Override
	public boolean readConfig(Object config) throws AppException {
		if(config instanceof APIManagerAdapter && CommandParameters.getInstance()!=null) return true;
		return false;
	}

	/**
	 * Returns a list of applications.
	 * @throws AppException if applications cannot be retrieved
	 */
	private void readApplicationsFromAPIManager(ClientAppFilter appFilter) throws AppException {
		if(this.apiManagerResponse !=null && this.apiManagerResponse.get(appFilter)!=null) return;
		try {
			URI uri = getApplicationsUri(appFilter);
			LOG.info("Sending request to find existing applications: " + uri);
			RestAPICall getRequest = new GETRequest(uri, null, APIManagerAdapter.hasAdminAccount());
			
			HttpResponse response = getRequest.execute();
			this.apiManagerResponse.put(appFilter,EntityUtils.toString(response.getEntity(), "UTF-8"));
			//this.apps = mapper.readValue(response, new TypeReference<List<ClientApplication>>(){});
			
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	@Override
	public List<ClientApplication> getApplications(ClientAppFilter filter) throws AppException {
		readApplicationsFromAPIManager(filter);
		List<ClientApplication> apps;
		try {
			apps = mapper.readValue(this.apiManagerResponse.get(filter), new TypeReference<List<ClientApplication>>(){});
			if(filter.isIncludeImage()) {
				addImage(apps);
			}
			if(filter.isIncludeQuota()) {
				for(ClientApplication app : apps) {
					quotaAdapter.getQuotaForAPI(app.getId(), null);
				}
			}
			if(filter.isIncludeCredentials()) {
				addApplicationCredentials(apps);
			}
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}

		return apps;
	}
	
	@Override
	public List<ClientApplication> getAllApplications() throws AppException {
		return getApplications(new ClientAppFilter.Builder().build());
	}
	
	public List<ClientApplication> getAppsSubscribedWithAPI(String apiId) throws AppException {
		readAppsSubscribedFromAPIManager(apiId);
		List<ClientApplication> subscribedApps;
		try {
			subscribedApps = mapper.readValue(this.subscribedAppAPIManagerResponse.get(apiId), new TypeReference<List<ClientApplication>>(){});
		} catch (IOException e) {
			LOG.error("Error cant load subscribes applications from API-Manager. Can't parse response: " + this.subscribedAppAPIManagerResponse.get(apiId));
			throw new AppException("Error cant load subscribes applications from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		return subscribedApps;
	}
	
	private void readAppsSubscribedFromAPIManager(String apiId) throws AppException {
		if(this.subscribedAppAPIManagerResponse.get(apiId) !=null) return;
		
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
			
			
		} catch (Exception e) {
			LOG.error("Error cant load subscribes applications from API-Manager. Can't parse response: " + response);
			throw new AppException("Error cant load subscribes applications from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	
	
	public ClientApplication getApplication(ClientAppFilter filter) throws AppException {
		List<ClientApplication> apps = getApplications(filter);
		return uniqueApplication(apps);
	}
	
	private ClientApplication uniqueApplication(List<ClientApplication> apps) throws AppException {
		if(apps.size()>1) {
			throw new AppException("No unique application found", ErrorCode.UNKNOWN_API);
		}
		if(apps.size()==0) return null;
		return apps.get(0);
	}
	
	URI getApplicationsUri(ClientAppFilter appFilter) throws URISyntaxException {
		URI uri;
		List<NameValuePair> usedFilters = new ArrayList<>();
		String searchForAppId = "";
		if(appFilter!=null) {
			if(appFilter != null && appFilter.getFilters().size()!=0) { usedFilters.addAll(appFilter.getFilters()); }
			
			if(appFilter.getApplicationId()!=null) {
				searchForAppId = "/"+appFilter.getApplicationId();
			}
		}
		uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications"+searchForAppId)
				.addParameters(usedFilters)
				.build();
		return uri;
	}
	
	void addApplicationCredentials(List<ClientApplication> apps) throws Exception {
		URI uri;
		List<ClientAppCredential> credentials;
		String[] types = new String[] {"extclients", "oauth", "apikeys"};
		TypeReference[] classTypes = new TypeReference[] {new TypeReference<List<ExtClients>>(){}, new TypeReference<List<OAuth>>(){}, new TypeReference<List<APIKey>>(){}};
		for(ClientApplication app : apps) {
			for(int i=0; i<types.length; i++) {
				String type = types[i];
				TypeReference classType = classTypes[i];
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications/"+app.getId()+"/"+type)
						.build();
				RestAPICall getRequest = new GETRequest(uri, null);
				InputStream response = getRequest.execute().getEntity().getContent();
				credentials = mapper.readValue(response, classType);
				app.getCredentials().addAll(credentials);
			}
		}
	}
	
	void addImage(List<ClientApplication> apps) throws Exception {
		URI uri;
		for(ClientApplication app : apps) {
			if(app.getImageUrl()==null) continue;
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications/"+app.getId()+"/image")
					.build();
			Image image = APIManagerAdapter.getImageFromAPIM(uri, "app-image");
			app.setImage(image);
		}
	}
	
	public ClientApplication createApplication(ClientApplication app) throws AppException {
		getApplication(new ClientAppFilter.Builder().hasName(app.getName()).build());
		HttpResponse httpResponse = null;
		ClientApplication createdApp;
		try {
			CommandParameters cmd = CommandParameters.getInstance();
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/applications/").build();
			mapper.setSerializationInclusion(Include.NON_NULL);
			mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
			String json = mapper.writerWithView(JSONViews.ApplicationBase.class).writeValueAsString(app);
			HttpEntity entity = new StringEntity(json);
			try {
				POSTRequest postRequest = new POSTRequest(entity, uri, null);
				postRequest.setContentType("application/json");
				httpResponse = postRequest.execute();
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if( statusCode != 201){
					LOG.error("Error creating application' Response-Code: "+statusCode+"");
					throw new AppException("Error creating application' Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
				}
				createdApp = mapper.readValue(httpResponse.getEntity().getContent(), ClientApplication.class);
			} catch (Exception e) {
				throw new AppException("Error uploading application image", ErrorCode.CANT_CREATE_API_PROXY, e);
			} finally {
				try {
					((CloseableHttpResponse)httpResponse).close();
				} catch (Exception ignore) { }
			}
			
			app.setId(createdApp.getId());
			saveImage(app);
			saveCredentials(app);
			saveQuota(app);
			return createdApp;

		} catch (Exception e) {
			throw new AppException("Error creating application", ErrorCode.CANT_CREATE_API_PROXY, e);
		}
	}
	
	private void saveImage(ClientApplication app) throws URISyntaxException, AppException {
		if(app.getImage()==null) return;
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
			if( statusCode != 200){
				LOG.error("Error uploading application image' Response-Code: "+statusCode+"");
			}
		} catch (Exception e) {
			throw new AppException("Error uploading application image", ErrorCode.CANT_CREATE_API_PROXY, e);
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
	}
	
	private void saveCredentials(ClientApplication app) throws AppException, URISyntaxException, JsonProcessingException, UnsupportedEncodingException {
		if(app.getCredentials()==null || app.getCredentials().size()==0) return;
		String endpoint = "";
		HttpResponse httpResponse = null;
		for(ClientAppCredential cred : app.getCredentials()) {
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
				String json = mapper.writerWithView(JSONViews.CredentialsBase.class).writeValueAsString(cred);
				HttpEntity entity = new StringEntity(json);
				
				POSTRequest postRequest = new POSTRequest(entity, uri, null);
				postRequest.setContentType("application/json");
				httpResponse = postRequest.execute();
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if( statusCode != 201){
					LOG.error("Error creating application' Response-Code: "+statusCode+"");
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
	
	private void saveQuota(ClientApplication app) throws AppException {
		if(app.getAppQuota()==null || app.getAppQuota().getRestrictions().size()==0) return;
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
			POSTRequest postRequest = new POSTRequest(entity, uri, null, true);
			postRequest.setContentType("application/json");
			httpResponse = postRequest.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if( statusCode != 201){
				LOG.error("Error creating application' Response-Code: "+statusCode+"");
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
	
	public void setTestApiManagerResponse(ClientAppFilter filter, String apiManagerResponse) {
		this.apiManagerResponse.put(filter, apiManagerResponse);
	}

	public void setTestSubscribedAppAPIManagerResponse(String apiId, String subscribedAppAPIManagerResponse) {
		this.subscribedAppAPIManagerResponse.put(apiId, subscribedAppAPIManagerResponse);
	}
}
