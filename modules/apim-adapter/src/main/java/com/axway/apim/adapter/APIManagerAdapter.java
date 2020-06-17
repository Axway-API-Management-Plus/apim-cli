package com.axway.apim.adapter;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.ehcache.Status;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIMethodAdapter;
import com.axway.apim.adapter.apis.APIManagerConfigAdapter;
import com.axway.apim.adapter.apis.APIManagerOAuthClientProfilesAdapter;
import com.axway.apim.adapter.apis.APIManagerOrganizationAdapter;
import com.axway.apim.adapter.apis.APIManagerPoliciesAdapter;
import com.axway.apim.adapter.apis.APIManagerQuotaAdapter;
import com.axway.apim.adapter.clientApps.APIMgrAppsAdapter;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.User;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.DoNothingCacheManager;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.TestIndicator;
import com.axway.apim.lib.utils.rest.APIMHttpClient;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.JsonParser;
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
	
	public static String apiManagerVersion = null;
	
	public static ObjectMapper mapper = new ObjectMapper();
	
	private static Map<String, ClientApplication> clientCredentialToAppMap = new HashMap<String, ClientApplication>();
	
	private boolean usingOrgAdmin = false;
	private boolean hasAdminAccount = false;
	
	private ErrorState error = ErrorState.getInstance();
	
	public static String CREDENTIAL_TYPE_API_KEY 		= "apikeys";
	public static String CREDENTIAL_TYPE_EXT_CLIENTID	= "extclients";
	public static String CREDENTIAL_TYPE_OAUTH			= "oauth";
	
	public final static String SYSTEM_API_QUOTA 				= "00000000-0000-0000-0000-000000000000";
	public final static String APPLICATION_DEFAULT_QUOTA 		= "00000000-0000-0000-0000-000000000001";
	
	public final static String TYPE_FRONT_END = "proxies";
	public final static String TYPE_BACK_END = "apirepo";
	
	public static enum CUSTOM_PROP_TYPE {
		api, 
		user, 
		organization, 
		application
	}
	
	private CommandParameters cmd;
	
	private static CacheManager cacheManager;
	
	public APIManagerConfigAdapter configAdapter;
	public APIManagerAPIAdapter apiAdapter;
	public APIManagerAPIMethodAdapter methodAdapter;
	public APIManagerPoliciesAdapter policiesAdapter;
	public APIManagerQuotaAdapter quotaAdapter;
	public APIManagerOrganizationAdapter orgAdapter;
	public APIManagerAPIAccessAdapter accessAdapter;
	public APIManagerOAuthClientProfilesAdapter oauthClientAdapter;
	public APIMgrAppsAdapter appAdapter;
	
	public static enum CacheType {
		applicationAPIAccessCache, 
		organizationAPIAccessCache, 
		oauthClientProviderCache, 
		applicationsCache, 
		applicationsSubscriptionCache, 
		applicationsQuotaCache,
		applicationsCredentialCache,
		organizationCache;
	}
	
	public static synchronized APIManagerAdapter getInstance() throws AppException {
		if (APIManagerAdapter.instance == null) {
			APIManagerAdapter.instance = new APIManagerAdapter();
			if(!TestIndicator.getInstance().isTestRunning()) {
				LOG.info("Successfully connected to API-Manager (" + getApiManagerVersion() + ") on: " + CommandParameters.getInstance().getAPIManagerURL());
			}
		}
		return APIManagerAdapter.instance;
	}
	
	public static synchronized void deleteInstance() throws AppException {
		if(APIManagerAdapter.cacheManager!=null && APIManagerAdapter.cacheManager.getStatus()==Status.AVAILABLE) {
			LOG.debug("Closing cache begin");
			APIManagerAdapter.cacheManager.close();
			LOG.debug("Closing cache end");
		}
		APIManagerAdapter.instance = null;
	}
	
	private APIManagerAdapter() throws AppException {
		super();
		this.cmd = CommandParameters.getInstance();
		this.configAdapter = new APIManagerConfigAdapter();
		this.apiAdapter = new APIManagerAPIAdapter();
		this.methodAdapter = new APIManagerAPIMethodAdapter();
		this.policiesAdapter = new APIManagerPoliciesAdapter();
		this.quotaAdapter = new APIManagerQuotaAdapter();
		this.orgAdapter = new APIManagerOrganizationAdapter();
		this.accessAdapter = new APIManagerAPIAccessAdapter();
		this.oauthClientAdapter = new APIManagerOAuthClientProfilesAdapter();
		this.appAdapter = new APIMgrAppsAdapter();
		if(TestIndicator.getInstance().isTestRunning()) {
			this.hasAdminAccount = true; // For unit tests we have an admin account
			return; // No need to initialize just for Unit-Tests
		}
		
		loginToAPIManager(false); // Login with the provided user (might be an Org-Admin)
		loginToAPIManager(true); // Second, login if needed with an admin account
	}
	
	public void loginToAPIManager(boolean useAdminClient) throws AppException {
		URI uri;
		if(cmd.ignoreAdminAccount() && useAdminClient) return;
		if(hasAdminAccount && useAdminClient) return; // Already logged in with an Admin-Account.
		HttpResponse httpResponse = null;
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
		    POSTRequest loginRequest = new POSTRequest(new UrlEncodedFormEntity(params), uri, useAdminClient);
			loginRequest.setContentType(null);
			httpResponse = loginRequest.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				String response = EntityUtils.toString(httpResponse.getEntity());
				if(statusCode==403 && response.contains("Unknown API")) {
					LOG.warn("Login failed with statusCode: " +statusCode+ ". Got response: '"+response+"' ... Try again in 1 second.");
					Thread.sleep(1000);
					httpResponse = loginRequest.execute();
					statusCode = httpResponse.getStatusLine().getStatusCode();
					if(statusCode < 200 || statusCode > 299){
						LOG.error("Login finally failed with statusCode: " +statusCode+ ". Got response: '"+response+"' Got response: '"+response+"'");
						throw new AppException("Login finally failed with statusCode: " +statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
					} else {
						LOG.info("Successfully logged in on retry. Received Status-Code: " +statusCode );
					}
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
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}	
	}
	
	private String[] getAdminUsernamePassword() throws AppException {
		if(CommandParameters.getInstance().getAdminUsername()==null) return null;
		String[] usernamePassword =  {CommandParameters.getInstance().getAdminUsername(), CommandParameters.getInstance().getAdminPassword()};
		return usernamePassword;
	}
	
	public static User getCurrentUser(boolean useAdminClient) throws AppException {
		URI uri;
		HttpResponse response = null;
		JsonNode jsonResponse = null;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/currentuser").build();
		    GETRequest currentUserRequest = new GETRequest(uri, useAdminClient);
		    response = currentUserRequest.execute();
		    getCsrfToken(response, useAdminClient); // Starting from 7.6.2 SP3 the CSRF token is returned on CurrentUser request
			String currentUser = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			int statusCode = response.getStatusLine().getStatusCode();
			if( statusCode != 200) {
				throw new AppException("Status-Code: "+statusCode+", Can't get current-user information on response: '" + currentUser + "'", 
						ErrorCode.API_MANAGER_COMMUNICATION);				
			}
			User user = mapper.readValue(currentUser, User.class);
			if(user == null) {
				throw new AppException("Can't get current-user information on response: '" + currentUser + "'", 
						ErrorCode.API_MANAGER_COMMUNICATION);
			}
			return user;
		    
		} catch (Exception e) {
			throw new AppException("Can't get current-user information on response: '" + jsonResponse + "'", 
					ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(response!=null) 
					((CloseableHttpResponse)response).close();
			} catch (Exception ignore) {}
		}
	}
	
	private static void getCsrfToken(HttpResponse response, boolean useAdminClient) throws AppException {
		for (Header header : response.getAllHeaders()) {
			if(header.getName().equals("CSRF-Token")) {
				APIMHttpClient.getInstance(useAdminClient).setCsrfToken(header.getValue());
				break;
			}
		}
	}
	
	private static CacheManager getCacheManager() {
		if(APIManagerAdapter.cacheManager!=null) {
			if(APIManagerAdapter.cacheManager.getStatus()==Status.UNINITIALIZED) 
				APIManagerAdapter.cacheManager.init();
			return APIManagerAdapter.cacheManager;
		}
		if(CommandParameters.getInstance().ignoreCache()) {
			APIManagerAdapter.cacheManager = new DoNothingCacheManager();
		} else {
			URL myUrl = APIManagerAdapter.class.getResource("/cacheConfig.xml");
			XmlConfiguration xmlConfig = new XmlConfiguration(myUrl);
			APIManagerAdapter.cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
			APIManagerAdapter.cacheManager.init();
		}
		return cacheManager;
	}
	
	public static <K, V> Cache<K, V> getCache(CacheType cacheType, Class<K> key, Class<V> value) {
		getCacheManager();
		Cache<K, V> cache = APIManagerAdapter.cacheManager.getCache(cacheType.name(), key, value);
		if(CommandParameters.getInstance().clearCaches()!=null && CommandParameters.getInstance().clearCaches().contains(cacheType)) {
			cache.clear();
			LOG.info("Cache: " + cacheType.name() + " successfully cleared.");
		}
		return cache;
	}
	
	
	
	public static void clearCache(String cacheName) {
		if(APIManagerAdapter.cacheManager==null || APIManagerAdapter.cacheManager.getStatus()==Status.UNINITIALIZED) return;
		Cache<Object, Object> cache = APIManagerAdapter.cacheManager.getCache(cacheName, null, null);
		cache.clear();
	}
	
	
	
	/**
	 * Checks if the API-Manager has at least given version. If the given requested version is the same or lower 
	 * than the actual API-Manager version, true is returned otherwise false.  
	 * This helps to use features, that are introduced with a certain version or even service-pack.
	 * @param version has the API-Manager this version of higher?
	 * @return false if API-Manager doesn't have this version otherwise true
	 */
	public static boolean hasAPIManagerVersion(String version) {
		try {
			List<String> managerVersion	= getMajorVersions(getApiManagerVersion());
			List<String> requestedVersion	= getMajorVersions(version);
			Date datedManagerVersion = getDateVersion(managerVersion);
			Date datedRequestedVersion = getDateVersion(requestedVersion);
			int managerSP	= getServicePackVersion(getApiManagerVersion());
			int requestedSP = getServicePackVersion(version);
			for(int i=0;i<requestedVersion.size(); i++) {
				int managerVer = Integer.parseInt(managerVersion.get(i));
				if(managerVer>Integer.parseInt(requestedVersion.get(i))) return true;
				if(managerVer<Integer.parseInt(requestedVersion.get(i))) return false;
				if(datedManagerVersion!=null && datedRequestedVersion!=null && datedManagerVersion.before(datedRequestedVersion)) return false;
			}
			if(requestedSP!=0 && datedManagerVersion!=null) return true;
			if(managerSP<requestedSP) return false;
		} catch(Exception e) {
			LOG.warn("Can't parse API-Manager version: '"+apiManagerVersion+"'. Requested version was: '"+version+"'. Returning false!");
			return false;
		}
		return true;
	}
	
	private static Date getDateVersion(List<String> managerVersion) {
		if(managerVersion.size()==3) {
			try {
				String dateVersion = managerVersion.get(2);
				Date datedVersion=new SimpleDateFormat("yyyyMMdd").parse(dateVersion);				
				return datedVersion;
			} catch (Exception e) {
				LOG.trace("API-Manager version: '"+apiManagerVersion+"' seems not to contain a dated version");
			}
		}
		return null;
	}
	
	private static int getServicePackVersion(String version) {
		int spNumber = 0;
		if(version.contains(" SP")) {
			try {
				String spVersion = version.substring(version.indexOf(" SP")+3);
				spNumber = Integer.parseInt(spVersion);
			} catch (Exception e){
				LOG.trace("Can't parse service pack version in version: '"+version+"'");
			}
		}
		return spNumber;
	}
	
	private static List<String> getMajorVersions(String version) {
		List<String> majorNumbers = new ArrayList<String>();
		String versionWithoutSP = version;
		if(version.contains(" SP")) {
			versionWithoutSP = version.substring(0, version.indexOf(" SP"));
		}
		try {
			String[] versions = versionWithoutSP.split("\\.");
			for(int i = 0; i<versions.length; i++) {
				majorNumbers.add(versions[i]);
			}
		} catch (Exception e){
			LOG.trace("Can't parse major version numbers in: '"+version+"'");
		}
		return majorNumbers;
	}
	
	/**
	 * The actual App-ID based on the AppName. Lazy implementation.
	 * @param credential The credentials (API-Key, Client-ID) which is registered for an application
	 * @param type of the credential. See APIManagerAdapter for potential credential types 
	 * @return the id of the organization
	 * @throws AppException if JSON response from API-Manager can't be parsed
	 */
	public ClientApplication getAppIdForCredential(String credential, String type) throws AppException {
		if(clientCredentialToAppMap.containsKey(type+"_"+credential)) {
			ClientApplication app = clientCredentialToAppMap.get(type+"_"+credential);
			LOG.info("Found existing application (in cache): '"+app.getName()+"' based on credential (Type: '"+type+"'): '"+credential+"'");
			return app;
		}
		List<ClientApplication> allApps = this.appAdapter.getAllApplications(false); // Make sure, we loaded all apps before!
		LOG.debug("Searching credential (Type: "+type+"): '"+credential+"' in: " + allApps.size() + " apps.");
		Collection<ClientApplication> appIds = clientCredentialToAppMap.values();
		HttpResponse httpResponse = null;
		for(ClientApplication app : allApps) {
			if(appIds.contains(app)) continue;
			String response = null;
			URI uri;
			try {
				uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/applications/"+app.getId()+"/"+type+"").build();
				LOG.debug("Loading credentials of type: '" + type + "' for application: '" + app.getName() + "' from API-Manager.");
				RestAPICall getRequest = new GETRequest(uri, true);
				httpResponse = getRequest.execute();
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
						if(clientId.get("clientId")==null) {
							key = "NOT_FOUND";
						} else {
							key = clientId.get("clientId").asText();
						}
					} else {
						throw new AppException("Unknown credential type: " + type, ErrorCode.UNXPECTED_ERROR);
					}
					LOG.debug("Found credential (Type: '"+type+"'): '"+key+"' for application: '"+app.getName()+"'");
					clientCredentialToAppMap.put(type+"_"+key, app);
					if(key.equals(credential)) {
						LOG.info("Found existing application: '"+app.getName()+"' ("+app.getId()+") based on credential (Type: '"+type+"'): '"+credential+"'");
						return app;
					}
				}
			} catch (Exception e) {
				LOG.error("Can't load applications credentials. Can't parse response: " + response, e);
				throw new AppException("Can't load applications credentials.", ErrorCode.API_MANAGER_COMMUNICATION, e);
			} finally {
				try {
					if(httpResponse!=null) 
						((CloseableHttpResponse)httpResponse).close();
				} catch (Exception ignore) {}
			}
		}
		LOG.error("No application found for credential ("+type+"): " + credential);
		return null;
	}
	
	public static Image getImageFromAPIM(URI uri, String baseFilename) throws AppException {
		Image image = new Image();
		HttpResponse httpResponse = null;
		try {
			RestAPICall getRequest = new GETRequest(uri);
			httpResponse = getRequest.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode == 404) return null; // No Image found
			if(statusCode != 200) {
				LOG.error("Can't read Image from API-Manager.. Message: '"+EntityUtils.toString(httpResponse.getEntity())+"' Response-Code: "+statusCode+"");
				throw new AppException("Can't read Image from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			if(httpResponse == null || httpResponse.getEntity() == null) return null; // no Image found in API-Manager
			InputStream is = httpResponse.getEntity().getContent();
			image.setImageContent(IOUtils.toByteArray(is));
			if(httpResponse.containsHeader("Content-Type")) {
				String contentType = httpResponse.getHeaders("Content-Type")[0].getValue();
				image.setContentType(contentType);
			}
			image.setBaseFilename(baseFilename);
			return image;
		} catch (Exception e) {
			throw new AppException("Can't read Image from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public static String getApiManagerVersion() throws AppException {
		if(APIManagerAdapter.apiManagerVersion!=null) {
			return apiManagerVersion;
		}
		APIManagerAdapter.apiManagerVersion = APIManagerAdapter.getInstance().configAdapter.getApiManagerConfig("productVersion");
		return APIManagerAdapter.apiManagerVersion;
	}
	
	public static Map<String, String> getAllConfiguredCustomProperties(CUSTOM_PROP_TYPE type) {
    	Map<String, String> allCustomProps = new HashMap<String, String>();
    	try {
    		JsonNode appConfig = getCustomPropertiesConfig();
    		JsonNode apiCustomProps = appConfig.get(type.name());
    		if(apiCustomProps==null) return null;
    		Iterator<Entry<String, JsonNode>> it = apiCustomProps.fields();
    		while(it.hasNext()) {
    			Entry<String, JsonNode> entry = it.next();
    			allCustomProps.put(entry.getKey(), null);
    		}
    		return allCustomProps;
    	} catch (Exception e) {
    		LOG.error("Error loading configured custom properties from API-Manager", e);
    		return null;
    	}
    }
	
	public static JsonNode getCustomPropertiesConfig() throws AppException {
		
		String appConfig = null;
		URI uri;
		HttpEntity httpResponse = null;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath("/vordel/apiportal/app/app.config").build();
			RestAPICall getRequest = new GETRequest(uri);
			httpResponse = getRequest.execute().getEntity();
			appConfig = IOUtils.toString(httpResponse.getContent(), "UTF-8");
			return parseAppConfig(appConfig);
		} catch (Exception e) {
			throw new AppException("Can't read app.config from API-Manager: '" + appConfig + "'", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
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
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/certinfo/").build();
			
			HttpEntity entity = MultipartEntityBuilder.create()
					.addBinaryBody("file", IOUtils.toByteArray(certFile), ContentType.create("application/x-x509-ca-cert"), cert.getCertFile())
					.addTextBody("inbound", cert.getInbound())
					.addTextBody("outbound", cert.getOutbound())
					.build();
			POSTRequest postRequest = new POSTRequest(entity, uri);
			postRequest.setContentType(null);
			httpResponse = postRequest.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if( statusCode != 200){
				LOG.error("Can't decode provided certificate. Message: '"+EntityUtils.toString(httpResponse.getEntity())+"' Response-Code: "+statusCode+"");
				throw new AppException("Can't decode provided certificate: " + cert.getCertFile(), ErrorCode.API_MANAGER_COMMUNICATION);
			}
			JsonNode jsonResponse = mapper.readTree(httpResponse.getEntity().getContent());
			return jsonResponse;
		} catch (Exception e) {
			throw new AppException("Can't read certificate information from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	/**
	 * Helper method to translate a Base64 encoded format 
	 * as it's needed by the API-Manager.
	 * @param certificate the certificate content
	 * @param filename the name of the certificate file used as a reference in the generated Json object
	 * @throws AppException when the certificate information can't be created
	 * @return a Json-Object structure as needed by the API-Manager
	 */
	public static JsonNode getFileData(byte[] certificate, String filename) throws AppException {
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/filedata/").build();
			
			HttpEntity entity = MultipartEntityBuilder.create()
					.addBinaryBody("file", certificate, ContentType.create("application/x-pkcs12"), filename)
					.build();
			POSTRequest postRequest = new POSTRequest(entity, uri);
			postRequest.setContentType(null);
			httpResponse = postRequest.execute();
			JsonNode jsonResponse = mapper.readTree(httpResponse.getEntity().getContent());
			return jsonResponse;
		} catch (Exception e) {
			throw new AppException("Can't read certificate information from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}

	/**
	 * @return true, when admin credentials are provided
	 * @throws AppException when the API-Manager instance is not initialized
	 */
	public static boolean hasAdminAccount() throws AppException {
		return APIManagerAdapter.getInstance().hasAdminAccount;
	}
	
	/**
	 * @return true, if an OrgAdmin is the primary user (additional Admin-Credentials may have provided anyway)
	 */
	public boolean isUsingOrgAdmin() {
		return usingOrgAdmin;
	}
}
