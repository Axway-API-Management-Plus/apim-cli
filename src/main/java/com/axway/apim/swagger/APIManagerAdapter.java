package com.axway.apim.swagger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.CreateNewAPI;
import com.axway.apim.actions.RecreateToUpdateAPI;
import com.axway.apim.actions.UpdateExistingAPI;
import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.APIManagerAPI;
import com.axway.apim.swagger.api.AbstractAPIDefinition;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
import com.axway.apim.swagger.api.properties.apiAccess.APIAccess;
import com.axway.apim.swagger.api.properties.applications.ClientApplication;
import com.axway.apim.swagger.api.properties.cacerts.CaCert;
import com.axway.apim.swagger.api.properties.organization.ApiAccess;
import com.axway.apim.swagger.api.properties.organization.Organization;
import com.axway.apim.swagger.api.properties.quota.APIQuota;
import com.axway.apim.swagger.api.properties.quota.QuotaRestriction;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The APIContract reflects the actual existing API in the API-Manager.
 * 
 *  @author cwiechmann@axway.com
 */
public class APIManagerAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerAdapter.class);
	
	private static String apiManagerVersion = null;
	
	private static List<Organization> allOrgs = null;
	private static List<ClientApplication> allApps = null;
	
	private static Map<String, ClientApplication> clientCredentialToAppMap = new HashMap<String, ClientApplication>();
	
	private static Map<String, List<ApiAccess>> orgsApiAccess = new HashMap<String, List<ApiAccess>>();
	
	private boolean enforceBreakingChange = false;
	
	public static APIQuota sytemQuotaConfig = null;
	public static APIQuota applicationQuotaConfig = null;
	
	public static String CREDENTIAL_TYPE_API_KEY 		= "apikeys";
	public static String CREDENTIAL_TYPE_EXT_CLIENTID	= "extclients";
	public static String CREDENTIAL_TYPE_OAUTH			= "oauth";
	
	public APIManagerAdapter() throws AppException {
		super();
		APIManagerAdapter.allApps = null; // Reset allApps with every run (relevant for testing, as executed in the JVM)
		loginToAPIManager();
		this.enforceBreakingChange = CommandParameters.getInstance().isEnforceBreakingChange();
	}

	/**
	 * This method is taking in the APIChangeState to decide about the strategy how to 
	 * synchronize the desired API-State into the API-Manager.
	 * @param changeState containing the desired & actual API
	 * @throws AppException is the desired state can't be replicated into the API-Manager.
	 */
	public void applyChanges(APIChangeState changeState) throws AppException {
		// No existing API found (means: No match for APIPath), creating a complete new
		if(!changeState.getActualAPI().isValid()) {
			// --> CreateNewAPI
			LOG.info("Strategy: No existing API found, creating new!");
			CreateNewAPI createAPI = new CreateNewAPI();
			createAPI.execute(changeState);
		// We do have a breaking change!
		} else {
			LOG.info("Strategy: Going to update existing API: " + changeState.getActualAPI().getName() +" (Version: "+ changeState.getActualAPI().getVersion() + ")");
			if(!changeState.hasAnyChanges()) {
				LOG.warn("BUT, no changes detected between Import- and API-Manager-API. Exiting now...");
				throw new AppException("No changes detected between Import- and API-Manager-API", ErrorCode.NO_CHANGE, false);
			}			
			if (changeState.isBreaking()) {
				LOG.info("Recognized the following changes. Breaking: " + changeState.getBreakingChanges() + 
						" plus Non-Breaking: " + changeState.getNonBreakingChanges());
				if(changeState.getActualAPI().getState().equals(IAPIDefinition.STATE_UNPUBLISHED)) {
					LOG.info("Strategy: Applying ALL changes on existing UNPUBLISHED API.");
					UpdateExistingAPI updateAPI = new UpdateExistingAPI();
					updateAPI.execute(changeState);
					return;
				} else { // Breaking-Changes for PUBLISHED APIs and Non-Breaking
					if(enforceBreakingChange) {
						if(changeState.isUpdateExistingAPI()) {
							LOG.info("Strategy: Breaking changes - Updating existing API: " + changeState.getBreakingChanges() + 
									" plus Non-Breaking: " + changeState.getNonBreakingChanges());
							UpdateExistingAPI updateAPI = new UpdateExistingAPI();
							updateAPI.execute(changeState);
							return;
						} else {
							LOG.info("Strategy: Apply breaking changes: "+changeState.getBreakingChanges()+" & and "
									+ "Non-Breaking: "+changeState.getNonBreakingChanges()+", for PUBLISHED API. Recreating it!");
							RecreateToUpdateAPI recreate = new RecreateToUpdateAPI();
							recreate.execute(changeState);
						}
					} else {
						throw new AppException("A breaking change can't be applied without enforcing it! Try option: -f true", ErrorCode.BREAKING_CHANGE_DETECTED, false);
					}
				}
			// A NON-Breaking change
			} else if(!changeState.isBreaking()) {
				if(changeState.isUpdateExistingAPI()) {
					// Contains only changes, that can be applied to the existing API (even depends on the status)
					LOG.info("Strategy: No breaking change - Updating existing API: " + changeState.getNonBreakingChanges());
					UpdateExistingAPI updateAPI = new UpdateExistingAPI();
					updateAPI.execute(changeState);
					return;
				} else {
					// We have changes requiring a new API to be imported
					LOG.info("Strategy: No breaking change - Create and Update API, delete existing: " +  changeState.getNonBreakingChanges());
					RecreateToUpdateAPI recreate = new RecreateToUpdateAPI();
					recreate.execute(changeState);
				}
			}
		}
	}
	
	private void loginToAPIManager() throws AppException {
		URI uri;
		CommandParameters cmd = CommandParameters.getInstance();
		Transaction transaction = Transaction.getInstance();
		transaction.beginTransaction();
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/login").build();
			
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
		    params.add(new BasicNameValuePair("username", cmd.getUsername()));
		    params.add(new BasicNameValuePair("password", cmd.getPassword()));
		    POSTRequest loginRequest = new POSTRequest(new UrlEncodedFormEntity(params), uri, null);
			loginRequest.setContentType(null);
		    
			loginRequest.execute();
		} catch (Exception e) {
			throw new AppException("Can't login to API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		
		
	}
	
	/**
	 * Creates the API-Manager API-Representation. Basically the "Actual" state of the API.
	 *  
	 * @param jsonConfiguration the JSON-Configuration which is returned from the API-Manager REST-API (Proxy-Endpoint)
	 * @param desiredAPI for some tasks the desiredAPI is needed (e.g. Custom-Properties)
	 * @return an APIManagerAPI instance, which is flagged either as valid, if the API was found or invalid, if not found!
	 * @throws AppException when the API-Manager API-State can't be created
	 */
	public static IAPIDefinition getAPIManagerAPI(JsonNode jsonConfiguration, IAPIDefinition desiredAPI) throws AppException {
		if(jsonConfiguration == null) {
			IAPIDefinition apiManagerAPI = new APIManagerAPI();
			apiManagerAPI.setValid(false);
			return apiManagerAPI;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		IAPIDefinition apiManagerApi;
		try {
			apiManagerApi = mapper.readValue(jsonConfiguration.toString(), APIManagerAPI.class);
			apiManagerApi.setSwaggerDefinition(new APISwaggerDefinion(getOriginalSwaggerFromAPIM(apiManagerApi.getApiId())));
			if(apiManagerApi.getImage()!=null) {
				apiManagerApi.getImage().setImageContent(getAPIImageFromAPIM(apiManagerApi.getId()));
			}
			apiManagerApi.setValid(true);
			// As the API-Manager REST doesn't provide information about Custom-Properties, we have to setup 
			// the Custom-Properties based on the Import API.
			if(desiredAPI!=null && desiredAPI.getCustomProperties() != null) {
				Map<String, String> customProperties = new LinkedHashMap<String, String>();
				Iterator<String> it = desiredAPI.getCustomProperties().keySet().iterator();
				while(it.hasNext()) {
					String customPropKey = it.next();
					JsonNode value = jsonConfiguration.get(customPropKey);
					String customPropValue = (value == null) ? null : value.asText();
					customProperties.put(customPropKey, customPropValue);
				}
				((AbstractAPIDefinition)apiManagerApi).setCustomProperties(customProperties);
			}
			addQuotaConfiguration(apiManagerApi);
			addClientOrganizations(apiManagerApi, desiredAPI);
			addClientApplications(apiManagerApi, desiredAPI);
			return apiManagerApi;
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-State.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	private static void addClientOrganizations(IAPIDefinition apiManagerApi, IAPIDefinition desiredAPI) throws AppException {
		if(desiredAPI.getState().equals(IAPIDefinition.STATE_UNPUBLISHED)) {
			LOG.info("Ignoring Client-Organizations, as desired API-State is Unpublished!");
			return;
		}
		List<String> grantedOrgs = new ArrayList<String>();
		List<Organization> allOrgs = getAllOrgs();
		for(Organization org : allOrgs) {
			List<APIAccess> orgAPIAccess = getAPIAccess(org.getId(), "organizations");
			for(APIAccess access : orgAPIAccess) {
				if(access.getApiId().equals(apiManagerApi.getId())) {
					grantedOrgs.add(org.getName());
				}
			}
		}
		apiManagerApi.setClientOrganizations(grantedOrgs);
	}
	
	private static void addClientApplications(IAPIDefinition apiManagerApi, IAPIDefinition desiredAPI) throws AppException {
		if(desiredAPI.getState().equals(IAPIDefinition.STATE_UNPUBLISHED)) {
			LOG.info("Ignoring Client-Applications, as desired API-State is Unpublished!");
			return;
		}
		List<ClientApplication> existingClientApps = new ArrayList<ClientApplication>();
		List<ClientApplication> allApps = getAllApps();
		for(ClientApplication app : allApps) {
			List<APIAccess> APIAccess = getAPIAccess(app.getId(), "applications");
			for(APIAccess access : APIAccess) {
				if(access.getApiId().equals(apiManagerApi.getId())) {
					existingClientApps.add(app);
				}
			}
		}
		apiManagerApi.setApplications(existingClientApps);
	}
	
	/**
	 * The actual Org-ID based on the OrgName. Lazy implementation.
	 * @param orgName the name of the organizations
	 * @return the id of the organization
	 * @throws AppException 
	 */
	public static String getOrgId(String orgName) throws AppException {
		if(allOrgs == null) getAllOrgs();
		for(Organization org : allOrgs) {
			if(orgName.equals(org.getName())) return org.getId();
		}
		LOG.error("Requested OrgId for unknown orgName: " + orgName);
		return null;
	}
	
	/**
	 * The actual Org-ID based on the OrgName. Lazy implementation.
	 * @param orgName the name of the organizations
	 * @return the id of the organization
	 * @throws AppException 
	 */
	public static String getOrgName(String orgId) throws AppException {
		if(allOrgs == null) getAllOrgs();
		for(Organization org : allOrgs) {
			if(orgId.equals(org.getId())) return org.getName();
		}
		LOG.error("Requested OrgName for unknown orgId: " + orgId);
		return null;
	}
	
	/**
	 * The actual App-ID based on the AppName. Lazy implementation.
	 * @param orgName the name of the organizations
	 * @return the id of the organization
	 * @throws AppException 
	 */
	public static ClientApplication getApplication(String appName) throws AppException {
		if(allApps==null) getAllApps();
		for(ClientApplication app : allApps) {
			LOG.debug("Configured app with name: '"+appName+"' found. ID: '"+app.getId()+"'");
			if(appName.equals(app.getName())) return app;
		}
		LOG.error("Requested AppId for unknown appName: " + appName);
		return null;
	}
	
	/**
	 * The actual App-ID based on the AppName. Lazy implementation.
	 * @param orgName the name of the organizations
	 * @return the id of the organization
	 */
	public static ClientApplication getAppForId(String appId) {
		for(ClientApplication app : allApps) {
			if(appId.equals(app.getId())) return app;
		}
		LOG.error("Requested Application for unknown appId: "+appId+" not found.");
		return null;
	}
	
	/**
	 * The actual App-ID based on the AppName. Lazy implementation.
	 * @param orgName the name of the organizations
	 * @return the id of the organization
	 * @throws AppException 
	 */
	public static ClientApplication getAppIdForCredential(String credential, String type) throws AppException {
		if(clientCredentialToAppMap.containsKey(type+"_"+credential)) {
			ClientApplication app = clientCredentialToAppMap.get(type+"_"+credential);
			LOG.info("Found existing application (in cache): '"+app.getName()+"' based on credential (Type: '"+type+"'): '"+credential+"'");
			return app;
		}
		getAllApps(); // Make sure, we loaded all app before!
		LOG.debug("Searching credential (Type: "+type+"): '"+credential+"' in: " + allApps.size() + " apps.");
		Collection<ClientApplication> appIds = clientCredentialToAppMap.values();
		for(ClientApplication app : allApps) {
			if(appIds.contains(app.getId())) continue;
			ObjectMapper mapper = new ObjectMapper();
			String response = null;
			URI uri;
			try {
				uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications/"+app.getId()+"/"+type+"").build();
				LOG.debug("Loading credentials of type: '" + type + "' for application: '" + app.getName() + "' from API-Manager.");
				RestAPICall getRequest = new GETRequest(uri, null);
				HttpResponse httpResponse = getRequest.execute();
				response = EntityUtils.toString(httpResponse.getEntity());
				LOG.trace("Response: " + response);
				JsonNode clientIds = mapper.readTree(response);
				if(clientIds.size()==0) {
					LOG.debug("No credentials (Type: '"+type+"') found for application: '"+app.getName()+"'");
					continue;
				}
				for(JsonNode clientId : clientIds) {
					String key;
					if(type.equals(CREDENTIAL_TYPE_API_KEY)) {
						key = clientId.get("id").asText();
					} else if(type.equals(CREDENTIAL_TYPE_EXT_CLIENTID) || type.equals(CREDENTIAL_TYPE_OAUTH)) {
						key = clientId.get("clientId").asText();
					} else {
						throw new AppException("Unknown credential type: " + type, ErrorCode.UNXPECTED_ERROR);
					}
					LOG.debug("Found credential (Type: '"+type+"'): '"+key+"' for application: '"+app.getName()+"'");
					clientCredentialToAppMap.put(type+"_"+key, app);
					if(key.equals(credential)) {
						LOG.info("Found existing application: '"+app.getName()+"' based on credential (Type: '"+type+"'): '"+credential+"'");
						return app;
					}
				}
			} catch (Exception e) {
				LOG.error("Can't load applications credentials. Can't parse response: " + response);
				throw new AppException("Can't load applications credentials.", ErrorCode.API_MANAGER_COMMUNICATION, e);
			}
		}
		LOG.error("No application found for credential ("+type+"): " + credential);
		return null;
	}
	
	
	
	private static void addQuotaConfiguration(IAPIDefinition api) throws AppException {
		//APPLICATION:	00000000-0000-0000-0000-000000000001
		//SYSTEM: 		00000000-0000-0000-0000-000000000000
		APIManagerAPI managerAPI = (APIManagerAPI)api;
		try {
			applicationQuotaConfig = getQuotaFromAPIManager("00000000-0000-0000-0000-000000000001"); // Get the Application-Default-Quota
			sytemQuotaConfig = getQuotaFromAPIManager("00000000-0000-0000-0000-000000000000"); // Get the System-Default-Quota
			managerAPI.setApplicationQuota(getAPIQuota(applicationQuotaConfig, managerAPI.getId()));
			managerAPI.setSystemQuota(getAPIQuota(sytemQuotaConfig, managerAPI.getId()));
		} catch (AppException e) {
			LOG.error("Application-Default quota response: '"+applicationQuotaConfig+"'");
			LOG.error("System-Default quota response: '"+sytemQuotaConfig+"'");
			throw e;
		}
	}
	
	private static APIQuota getQuotaFromAPIManager(String type) throws AppException {
		ObjectMapper mapper = new ObjectMapper();
		URI uri;
		
			try {
				uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/quotas/"+type).build();
				RestAPICall getRequest = new GETRequest(uri, null);
				HttpResponse response = getRequest.execute();
				int statusCode = response.getStatusLine().getStatusCode();
				if( statusCode == 403){
					throw new AppException("Can't get API-Manager Quota-Configuration, User should have API administrator role", ErrorCode.API_MANAGER_COMMUNICATION);
				}
				if( statusCode != 200){
					throw new AppException("Can't get API-Manager Quota-Configuration.", ErrorCode.API_MANAGER_COMMUNICATION);
				}
				String config = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
				APIQuota quotaConfig = mapper.readValue(config, APIQuota.class);
				return quotaConfig;
			} catch (URISyntaxException | UnsupportedOperationException | IOException e) {
				throw new AppException("Can't get API-Manager Quota-Configuration.", ErrorCode.API_MANAGER_COMMUNICATION, e);
			} 
		
	}
	
	private static APIQuota getAPIQuota(APIQuota quotaConfig, String apiId) throws AppException {
		APIQuota apiQuota;
		try {
			for(QuotaRestriction restriction : quotaConfig.getRestrictions()) {
				if(restriction.getApi().equals(apiId)) {
					apiQuota = new APIQuota();
					apiQuota.setDescription(quotaConfig.getDescription());
					apiQuota.setName(quotaConfig.getName());
					apiQuota.setRestrictions(new ArrayList<QuotaRestriction>());
					apiQuota.getRestrictions().add(restriction);
					return apiQuota;
				}
			}
		} catch (Exception e) {
			throw new AppException("Can't parse quota from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		return null;
	}
	
	/**
	 * Based on the given apiPath this method returns the JSON-Configuration for the API 
	 * as it's stored in the API-Manager. The result is basically used to create the APIManagerAPI in 
	 * method getAPIManagerAPI
	 * @param apiPath path of the API, which can be considered as the key.
	 * @return the JSON-Configuration as it's returned from the API-Manager REST-API /proxies endpoint.
	 * @throws AppException if the API can't be found or created
	 */
	public static JsonNode getExistingAPI(String apiPath) throws AppException {
		CommandParameters cmd = CommandParameters.getInstance();
		ObjectMapper mapper = new ObjectMapper();
		URI uri;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/proxies").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();
			
			JsonNode jsonResponse;
			String path;
			String apiId = null;
			try {
				jsonResponse = mapper.readTree(response);
				for(JsonNode node : jsonResponse) {
					path = node.get("path").asText();
					if(path.equals(apiPath)) {
						LOG.info("Found existing API on path: '"+path+"' / "+node.get("state").asText()+" ('" + node.get("id").asText()+"')");
						apiId = node.get("id").asText();
						break;
					}
				}
				if(apiId==null) {
					LOG.info("No existing API found exposed on: " + apiPath);
					return null;
				}
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/proxies/"+apiId).build();
				getRequest = new GETRequest(uri, null);
				response = getRequest.execute().getEntity().getContent();
				jsonResponse = mapper.readTree(response);
				return jsonResponse;
			} catch (IOException e) {
				throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
			}
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	private static byte[] getOriginalSwaggerFromAPIM(String backendApiID) throws AppException {
		URI uri;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/apirepo/"+backendApiID+"/download")
					.setParameter("original", "true").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();
			return IOUtils.toByteArray(response);
		} catch (Exception e) {
			throw new AppException("Can't read Swagger-File.", ErrorCode.CANT_READ_SWAGGER_FILE, e);
		}
	}
	
	private static byte[] getAPIImageFromAPIM(String backendApiID) throws AppException {
		URI uri;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/proxies/"+backendApiID+"/image").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			HttpEntity response = getRequest.execute().getEntity();
			if(response == null) return null; // no Image found in API-Manager
			InputStream is = response.getContent();
			return IOUtils.toByteArray(is);
		} catch (Exception e) {
			throw new AppException("Can't read Image from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	/**
	 * Lazy helper method to get the actual API-Manager version. This is used to toggle on/off some 
	 * of the features (such as API-Custom-Properties)
	 * @return the API-Manager version as returned from the API-Manager REST-API /config endpoint
	 * @throws AppException is something goes wrong.
	 */
	public static String getApiManagerVersion() throws AppException {
		if(APIManagerAdapter.apiManagerVersion!=null) {
			return apiManagerVersion;
		}
		ObjectMapper mapper = new ObjectMapper();
		String response = null;
		URI uri;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/config").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			HttpResponse httpResponse = getRequest.execute();
			response = EntityUtils.toString(httpResponse.getEntity());
			JsonNode jsonResponse;
			jsonResponse = mapper.readTree(response);
			String apiManagerVersion = jsonResponse.get("productVersion").asText();
			LOG.debug("API-Manager version is: " + apiManagerVersion);
			return jsonResponse.get("productVersion").asText();
		} catch (Exception e) {
			LOG.error("Error AppInfo from API-Manager. Can't parse response: " + response);
			throw new AppException("Can't get version from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	private static List<APIAccess> getAPIAccess(String id, String type) throws AppException {
		List<APIAccess> allApiAccess = new ArrayList<APIAccess>();
		ObjectMapper mapper = new ObjectMapper();
		String response = null;
		URI uri;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/"+type+"/"+id+"/apis").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			HttpResponse httpResponse = getRequest.execute();
			response = EntityUtils.toString(httpResponse.getEntity());
			allApiAccess = mapper.readValue(response, new TypeReference<List<APIAccess>>(){});
			return allApiAccess;
		} catch (Exception e) {
			LOG.error("Error cant API-Access for "+type+" from API-Manager. Can't parse response: " + response);
			throw new AppException("API-Access for "+type+" from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	
	
	public static List<Organization> getAllOrgs() throws AppException {
		if(APIManagerAdapter.allOrgs!=null) {
			return APIManagerAdapter.allOrgs;
		}
		allOrgs = new ArrayList<Organization>();
		ObjectMapper mapper = new ObjectMapper();
		String response = null;
		URI uri;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/organizations").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			HttpResponse httpResponse = getRequest.execute();
			response = EntityUtils.toString(httpResponse.getEntity());
			allOrgs = mapper.readValue(response, new TypeReference<List<Organization>>(){});
			return allOrgs;
		} catch (Exception e) {
			LOG.error("Error cant read all orgs from API-Manager. Can't parse response: " + response);
			throw new AppException("Can't read all orgs from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	public static List<ClientApplication> getAllApps() throws AppException {
		if(APIManagerAdapter.allApps!=null) {
			LOG.trace("Not reloading existing apps from API-Manager. Number of apps: " + APIManagerAdapter.allApps.size());
			return APIManagerAdapter.allApps;
		}
		LOG.debug("Loading existing apps from API-Manager.");
		allApps = new ArrayList<ClientApplication>();
		ObjectMapper mapper = new ObjectMapper();
		String response = null;
		URI uri;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			HttpResponse httpResponse = getRequest.execute();
			response = EntityUtils.toString(httpResponse.getEntity());
			allApps = mapper.readValue(response, new TypeReference<List<ClientApplication>>(){});
			LOG.debug("Loaded: " + allApps.size() + " apps from API-Manager.");
			return allApps;
		} catch (Exception e) {
			LOG.error("Error cant read all applications from API-Manager. Can't parse response: " + response);
			throw new AppException("Can't read all applications from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	
	
	public static List<ApiAccess> getOrgsApiAccess(String orgId, boolean forceReload) throws AppException {
		if(!forceReload && orgsApiAccess.containsKey(orgId)) {
			return orgsApiAccess.get(orgId);
		}
		ObjectMapper mapper = new ObjectMapper();
		String response = null;
		URI uri;
		List<ApiAccess> apiAccess;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/organizations/"+orgId+"/apis").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			HttpResponse httpResponse = getRequest.execute();
			response = EntityUtils.toString(httpResponse.getEntity());
			apiAccess = mapper.readValue(response, new TypeReference<List<ApiAccess>>(){});
			orgsApiAccess.put(orgId, apiAccess);
			return apiAccess;
		} catch (Exception e) {
			LOG.error("Error cant read API-Access for org: "+orgId+" from API-Manager. Can't parse response: " + response);
			throw new AppException("Error cant read API-Access for org: "+orgId+" from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	public static JsonNode getCustomPropertiesConfig() throws AppException {
		
		String appConfig = null;
		URI uri;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath("/vordel/apiportal/app/app.config").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			HttpEntity response = getRequest.execute().getEntity();
			appConfig = IOUtils.toString(response.getContent(), "UTF-8");
			return parseAppConfig(appConfig);
		} catch (Exception e) {
			throw new AppException("Can't read app.config from API-Manager: '" + appConfig + "'", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	/**
	 * Helper method to validate that configured Custom-Properties are really configured 
	 * in the API-Manager configuration.<br>
	 * Will become obsolete sine the API-Manager REST-API provides an endpoint for that.
	 * @param appConfig from the API-Manager (which isn't JSON)
	 * @return JSON-Configuration with the custom-properties section
	 * @throws AppException if the app.config can't be parsed
	 */
	public static JsonNode parseAppConfig(String appConfig) throws AppException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			appConfig = appConfig.substring(appConfig.indexOf("customPropertiesConfig:")+23, appConfig.indexOf("wizardModels"));
			//appConfig = appConfig.substring(0, appConfig.length()-1); // Remove the tail comma
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
			mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
			mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
			return mapper.readTree(appConfig);
		} catch (Exception e) {
			throw new AppException("Can't parse API-Manager app.config.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	/**
	 * Helper method to fulfill the given certificates by the API-Developer into the required 
	 * format as it's needed by the API-Manager. 
	 * @param certFile InputStream to the Certificate
	 * @param cert the certificate itself
	 * @return JsonNode as it's required by the API-Manager.
	 * @throws AppException if JSON-Node-Config can't be created
	 */
	public static JsonNode getCertInfo(InputStream certFile, CaCert cert) throws AppException {
		URI uri;
		ObjectMapper mapper = new ObjectMapper();
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/certinfo/").build();
			
			HttpEntity entity = MultipartEntityBuilder.create()
					.addBinaryBody("file", IOUtils.toByteArray(certFile), ContentType.create("application/x-x509-ca-cert"), cert.getCertFile())
					.addTextBody("inbound", cert.getInbound())
					.addTextBody("outbound", cert.getOutbound())
					.build();
			POSTRequest postRequest = new POSTRequest(entity, uri, null);
			postRequest.setContentType(null);
			HttpEntity response = postRequest.execute().getEntity();
			JsonNode jsonResponse = mapper.readTree(response.getContent());
			return jsonResponse;
		} catch (Exception e) {
			throw new AppException("Can't read certificate information from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
}
