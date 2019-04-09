package com.axway.apim.swagger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
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
import com.axway.apim.actions.rest.APIMHttpClient;
import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.lib.ErrorState;
import com.axway.apim.swagger.api.properties.APIDefintion;
import com.axway.apim.swagger.api.properties.apiAccess.APIAccess;
import com.axway.apim.swagger.api.properties.applications.ClientApplication;
import com.axway.apim.swagger.api.properties.cacerts.CaCert;
import com.axway.apim.swagger.api.properties.organization.ApiAccess;
import com.axway.apim.swagger.api.properties.organization.Organization;
import com.axway.apim.swagger.api.properties.quota.APIQuota;
import com.axway.apim.swagger.api.properties.quota.QuotaRestriction;
import com.axway.apim.swagger.api.properties.user.User;
import com.axway.apim.swagger.api.state.AbstractAPI;
import com.axway.apim.swagger.api.state.ActualAPI;
import com.axway.apim.swagger.api.state.IAPI;
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
	
	private static APIManagerAdapter instance;
	
	private static String apiManagerVersion = null;
	
	private static List<Organization> allOrgs = null;
	private static List<ClientApplication> allApps = null;
	
	private static Map<String, ClientApplication> clientCredentialToAppMap = new HashMap<String, ClientApplication>();
	
	private static Map<String, List<ApiAccess>> orgsApiAccess = new HashMap<String, List<ApiAccess>>();
	
	private boolean enforceBreakingChange = false;
	
	public static APIQuota sytemQuotaConfig = null;
	public static APIQuota applicationQuotaConfig = null;
	private boolean usingOrgAdmin = false;
	private boolean hasAdminAccount = false;
	
	private ErrorState error = ErrorState.getInstance();
	
	public static String CREDENTIAL_TYPE_API_KEY 		= "apikeys";
	public static String CREDENTIAL_TYPE_EXT_CLIENTID	= "extclients";
	public static String CREDENTIAL_TYPE_OAUTH			= "oauth";
	
	public static synchronized APIManagerAdapter getInstance() throws AppException {
		if (APIManagerAdapter.instance == null) {
			APIManagerAdapter.instance = new APIManagerAdapter ();
		}
		return APIManagerAdapter.instance;
	}
	
	public static synchronized void deleteInstance() throws AppException {
			APIManagerAdapter.instance = null;
	}
	
	private APIManagerAdapter() throws AppException {
		super();
		Transaction transaction = Transaction.getInstance();
		transaction.beginTransaction();
		APIManagerAdapter.allApps = null; // Reset allApps with every run (relevant for testing, as executed in the same JVM)
		loginToAPIManager(false); // Login with the provided user (might be an Org-Admin)
		loginToAPIManager(true); // Second, login if needed with an admin account
		this.enforceBreakingChange = CommandParameters.getInstance().isEnforceBreakingChange();
	}

	/**
	 * This method is taking in the APIChangeState to decide about the strategy how to 
	 * synchronize the desired API-State into the API-Manager.
	 * @param changeState containing the desired & actual API
	 * @throws AppException is the desired state can't be replicated into the API-Manager.
	 */
	public void applyChanges(APIChangeState changeState) throws AppException {
		if(!this.hasAdminAccount && isAdminAccountNeeded(changeState) ) {
			error.setError("OrgAdmin user only allowed to change/register unpublished APIs.", ErrorCode.NO_ADMIN_ROLE_USER, false);
			throw new AppException("OrgAdmin user only allowed to change/register unpublished APIs.", ErrorCode.NO_ADMIN_ROLE_USER);
		}
		// No existing API found (means: No match for APIPath), creating a complete new
		if(!changeState.getActualAPI().isValid()) {
			// --> CreateNewAPI
			LOG.info("Strategy: No existing API found, creating new!");
			CreateNewAPI createAPI = new CreateNewAPI();
			createAPI.execute(changeState);
		// Otherwise an existing API exists
		} else {
			LOG.info("Strategy: Going to update existing API: " + changeState.getActualAPI().getName() +" (Version: "+ changeState.getActualAPI().getVersion() + ")");
			if(!changeState.hasAnyChanges()) {
				LOG.debug("BUT, no changes detected between Import- and API-Manager-API. Exiting now...");
				error.setWarning("No changes detected between Import- and API-Manager-API", ErrorCode.NO_CHANGE, false);
				throw new AppException("No changes detected between Import- and API-Manager-API", ErrorCode.NO_CHANGE);
			}
			LOG.info("Recognized the following changes. Potentially Breaking: " + changeState.getBreakingChanges() + 
					" plus Non-Breaking: " + changeState.getNonBreakingChanges());
			if (changeState.isBreaking()) { // Make sure, breaking changes aren't applied without enforcing it.
				if(!enforceBreakingChange) {
					error.setError("A potentially breaking change can't be applied without enforcing it! Try option: -f true", ErrorCode.BREAKING_CHANGE_DETECTED, false);
					throw new AppException("A potentially breaking change can't be applied without enforcing it! Try option: -f true", ErrorCode.BREAKING_CHANGE_DETECTED);
				}
			}
			
			if(changeState.isUpdateExistingAPI()) { // All changes can be applied to the existing API in current state
				LOG.info("Strategy: Update existing API, as all changes can be applied in current state.");
				UpdateExistingAPI updateAPI = new UpdateExistingAPI();
				updateAPI.execute(changeState);
				return;
			} else { // We changes, that require a re-creation of the API
				LOG.info("Strategy: Apply breaking changes: "+changeState.getBreakingChanges()+" & and "
						+ "Non-Breaking: "+changeState.getNonBreakingChanges()+", for PUBLISHED API. Recreating it!");
				RecreateToUpdateAPI recreate = new RecreateToUpdateAPI();
				recreate.execute(changeState);
				return;
			}
		}
	}
	
	private boolean isAdminAccountNeeded(APIChangeState changeState) throws AppException {
		if(changeState.getDesiredAPI().getState().equals(IAPI.STATE_UNPUBLISHED) && 
				(!changeState.getActualAPI().isValid() || changeState.getActualAPI().getState().equals(IAPI.STATE_UNPUBLISHED))) {
			return false;
		} else {
			return true;
		}		
	}
	
	public void loginToAPIManager(boolean useAdminClient) throws AppException {
		URI uri;
		CommandParameters cmd = CommandParameters.getInstance();
		if(cmd.ignoreAdminAccount() && useAdminClient) return;
		if(hasAdminAccount && useAdminClient) return; // Already logged in with an Admin-Account.
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/login").build();
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			String username;
			String password;
			if(useAdminClient) {
				String[] usernamePassword = getAdminUsernamePassword();
				if(usernamePassword==null) return;
				username = usernamePassword[0];
				password = usernamePassword[1];
				LOG.debug("Logging in with Admin-User: '" + username + "'");
			} else {
				username = cmd.getUsername();
				password = cmd.getPassword();
				LOG.debug("Logging in with User: '" + username + "'");
			}
			// This forces to create a client which is re-used based on useAdmin
			APIMHttpClient client = APIMHttpClient.getInstance(useAdminClient);
		    params.add(new BasicNameValuePair("username", username));
		    params.add(new BasicNameValuePair("password", password));
		    POSTRequest loginRequest = new POSTRequest(new UrlEncodedFormEntity(params), uri, null, useAdminClient);
			loginRequest.setContentType(null);
			HttpResponse response = loginRequest.execute();
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode == 403){
				LOG.error("Login failed: " +statusCode+ ", Response: " + response);
				throw new AppException("Given user: '"+username+"' can't login.", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			for (Header header : response.getAllHeaders()) {
				if(header.getName().equals("CSRF-Token")) {
					client.setCsrfToken(header.getValue());
					break;
				}
			}
			User user = getCurrentUser(useAdminClient);
			if(user.getRole().equals("admin")) {
				this.hasAdminAccount = true;
				// Also register this client as an Admin-Client 
				APIMHttpClient.addInstance(true, client);
			} else if (user.getRole().equals("oadmin")) {
				this.usingOrgAdmin = true;
			} else {
				error.setError("Not supported user-role: '"+user.getRole()+"'", ErrorCode.API_MANAGER_COMMUNICATION, false);
				throw new AppException("Not supported user-role: "+user.getRole()+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
		} catch (Exception e) {
			throw new AppException("Can't login to API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	private String[] getAdminUsernamePassword() throws AppException {
		if(CommandParameters.getInstance().getAdminUsername()==null) return null;
		String[] usernamePassword =  {CommandParameters.getInstance().getAdminUsername(), CommandParameters.getInstance().getAdminPassword()};
		return usernamePassword;
	}
	
	public static User getCurrentUser(boolean useAdminClient) throws AppException {
		ObjectMapper mapper = new ObjectMapper();
		URI uri;
		HttpResponse response = null;
		JsonNode jsonResponse = null;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/currentuser").build();
		    GETRequest currentUserRequest = new GETRequest(uri, null, useAdminClient);
		    response = currentUserRequest.execute();
			String currentUser = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			User user = mapper.readValue(currentUser, User.class);
			if(user == null) {
				throw new AppException("Can't get current-user information on response: '" + currentUser + "'", 
						ErrorCode.API_MANAGER_COMMUNICATION);
			}
			return user;
		    
		} catch (Exception e) {
			throw new AppException("Can't get current-user information on response: '" + jsonResponse + "'", 
					ErrorCode.API_MANAGER_COMMUNICATION, e);
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
	public IAPI getAPIManagerAPI(JsonNode jsonConfiguration, IAPI desiredAPI) throws AppException {
		if(jsonConfiguration == null) {
			IAPI apiManagerAPI = new ActualAPI();
			apiManagerAPI.setValid(false);
			return apiManagerAPI;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		IAPI apiManagerApi;
		try {
			apiManagerApi = mapper.readValue(jsonConfiguration.toString(), ActualAPI.class);
			apiManagerApi.setAPIDefinition(new APIDefintion(getOriginalAPIDefinitionFromAPIM(apiManagerApi.getApiId())));
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
				((AbstractAPI)apiManagerApi).setCustomProperties(customProperties);
			}
			addQuotaConfiguration(apiManagerApi, desiredAPI);
			addClientOrganizations(apiManagerApi, desiredAPI);
			addClientApplications(apiManagerApi, desiredAPI);
			return apiManagerApi;
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-State.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	private void addClientOrganizations(IAPI apiManagerApi, IAPI desiredAPI) throws AppException {
		if(!hasAdminAccount) return;
		if(desiredAPI.getState().equals(IAPI.STATE_UNPUBLISHED)) {
			LOG.info("Ignoring Client-Organizations, as desired API-State is Unpublished!");
			return;
		}
		if(desiredAPI.getClientOrganizations()==null && desiredAPI.getApplications()==null) return;
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
	
	private void addClientApplications(IAPI apiManagerApi, IAPI desiredAPI) throws AppException {
		if(!hasAdminAccount) return;
		if(desiredAPI.getState().equals(IAPI.STATE_UNPUBLISHED)) {
			LOG.info("Ignoring Client-Applications, as desired API-State is Unpublished!");
			return;
		}
		if(desiredAPI.getClientOrganizations()==null && desiredAPI.getApplications()==null) return;
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
	public String getOrgId(String orgName) throws AppException {
		if(!this.hasAdminAccount) return null;
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
	public String getOrgName(String orgId) throws AppException {
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
	public ClientApplication getApplication(String appName) throws AppException {
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
	public ClientApplication getAppIdForCredential(String credential, String type) throws AppException {
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
	
	
	
	private static void addQuotaConfiguration(IAPI api, IAPI desiredAPI) throws AppException {
		//APPLICATION:	00000000-0000-0000-0000-000000000001
		//SYSTEM: 		00000000-0000-0000-0000-000000000000
		// No need to load quota, if not given in the desired API
		if(desiredAPI!=null && (desiredAPI.getApplicationQuota() == null && desiredAPI.getSystemQuota() == null)) return;
		ActualAPI managerAPI = (ActualAPI)api;
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
				RestAPICall getRequest = new GETRequest(uri, null, true);
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
	public JsonNode getExistingAPI(String apiPath) throws AppException {
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
	
	private static byte[] getOriginalAPIDefinitionFromAPIM(String backendApiID) throws AppException {
		URI uri;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/apirepo/"+backendApiID+"/download")
					.setParameter("original", "true").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			HttpResponse response=getRequest.execute();
			String res = EntityUtils.toString(response.getEntity(),StandardCharsets.UTF_8);
			return res.getBytes(StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new AppException("Can't read Swagger-File.", ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
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
			RestAPICall getRequest = new GETRequest(uri, null, true);
			HttpResponse httpResponse = getRequest.execute();
			response = EntityUtils.toString(httpResponse.getEntity());
			allApiAccess = mapper.readValue(response, new TypeReference<List<APIAccess>>(){});
			return allApiAccess;
		} catch (Exception e) {
			LOG.error("Error cant load API-Access for "+type+" from API-Manager. Can't parse response: " + response);
			throw new AppException("API-Access for "+type+" from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	
	
	public List<Organization> getAllOrgs() throws AppException {
		if(!hasAdminAccount) {
			LOG.error("Cant load all organizations without an Admin-Account.");
			return null;
		}
		if(APIManagerAdapter.allOrgs!=null) {
			return APIManagerAdapter.allOrgs;
		}
		allOrgs = new ArrayList<Organization>();
		ObjectMapper mapper = new ObjectMapper();
		String response = null;
		URI uri;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/organizations").build();
			RestAPICall getRequest = new GETRequest(uri, null, true);
			HttpResponse httpResponse = getRequest.execute();
			response = EntityUtils.toString(httpResponse.getEntity());
			allOrgs = mapper.readValue(response, new TypeReference<List<Organization>>(){});
			return allOrgs;
		} catch (Exception e) {
			LOG.error("Error cant read all orgs from API-Manager. Can't parse response: " + response);
			throw new AppException("Can't read all orgs from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	public List<ClientApplication> getAllApps() throws AppException {
		if(!hasAdminAccount) {
			LOG.error("Cant load all applications without an Admin-Account.");
			return null;
		}
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
			RestAPICall getRequest = new GETRequest(uri, null, true);
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
			RestAPICall getRequest = new GETRequest(uri, null, true);
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

	public boolean hasAdminAccount() {
		return hasAdminAccount;
	}
	
	public boolean isUsingOrgAdmin() {
		return usingOrgAdmin;
	}
}
