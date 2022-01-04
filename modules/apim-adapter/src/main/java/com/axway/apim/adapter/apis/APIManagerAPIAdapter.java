package com.axway.apim.adapter.apis;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.adapter.apis.APIFilter.METHOD_TRANSLATION;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.adapter.jackson.APIImportSerializerModifier;
import com.axway.apim.adapter.jackson.PolicySerializerModifier;
import com.axway.apim.api.API;
import com.axway.apim.api.APIBaseDefinition;
import com.axway.apim.api.definition.APISpecification;
import com.axway.apim.api.definition.APISpecification.APISpecType;
import com.axway.apim.api.definition.APISpecificationFactory;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.api.model.Profile;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.DELRequest;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.PUTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class APIManagerAPIAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerAPIAdapter.class);

	Map<APIFilter, String> apiManagerResponse = new HashMap<APIFilter, String>();
	
	Map<String, Image> imagesResponse = new HashMap<String, Image>();
	
	ObjectMapper mapper = new ObjectMapper();
	
	static CoreParameters cmd = CoreParameters.getInstance();
	
	/**
	 * Maps the provided status to the REST-API endpoint to change the status!
	 */
	public static enum StatusEndpoint {
		unpublished("unpublish"),
		published("publish"), 
		deprecated("deprecate"), 
		undeprecated("undeprecate");
		
		private final String endpoint;

		private StatusEndpoint(String endpoint) {
			this.endpoint = endpoint;
		}

		public String getEndpoint() {
			return endpoint;
		}
	};

	public APIManagerAPIAdapter() throws AppException {
		FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"apiId"}));
		mapper.setFilterProvider(filter);
	}

	public List<API> getAPIs(APIFilter filter, boolean logProgress) throws AppException {
		List<API> apis = new ArrayList<API>();
		try {
			_readAPIsFromAPIManager(filter);
			apis = filterAPIs(filter);
			for(int i=0;i<apis.size();i++) {
				API api = apis.get(i);
				translateMethodIds(api, filter.getTranslateMethodMode());
				addQuotaConfiguration(api, filter.isIncludeQuotas());
				addClientOrganizations(api, filter.isIncludeClientOrganizations());
				addClientApplications(api, filter);
				addExistingClientAppQuotas(api, filter.isIncludeQuotas());
				addOriginalAPIDefinitionFromAPIM(api, filter);
				addImageFromAPIM(api, filter.isIncludeImage());
				addRemoteHost(api, filter.isIncludeRemoteHost());
				if(logProgress && apis.size()>5) Utils.progressPercentage(i, apis.size(), "Loading details of "+apis.size()+" APIs");
			}
			Utils.addCustomPropertiesForEntity(apis, this.apiManagerResponse.get(filter), filter);
			if(logProgress && apis.size()>5) System.out.print("\n");
		} catch (IOException e) {
			throw new AppException("Cannot read APIs from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		return apis;
	}
	
	public API getAPI(APIFilter filter, boolean logMessage) throws AppException {
		List<API> foundAPIs = getAPIs(filter, false);
		API api = getUniqueAPI(foundAPIs, filter);
		if(logMessage && api!=null) LOG.info("Found existing API: '"+api.getName()+"' ("+api.getState()+") on path: '"+api.getPath()+"' (ID: '"+api.getId()+"')");
		return api;
	}
	
	public API getAPIWithId(String id) throws AppException {
		if(id==null) return null;
		return getAPI(new APIFilter.Builder().hasId(id).build(), false);
	}

	/**
	 * Returns a list of requested proxies (Front-End APIs).
	 * @throws AppException if the API representation cannot be created
	 */
	private void _readAPIsFromAPIManager(APIFilter filter) throws AppException {
		if(this.apiManagerResponse.get(filter)!=null) return;
		URI uri;
		HttpResponse httpResponse = null;
		try { 
			uri = getAPIRequestUri(filter);
			LOG.debug("Sending request to find existing APIs: " + uri);
			RestAPICall getRequest = new GETRequest(uri, APIManagerAdapter.hasAdminAccount());
			httpResponse = getRequest.execute();
			String response = EntityUtils.toString(httpResponse.getEntity());
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				if(statusCode == 403 && filter.getId()!=null) {
					LOG.error("Unable to find API with ID: "+filter.getId()+". Please have in mind during API-Update the ID is re-created!");
					apiManagerResponse.put(filter, "[]");
					return;
				}
				LOG.error("Error loading APIs from API-Manager. Response-Code: "+statusCode+". Got response: '"+response+"'");
				throw new AppException("Error loading APIs from API-Manager. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			if(response.startsWith("{")) { // Got a single response!
				response = "["+response+"]";
			}
			apiManagerResponse.put(filter, response);
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	URI getAPIRequestUri(APIFilter filter) throws URISyntaxException {		
		String requestedId = "";
		if(filter==null) filter = new APIFilter.Builder().build();
		if(filter.getId()!=null) {
			requestedId = "/"+filter.getId();
		}
		URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/"+filter.getApiType() + requestedId)
				.addParameters(filter.getFilters())
				.build();
		return uri;
	}
	
	API getUniqueAPI(List<API> foundAPIs, APIFilter filter) throws AppException {
		if(foundAPIs.size()==0) return null;
		// If filtered resultSet contains more than one API, here we try to find a unique API based on the logical 
		// criteria (apiPath, VHost and QueryVersion)
		// This can occur if the DesiredAPI does not define a QueryStringVersion and/or VHost. Then it will filter 
		// without QueryString and return all APIs with the same path. 
		// With or without QueryStringVersion/Vhost. It is then the API unique that has no QueryVersion 
		// defined, which is the actual API according to the desiredAPI. The same applies to the VHost.
		if(foundAPIs.size()>1) {
			Map<String, List<API>> apisPerKey = new HashMap<String, List<API>>();
			// Create a List of APIs based on the logical keys
			for(API api : foundAPIs) {
				String key = api.getPath() + "###" + api.getVhost() + "###" + api.getApiRoutingKey();
				if(apisPerKey.containsKey(key)) {
					apisPerKey.get(key).add(api);
				} else {
					List<API> apiWithKey = new ArrayList<API>();
					apiWithKey.add(api);
					apisPerKey.put(key, apiWithKey);
				}
			}
			String filterKey = filter.getApiPath()+"###"+filter.getVhost()+"###"+filter.getQueryStringVersion();
			if(apisPerKey.get(filterKey)!=null && apisPerKey.get(filterKey).size() ==1) {
				return apisPerKey.get(filterKey).get(0);
			}
			throw new AppException("No unique API found. Found " + foundAPIs.size() + " APIs based on filter: " + filter, ErrorCode.UNKNOWN_API);
		}
		return foundAPIs.get(0);
	}

	private List<API> filterAPIs(APIFilter filter) throws AppException, JsonParseException, JsonMappingException, IOException {
		List<API> apis = mapper.readValue(this.apiManagerResponse.get(filter), new TypeReference<List<API>>(){});
		apis.removeIf(api -> filter.filter(api));
		
		if(apis.size()!=0) {
			String dbgCrit = "";
			if(apis.size()>1) 
				dbgCrit = " (apiPath: '"+filter.getApiPath()+"', filter: "+filter+", vhost: '"+filter.getVhost()+"', requestedType: "+filter.getApiType()+")";
			LOG.debug("Found: "+apis.size()+" exposed API(s)" + dbgCrit);
			return apis;
		}
		LOG.debug("No existing API found based on filter: " + getFilterFields(filter));
		return apis;
	}
	
	/**
	 * Translates the methodIds of the given api. The operations are loaded from the API-Manager based on the apiId
	 * @param <profile> An Outbound- or InboundProfile
	 * @param api in which the methods should be translated
	 * @param apiId the methods are loaded based on this API-ID (this might be a another referenced API 
	 * @param mode translation direction 
	 * @throws AppException when something goes wrong
	 */
	public <profile> void translateMethodIds(API api, String apiId, METHOD_TRANSLATION mode) throws AppException {
		if(mode == METHOD_TRANSLATION.NONE) return; 
		translateMethodIds(Arrays.asList(api), Arrays.asList(apiId), mode);
	}
	
	/**
	 * Translates the methodIds of the given api. The operations are loaded from the API-Manager based on the api.getId()
	 * @param api in which the methods should be translated
	 * @param mode translation direction 
	 * @param <profile> the type of the profile
	 * @throws AppException if methods cannot be translated 
	 */
	public <profile> void translateMethodIds(API api, METHOD_TRANSLATION mode) throws AppException {
		if(mode == METHOD_TRANSLATION.NONE) return;
		if(api.getOutboundProfiles()!=null) _translateMethodIds(api.getOutboundProfiles(), mode, Arrays.asList(api.getId()));
		if(api.getInboundProfiles()!=null) _translateMethodIds(api.getInboundProfiles(), mode, Arrays.asList(api.getId()));
	}
	
	public <profile> void translateMethodIds(List<API> apis, List<String> apiIds, METHOD_TRANSLATION mode) throws AppException {
		if(mode == METHOD_TRANSLATION.NONE) return; 
		for(API api : apis) {
			if(api.getOutboundProfiles()!=null) _translateMethodIds(api.getOutboundProfiles(), mode, apiIds);
			if(api.getInboundProfiles()!=null) _translateMethodIds(api.getInboundProfiles(), mode, apiIds);
		}
	}
	
	private void addImageFromAPIM(API api, boolean includeImage) throws AppException {
		if(!includeImage) {
			api.setImage(null);
			return;
		}
		Image image = new Image();
			image = new Image();
			URI uri;
			HttpResponse httpResponse = null;
			try {
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/proxies/"+api.getId()+"/image").build();
				RestAPICall getRequest = new GETRequest(uri);
				httpResponse = getRequest.execute();
				if(httpResponse == null || httpResponse.getEntity() == null || httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
					api.setImage(null);
					return; // no Image found in API-Manager
				}
				InputStream is = httpResponse.getEntity().getContent();
				image.setImageContent(IOUtils.toByteArray(is));
				if(httpResponse.containsHeader("Content-Type")) {
					String contentType = httpResponse.getHeaders("Content-Type")[0].getValue();
					image.setContentType(contentType);
				}
				image.setBaseFilename("api-image");
				api.setImage(image);
			} catch (Exception e) {
				throw new AppException("Can't read Image from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
			} finally {
				try {
					if(httpResponse!=null) 
						((CloseableHttpResponse)httpResponse).close();
				} catch (Exception ignore) {}
			}
	}
	
	private void addRemoteHost(API api, boolean includeRemoteHost) throws AppException {
		if(!includeRemoteHost) return;
		String backendBasePath = null;
		try {
			backendBasePath = api.getServiceProfiles().get("_default").getBasePath();
			URL url = new URL(backendBasePath);
			RemoteHost remoteHost = APIManagerAdapter.getInstance().remoteHostsAdapter.getRemoteHost(url.getHost(), url.getPort());
			api.setRemotehost(remoteHost);
		} catch (Exception e) {
			if(LOG.isDebugEnabled()) {
				LOG.error("Error setting remote host for API based on backendBasePath: " + backendBasePath, e);
			}
		}
	}
	
	public void updateAPIImage(API api, Image image) throws AppException {
		if(!image.isValid()) {
			LOG.info("No image configured, doing nothing.");
			return;
		}
		api.setImage(image);
		LOG.debug("Updating API-Proxy-Image from file: " + api.getImage().getFilename());
		
		URI uri;
		HttpEntity entity;
		HttpResponse httpResponse = null;
		
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath()+"/proxies/"+api.getId()+"/image").build();
			
			entity = MultipartEntityBuilder.create()
						.addBinaryBody("file", api.getImage().getInputStream(), ContentType.create("image/jpeg"), api.getImage().getBaseFilename())
					.build();
			
			RestAPICall apiCall = new POSTRequest(entity, uri);
			httpResponse = apiCall.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String response = EntityUtils.toString(httpResponse.getEntity());
			if(statusCode != 200){
				LOG.error("Error updating API-Image. Received Status-Code: " +statusCode+ ", Response: '" + response + "'");
				throw new AppException("Error updating API-Image. Received Status-Code: " +statusCode, ErrorCode.UNXPECTED_ERROR);
			}
		} catch (Exception e) {
			throw new AppException("Can't update API-Image.", ErrorCode.UNXPECTED_ERROR, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	private <ProfileType> void _translateMethodIds(Map<String, ProfileType> profiles, METHOD_TRANSLATION mode, List<String> apiIds) throws AppException {
		Map<String, ProfileType> updatedEntries = new HashMap<String, ProfileType>();
		
		if(profiles!=null) {
			Iterator<String> keys = profiles.keySet().iterator();
			while(keys.hasNext()) {
				String key = keys.next();
				if(key.equals("_default")) continue;
				APIMethod method = null;
				for(String apiId : apiIds) {
					if(mode==METHOD_TRANSLATION.AS_NAME) {
						method = APIManagerAdapter.getInstance().methodAdapter.getMethodForId(apiId, key);
					} else {
						method = APIManagerAdapter.getInstance().methodAdapter.getMethodForName(apiId, key);
					}
					if(method!=null) break;
				}
				ProfileType profileWithType = profiles.get(key);
				Profile profile = (Profile)profileWithType;
				if(profile instanceof OutboundProfile) {
					profile.setApiMethodId(method.getApiMethodId());
					profile.setApiMethodName(method.getName());
					profile.setApiId(method.getApiId());
				} else {
					profile.setApiMethodId(method.getId());
					profile.setApiMethodName(method.getName());
					profile.setApiId(method.getVirtualizedApiId());
				}
				if(mode==METHOD_TRANSLATION.AS_NAME) {
					updatedEntries.put(method.getName(), profileWithType);
				} else {
					updatedEntries.put(method.getId(), profileWithType);
				}
				keys.remove();
			}
			profiles.putAll(updatedEntries);
		}
	}
	
	public void addQuotaConfiguration(API api) throws AppException {
		addQuotaConfiguration(api, true);
	}
	
	private void addQuotaConfiguration(API api, boolean addQuota) throws AppException {
		if(!addQuota || !APIManagerAdapter.hasAdminAccount()) return;
		APIQuota applicationQuota = null;
		APIQuota sytemQuota = null;
		try {
			applicationQuota = APIManagerAdapter.getInstance().quotaAdapter.getQuotaForAPI(APIManagerQuotaAdapter.Quota.APPLICATION_DEFAULT.getQuotaId(), api.getId()); // Get the Application-Default-Quota
			sytemQuota = APIManagerAdapter.getInstance().quotaAdapter.getQuotaForAPI(APIManagerQuotaAdapter.Quota.SYSTEM_DEFAULT.getQuotaId(), api.getId()); // Get the Application-Default-QuotagetQuotaFromAPIManager(); // Get the System-Default-Quota
			api.setApplicationQuota(applicationQuota);
			api.setSystemQuota(sytemQuota);
		} catch (AppException e) {
			LOG.error("Application-Default quota response: '"+applicationQuota+"'");
			LOG.error("System-Default quota response: '"+sytemQuota+"'");
			throw e;
		}
	}
	
	private void addExistingClientAppQuotas(API api, boolean addQuota) throws AppException {
		if(!addQuota || !APIManagerAdapter.hasAdminAccount()) return;
		if(api.getApplications()==null || api.getApplications().size()==0) return;
		for(ClientApplication app : api.getApplications()) {
			APIQuota appQuota = APIManagerAdapter.getInstance().quotaAdapter.getQuotaForAPI(app.getId(), null);
			app.setAppQuota(appQuota);
		}
	}
	
	public void addClientOrganizations(API api) throws AppException {
		addClientOrganizations(api, true);
	}
	
	private void addClientOrganizations(API api, boolean addClientOrganizations) throws AppException {
		if(!addClientOrganizations || !APIManagerAdapter.hasAdminAccount()) return;
		List<Organization> grantedOrgs;
		List<Organization> allOrgs = APIManagerAdapter.getInstance().orgAdapter.getAllOrgs();
		grantedOrgs = new ArrayList<Organization>();
		for(Organization org : allOrgs) {
			List<APIAccess> orgAPIAccess = APIManagerAdapter.getInstance().accessAdapter.getAPIAccess(org, APIManagerAPIAccessAdapter.Type.organizations);
			for(APIAccess access : orgAPIAccess) {
				if(access.getApiId().equals(api.getId())) {
					grantedOrgs.add(org);
				}
			}
		}
		api.setClientOrganizations(grantedOrgs);
	}
	
	public void addClientApplications(API api) throws AppException {
		addClientApplications(api, new APIFilter.Builder().includeClientApplications(true).build());
	}
	
	private void addClientApplications(API api, APIFilter filter) throws AppException {
		if(!filter.isIncludeClientApplications()) return;
		List<ClientApplication> existingClientApps = new ArrayList<ClientApplication>();
		List<ClientApplication> apps = null;
		// With version >7.7 we can retrieve the subscribed apps directly
		if(APIManagerAdapter.hasAPIManagerVersion("7.7")) {
			apps = APIManagerAdapter.getInstance().appAdapter.getAppsSubscribedWithAPI(api.getId());
			api.setApplications(apps);
		} else {
			apps = APIManagerAdapter.getInstance().appAdapter.getApplications(new ClientAppFilter.Builder()
					.includeQuotas(filter.isIncludeClientAppQuota()).includeOauthResources(true)
					.build(), false);
			for(ClientApplication app : apps) {
				List<APIAccess> APIAccess = APIManagerAdapter.getInstance().accessAdapter.getAPIAccess(app, APIManagerAPIAccessAdapter.Type.applications, true);
				app.setApiAccess(APIAccess);
				for(APIAccess access : APIAccess) {
					if(access.getApiId().equals(api.getId())) {
						existingClientApps.add(app);
					}
				}
			}
			api.setApplications(existingClientApps);
		}		
	}
	
	private void addOriginalAPIDefinitionFromAPIM(API api, APIFilter filter) throws AppException {
		if(!filter.isIncludeOriginalAPIDefinition()) return;
		URI uri;
		APISpecification apiDefinition;
		HttpResponse httpResponse = null;
		try {
			if(filter.isUseFEAPIDefinition()) {
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/discovery/swagger/api/id/"+api.getId())
						.setParameter("swaggerVersion", "2.0").build();
				LOG.debug("Loading API-Definition from FE-API: ");
			} else {
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/apirepo/"+api.getApiId()+"/download")
						.setParameter("original", "true").build();
			}
			RestAPICall getRequest = new GETRequest(uri, APIManagerAdapter.hasAdminAccount());
			httpResponse=getRequest.execute();
			String res = EntityUtils.toString(httpResponse.getEntity(),StandardCharsets.UTF_8);
			String origFilename = "Unkown filename";
			if(httpResponse.containsHeader("Content-Disposition")) {
				origFilename = httpResponse.getHeaders("Content-Disposition")[0].getValue();
			}
			apiDefinition = APISpecificationFactory.getAPISpecification(res.getBytes(StandardCharsets.UTF_8), origFilename.substring(origFilename.indexOf("filename=")+9), api.getName(), filter.isFailOnError(), false);
			addBackendResourcePath(api, apiDefinition, filter.isUseFEAPIDefinition());
			api.setApiDefinition(apiDefinition);
		} catch (Exception e) {
			throw new AppException("Cannot parse API-Definition for API: '" + api.getName() + "' ("+api.getVersion()+") on path: '"+api.getPath()+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	private void addBackendResourcePath(API api, APISpecification apiDefinition, boolean exportFEAPIDefinition) throws AppException {
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/apirepo/"+api.getApiId()).build();
			RestAPICall request = new GETRequest(uri, APIManagerAdapter.hasAdminAccount());
			httpResponse=request.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String response = EntityUtils.toString(httpResponse.getEntity());
			if(statusCode != 200){
				if((statusCode >= 400 && statusCode<=499) && response.contains("Unknown API")) {
					LOG.warn("Got unexpected error: 'Unknown API' while trying to read Backend-API ... Try again in "+cmd.getRetryDelay()+" milliseconds. (you may set -retryDelay <milliseconds>)");
					Thread.sleep(cmd.getRetryDelay());
					httpResponse = request.execute();
					statusCode = httpResponse.getStatusLine().getStatusCode();
					if(statusCode != 200) {
						LOG.error("Error reading backend API in order to update API-Specification. Received Status-Code: " +statusCode + ", Response: " + EntityUtils.toString(httpResponse.getEntity()));
						throw new AppException("Error reading backend API in order to update API-Specification. Received Status-Code: " +statusCode, ErrorCode.CANT_CREATE_BE_API);
					} else {
						LOG.info("Successfully retrieved backend API information on second request.");
						response = EntityUtils.toString(httpResponse.getEntity());
					}
				} else {
					LOG.error("Error reading backend API in order to update API-Specification. Received Status-Code: " +statusCode+ ", Response: '" + response + "'");
					throw new AppException("Error reading backend API in order to update API-Specification. ", ErrorCode.UNXPECTED_ERROR);
				}
			}
			JsonNode jsonNode = mapper.readTree(response);
			String resourcePath = jsonNode.get("resourcePath").asText();
			String basePath = jsonNode.get("basePath").asText();
			// Only adjust the API-Specification when exporting the FE-API-Spec otherwise we need the originally imported API-Spec
			if(exportFEAPIDefinition) {
				apiDefinition.configureBasepath(basePath + resourcePath);
			}
			// In any case, we save the backend resource path, as it is necessary for the full backendBasepath in the exported API config. 
			api.setBackendResourcePath(resourcePath);
			return;
		} catch (Exception e) {
			throw new AppException("Cannot parse Backend-API for API: '" + api.toStringHuman()+"' in order to change API-Specification", ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public API createAPIProxy(API api) throws AppException {
		LOG.debug("Create Front-End API: '"+api.getName()+"' (API-Proxy)");
		URI uri;
		HttpEntity entity;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath()+"/proxies/").build();
			entity = new StringEntity("{\"apiId\":\"" + api.getApiId() + "\",\"organizationId\":\"" + api.getOrganization().getId() + "\"}", ContentType.APPLICATION_JSON);
			
			RestAPICall request = new POSTRequest(entity, uri);
			httpResponse = request.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String response = EntityUtils.toString(httpResponse.getEntity());
			if(statusCode != 201){
				LOG.error("Error creating API-Proxy (FE-API). Received Status-Code: " +statusCode+ ", Response: '" + response + "'");
				throw new AppException("Error creating API-Proxy (FE-API). Received Status-Code: " +statusCode, ErrorCode.CANT_CREATE_API_PROXY);
			}
			API apiProxy =  mapper.readValue(response, API.class);
			return apiProxy;
		} catch (Exception e) {
			throw new AppException("Can't create API-Proxy.", ErrorCode.CANT_CREATE_API_PROXY, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public API updateAPIProxy(API api) throws AppException {
		LOG.debug("Updating API-Proxy: '"+api.getName()+" "+api.getVersion()+" ("+api.getId()+")'" );
		URI uri;
		HttpEntity entity;
		mapper.setSerializationInclusion(Include.NON_NULL);
		FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
				SimpleBeanPropertyFilter.serializeAllExcept(new String[] {"apiDefinition", "certFile", "useForInbound", "useForOutbound", "organization", "applications", "image", "clientOrganizations", "applicationQuota", "systemQuota", "backendBasepath", "remoteHost"}));
		mapper.registerModule(new SimpleModule().setSerializerModifier(new APIImportSerializerModifier(false)));
		mapper.setFilterProvider(filter);
		mapper.registerModule(new SimpleModule().setSerializerModifier(new PolicySerializerModifier(false)));
		HttpResponse httpResponse = null;
		translateMethodIds(api, api.getId(), METHOD_TRANSLATION.AS_ID);
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath()+"/proxies/"+api.getId()).build();
			entity = new StringEntity(mapper.writeValueAsString(api), ContentType.APPLICATION_JSON);
			
			RestAPICall request = new PUTRequest(entity, uri);
			httpResponse = request.execute();
			String response = EntityUtils.toString(httpResponse.getEntity());
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				LOG.error("Error updating API-Proxy. Response-Code: "+statusCode+". Got response: '"+response+"'");
				LOG.debug("Request sent:" + EntityUtils.toString(entity));
				throw new AppException("Error updating API-Proxy. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			API apiProxy =  mapper.readValue(response, API.class);
			return apiProxy;
		} catch (Exception e) {
			throw new AppException("Cannot update API-Proxy.", ErrorCode.CANT_UPDATE_API_PROXY, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public void deleteAPI(API api) throws AppException {
		APIStatusManager statusManager = new APIStatusManager();
		statusManager.update(api, API.STATE_DELETED, true);
	}
	
	public void deleteAPIProxy(API api) throws AppException {
		LOG.debug("Deleting API-Proxy");
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath()+"/proxies/"+api.getId()).build();
			
			RestAPICall request = new DELRequest(uri);
			httpResponse = request.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode != 204){
				LOG.error("Error deleting API-Proxy. Response-Code: "+statusCode + ", Response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
				throw new AppException("Error deleting API-Proxy. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			LOG.info("API: "+api.getName()+" "+api.getVersion()+" ("+api.getId()+")" + " successfully deleted");
		} catch (Exception e) {
			throw new AppException("Cannot delete API-Proxy.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public void deleteBackendAPI(API api) throws AppException {
		LOG.debug("Deleting API-Proxy");
		URI uri;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath()+"/apirepo/"+api.getApiId()).build();
			
			RestAPICall request = new DELRequest(uri);
			httpResponse = request.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode != 204){
				LOG.error("Error deleting Backend-API. Response-Code: "+statusCode + ", Response: '" + EntityUtils.toString(httpResponse.getEntity()) + "'");
				throw new AppException("Error deleting Backend-API. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
		} catch (Exception e) {
			throw new AppException("Cannot delete Backend-API.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public void publishAPI(API api, String vhost) throws AppException {
		if(API.STATE_PUBLISHED.equals(api.getState())) {
			LOG.info("API is already published");
			return;
		}
		updateAPIStatus(api, API.STATE_PUBLISHED, vhost);
	}
	
	public byte[] getAPIDatFile(API api, String password) throws AppException {
		URI uri;
		HttpResponse httpResponse = null;
		RestAPICall request;
		try {
			List <NameValuePair> parameters = new ArrayList <NameValuePair>();
			parameters.add(new BasicNameValuePair("filename", "api-export.dat"));
			parameters.add(new BasicNameValuePair("password", password));
			parameters.add(new BasicNameValuePair("id", api.getId()));
			HttpEntity entity = new UrlEncodedFormEntity(parameters);
			
			uri = new URIBuilder(cmd.getAPIManagerURL())
						.setPath(cmd.getApiBasepath()+"/proxies/export")
						.build();
			request = new POSTRequest(entity, uri);
			httpResponse = request.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode != 201){
				String response = EntityUtils.toString(httpResponse.getEntity());
				LOG.error("Error exporting DAT-File representation of API: "+api.getName()+" ("+api.getId()+"). Received Status-Code: " +statusCode+ ", Response: '" + response + "'");
				throw new AppException("Error exporting DAT-File representation of API: "+api.getName()+" ("+api.getId()+"). Received Status-Code: " +statusCode, ErrorCode.ERR_EXPORTING_API_DAT_FILE);
			} else {
				// The file can now be loaded from the returned Location header
				String locationHeader = httpResponse.getHeaders("Location")[0].getValue();
				uri = new URI(cmd.getAPIManagerURL() + locationHeader);
				request = new GETRequest(uri);
				httpResponse = request.execute();
				statusCode = httpResponse.getStatusLine().getStatusCode();
				if(statusCode != 200){
					String response = EntityUtils.toString(httpResponse.getEntity());
					LOG.error("Error getting DAT-File representation of API: "+api.getName()+" ("+api.getId()+"). Received Status-Code: " +statusCode+ ", Response: '" + response + "'");
					throw new AppException("Error getting DAT-File representation of API: "+api.getName()+" ("+api.getId()+"). Received Status-Code: " +statusCode, ErrorCode.ERR_EXPORTING_API_DAT_FILE);
				}
				return EntityUtils.toByteArray(httpResponse.getEntity());
			}
		} catch (Exception e) {
			throw new AppException("Cannot export API-DAT file.", ErrorCode.ERR_EXPORTING_API_DAT_FILE, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public void updateAPIStatus(API api, String desiredState, String vhost) throws AppException {
		LOG.debug("Update API-Proxy status to: " + api.getState());
		URI uri;
		HttpResponse httpResponse = null;
		RestAPICall request;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL())
				.setPath(cmd.getApiBasepath()+"/proxies/"+api.getId()+"/"+StatusEndpoint.valueOf(desiredState).endpoint)
				.build();
			if(vhost!=null && desiredState.equals(API.STATE_PUBLISHED)) { // During publish, it might be required to also set the VHost (See issue: #98)
				HttpEntity entity = new StringEntity("vhost="+vhost, ContentType.APPLICATION_FORM_URLENCODED);
				request = new POSTRequest(entity, uri, useAdminAccountForPublish());
			} else {
				HttpEntity entity = new StringEntity("", ContentType.APPLICATION_FORM_URLENCODED);
				request = new POSTRequest(entity, uri, useAdminAccountForPublish());
			}
			httpResponse = request.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode != 201 && statusCode != 200){ // See issue: #134 The API-Manager also returns 200 on this request
				String response = EntityUtils.toString(httpResponse.getEntity());
				if(statusCode == 403 &&  response.contains("API is already unpublished")) {
					LOG.warn("API: "+api.getName()+" "+api.getVersion()+" ("+api.getId()+") is already unpublished");
					return;
				}
				LOG.error("Error updating API status. Received Status-Code: " +statusCode+ ", Response: '" + response + "'");
				throw new AppException("Error updating API status. Received Status-Code: " +statusCode, ErrorCode.CANT_CREATE_BE_API);
			}
		} catch (Exception e) {
			throw new AppException("Cannot update API-Proxy status.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	private boolean useAdminAccountForPublish() throws AppException {
		if(APIManagerAdapter.hasAdminAccount()) return true;
		// This flag can be set to false to stop OrgAdmin from a Publishing request (means Pending approval)
		if(CoreParameters.getInstance().isAllowOrgAdminsToPublish()) return false;
		// In all other cases, we use the Admin-Account
		return true;
	}
	
	private String formatRetirementDate(Long retirementDate) {
		Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Z")));
		cal.setTimeInMillis(retirementDate);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Z")));
		return format.format(cal.getTime());
	}
	
	public void updateRetirementDate(API api, Long retirementDate) throws AppException {
		HttpResponse httpResponse = null;
		try {
			if(retirementDate==null || retirementDate==0) return;
			// Ignore the retirementDate if desiredState is not deprecated as it's used nowhere
			if(!api.getState().equals(API.STATE_DEPRECATED)) {
				LOG.info("Ignoring given retirementDate as API-Status is not set to deprecated");
				return;
			}
			URI uri = new URIBuilder(cmd.getAPIManagerURL())
					.setPath(cmd.getApiBasepath()+"/proxies/"+api.getId()+"/deprecate").build();
			RestAPICall apiCall = new POSTRequest(new StringEntity("retirementDate="+formatRetirementDate(retirementDate), ContentType.APPLICATION_FORM_URLENCODED), uri, true);
			httpResponse = apiCall.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String response = EntityUtils.toString(httpResponse.getEntity());
			if(statusCode != 201){
				LOG.error("Error updating retirement data of API. Received Status-Code: " +statusCode+ ", Response: '" + response + "'");
				throw new AppException("Error updating retirement data of API.", ErrorCode.CANT_CREATE_BE_API);
			}
		} catch (Exception e) {
			throw new AppException("Error while updating the retirementDate", ErrorCode.CANT_UPDATE_API_PROXY, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public API importBackendAPI(API api) throws AppException {
		LOG.debug("Import backend API: "+api.getName()+" based on "+api.getApiDefinition().getAPIDefinitionType().getNiceName()+" specification.");
		JsonNode jsonNode;
		try {
			if(api.getApiDefinition().getAPIDefinitionType()==APISpecType.WSDL_API) {
				jsonNode = importFromWSDL(api);
			} else {
				jsonNode =  importFromSwagger(api);
			}
			API createdAPI = new APIBaseDefinition();
			createdAPI.setApiId(jsonNode.get("id").asText());
			createdAPI.setName(jsonNode.get("name").asText());
			createdAPI.setCreatedOn(Long.parseLong(jsonNode.get("createdOn").asText()));
			return createdAPI;
		} catch (Exception e) {
			throw new AppException("Can't import definition / Create BE-API.", ErrorCode.CANT_CREATE_BE_API, e);
		}
	}
	
	private JsonNode importFromWSDL(API api) throws URISyntaxException, AppException, IOException {
		URI uri;
		HttpEntity entity = new StringEntity("", ContentType.APPLICATION_FORM_URLENCODED);
		String username=null;
		String pass=null;
		String wsdlUrl=null;
		String completeWsdlUrl=null;
		HttpResponse httpResponse = null;
		if(api.getApiDefinition().getApiSpecificationFile().endsWith(".url")) {
			completeWsdlUrl = Utils.getAPIDefinitionUriFromFile(api.getApiDefinition().getApiSpecificationFile());
		} else {
			completeWsdlUrl = api.getApiDefinition().getApiSpecificationFile();
		}
		wsdlUrl = extractURI(completeWsdlUrl);
		username=extractUsername(completeWsdlUrl);
		pass=extractPassword(completeWsdlUrl);

		try {
			URIBuilder uriBuilder = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath()+"/apirepo/importFromUrl/")
					.setParameter("organizationId", api.getOrganization().getId())
					.setParameter("type", "wsdl")
					.setParameter("url", wsdlUrl)
					.setParameter("name", api.getName());
			if (username!=null) {
				uriBuilder.setParameter("username", username);
				uriBuilder.setParameter("password", pass);
			}
			uri=uriBuilder.build();
			RestAPICall importWSDL = new POSTRequest(entity, uri);
			httpResponse = importWSDL.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String response = EntityUtils.toString(httpResponse.getEntity());
			if(statusCode != 201){
				LOG.error("Error importing WSDL. Received Status-Code: " +statusCode+ ", Response: '" + response + "'");
				throw new AppException("Can't import WSDL from URL / Create BE-API.", ErrorCode.CANT_CREATE_BE_API);
			}
			return mapper.readTree(response);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("Unexpected error creating Backend-API based on WSDL. Error message: " + e.getMessage(), ErrorCode.CANT_CREATE_BE_API, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}

	private JsonNode importFromSwagger(API api) throws URISyntaxException, AppException, IOException {
		URI uri;
		HttpEntity entity;
		HttpResponse httpResponse = null;
		if(APIManagerAdapter.hasAPIManagerVersion("7.6.2")) {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath()+"/apirepo/import/").build();
		} else {
			// Not sure, if 7.5.3 still needs it that way!
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath()+"/apirepo/import/")
					.setParameter("field", "name").setParameter("op", "eq").setParameter("value", "API Development").build();
		}
		try {
			entity = MultipartEntityBuilder.create()
					.addTextBody("name", api.getName(), ContentType.create("text/plain", StandardCharsets.UTF_8))
					.addTextBody("type", "swagger")
					.addBinaryBody("file", api.getApiDefinition().getApiSpecificationContent(), ContentType.create("application/json"), "filename")
					.addTextBody("fileName", "XYZ").addTextBody("organizationId", api.getOrganization().getId(), ContentType.create("text/plain", StandardCharsets.UTF_8))
					.addTextBody("integral", "false").addTextBody("uploadType", "html5").build();
			RestAPICall importSwagger = new POSTRequest(entity, uri);
			httpResponse = importSwagger.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String response = EntityUtils.toString(httpResponse.getEntity());
			if(statusCode != 201){
				LOG.error("Error importing API-Specification ("+api.getApiDefinition().getAPIDefinitionType().getNiceName()+") to create Backend-API. Received Status-Code: " +statusCode+ ", Response: '" + response + "'");
				throw new AppException("Can't import API-Specification to create Backend-API.", ErrorCode.CANT_CREATE_BE_API);
			}
			JsonNode jsonNode = mapper.readTree(response);
			return jsonNode;
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException("Unexpected error creating Backend-API based on API-Specification. Error message: " + e.getMessage(), ErrorCode.CANT_CREATE_BE_API, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public void upgradeAccessToNewerAPI(API apiToUpgradeAccess, API referenceAPI) throws AppException {
		upgradeAccessToNewerAPI(apiToUpgradeAccess, referenceAPI, null, null, null);
		// Existing applications now got access to the new API, hence we have to update the internal state
		// APIManagerAdapter.getInstance().addClientApplications(inTransitState, actualState);
		// Additionally we need to preserve existing (maybe manually created) application quotas
		URI uri;
		HttpEntity entity;
		RestAPICall request;
		HttpResponse httpResponse = null;
		boolean updateAppQuota = false;
		if(referenceAPI.getApplications().size()!=0) {
			LOG.debug("Found: "+referenceAPI.getApplications().size()+" subscribed applications for this API. Taking over potentially configured quota configuration.");
			for(ClientApplication app : referenceAPI.getApplications()) {
				if(app.getAppQuota()==null) continue;
				// REST-API for App-Quota is also returning Default-Quotas, but we have to ignore them here!
				if(app.getAppQuota().getId().equals(APIManagerAdapter.APPLICATION_DEFAULT_QUOTA) || app.getAppQuota().getId().equals(APIManagerAdapter.SYSTEM_API_QUOTA)) continue;
				for(QuotaRestriction restriction : app.getAppQuota().getRestrictions()) {
					if(restriction.getApiId().equals(referenceAPI.getId())) { // This application has a restriction for this specific API
						updateAppQuota = true;
						restriction.setApiId(apiToUpgradeAccess.getId()); // Take over the quota config to new API
						if(!restriction.getMethod().equals("*")) { // The restriction is for a specific method
							String originalMethodName = APIManagerAdapter.getInstance().methodAdapter.getMethodForId(referenceAPI.getId(), restriction.getMethod()).getName();
							// Try to find the same operation for the newly created API based on the name
							String newMethodId = APIManagerAdapter.getInstance().methodAdapter.getMethodForName(apiToUpgradeAccess.getId(), originalMethodName).getId();
							restriction.setMethod(newMethodId);
						}
					}
				}
				if(updateAppQuota) {
					LOG.info("Taking over existing quota config for application: '"+app.getName()+"' to newly created API.");
					try {
						uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath()+"/applications/"+app.getId()+"/quota").build();
						entity = new StringEntity(mapper.writeValueAsString(app.getAppQuota()), ContentType.APPLICATION_JSON);
						
						request = new PUTRequest(entity, uri, true);
						httpResponse = request.execute();
						int statusCode = httpResponse.getStatusLine().getStatusCode();
						if(statusCode < 200 || statusCode > 299){
							String response = EntityUtils.toString(httpResponse.getEntity());
							if((statusCode==404 || statusCode==400)) { // Status-Code 400 is returned by 7.7-20200331 ?!
								LOG.warn("Got unexpected error '" + response + " ("+statusCode+")' while taking over application quota to newer API ... Try again in "+cmd.getRetryDelay()+" milliseconds. (you may set -retryDelay <milliseconds>)");
								Thread.sleep(cmd.getRetryDelay());
								httpResponse = request.execute();
								statusCode = httpResponse.getStatusLine().getStatusCode();
								if(statusCode < 200 || statusCode > 299){
									LOG.error("Error taking over application quota to new API. Received Status-Code: " +statusCode + ", Response: " + response);
									throw new AppException("Error taking over application quota to new API. Received Status-Code: " +statusCode, ErrorCode.CANT_UPDATE_QUOTA_CONFIG);
								} else {
									LOG.info("Successfully took over application quota to newer API on retry. Received Status-Code: " +statusCode );
								}
							} else {
								LOG.error("Error taking over application quota to new API. Received Status-Code: " +statusCode + ", Response: " + response);
								throw new AppException("Error taking over application quota to new API. Received Status-Code: " +statusCode, ErrorCode.CANT_UPDATE_QUOTA_CONFIG);					
							}
						}
					} catch (Exception e) {
						throw new AppException("Can't update application quota. Error message: " + e.getMessage(), ErrorCode.CANT_UPDATE_QUOTA_CONFIG, e);
					} finally {
						try {
							if(httpResponse!=null) 
								((CloseableHttpResponse)httpResponse).close();
						} catch (Exception ignore) {}
					}
				}
			}
		}
	}
	
	public boolean upgradeAccessToNewerAPI(API apiToUpgradeAccess, API referenceAPI, Boolean deprecateRefApi, Boolean retireRefApi, Long retirementDateRefAPI) throws AppException {
		if(apiToUpgradeAccess.getState().equals(API.STATE_UNPUBLISHED)) {
			LOG.info("API to upgrade access has state unpublished.");
			return false;
		}
		if(apiToUpgradeAccess.getId().equals(referenceAPI.getId())) {
			LOG.warn("API to upgrade access: "+Utils.getAPILogString(apiToUpgradeAccess)+" and "
					+ "reference/old API: "+Utils.getAPILogString(referenceAPI)+" are the same. Skip upgrade access to newer API.");
			return false;
		}
		LOG.debug("Upgrade access & subscriptions to API: " + apiToUpgradeAccess.getName() + " " + apiToUpgradeAccess.getVersion() + " ("+apiToUpgradeAccess.getId()+")");
		
		URI uri;
		HttpEntity entity;
		RestAPICall request;
		HttpResponse httpResponse = null;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath()+"/proxies/upgrade/"+referenceAPI.getId()).build();
			
			List<NameValuePair> params = new Vector<NameValuePair>();
			params.add(new BasicNameValuePair("upgradeApiId", apiToUpgradeAccess.getId()));
			if(deprecateRefApi != null) 		params.add(new BasicNameValuePair("deprecate", deprecateRefApi.toString()));
			if(retireRefApi != null) 			params.add(new BasicNameValuePair("retire", retireRefApi.toString()));
			if(retirementDateRefAPI != null)	params.add(new BasicNameValuePair("retirementDate", formatRetirementDate(retirementDateRefAPI)));
			
			entity = new UrlEncodedFormEntity (params, "UTF-8");
			
			request = new POSTRequest(entity, uri, true);
			
			httpResponse = request.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode != 204){
				String response = EntityUtils.toString(httpResponse.getEntity());
				if((statusCode==403 || statusCode==404) && (response.contains("Unknown API") || response.contains("The entity could not be found")) ) {
					LOG.warn("Got unexpected error: 'Unknown API' while granting access to newer API ... Try again in "+cmd.getRetryDelay()+" milliseconds. (you may set -retryDelay <milliseconds>)");
					Thread.sleep(cmd.getRetryDelay());
					httpResponse = request.execute();
					statusCode = httpResponse.getStatusLine().getStatusCode();
					if(statusCode != 204) {
						LOG.error("Error upgrading access to newer API. Received Status-Code: " +statusCode + ", Response: " + EntityUtils.toString(httpResponse.getEntity()));
						throw new AppException("Error upgrading access to newer API. Received Status-Code: " +statusCode, ErrorCode.CANT_CREATE_BE_API);
					} else {
						LOG.info("Successfully granted access to newer API on retry. Received Status-Code: " +statusCode );
					}
				} else {
					LOG.error("Error upgrading access to newer API. Received Status-Code: " +statusCode + ", Response: " + response);
					throw new AppException("Error upgrading access to newer API. Received Status-Code: " +statusCode, ErrorCode.CANT_CREATE_BE_API);					
				}
			}
			return true;
		} catch (Exception e) {
			throw new AppException("Can't upgrade access to newer API!", ErrorCode.CANT_UPGRADE_API_ACCESS, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public void grantClientOrganization(List<Organization> grantAccessToOrgs, API api, boolean allOrgs) throws AppException {
		URI uri;
		HttpEntity entity;
		RestAPICall apiCall;
		HttpResponse httpResponse = null;
		String formBody;
		if(allOrgs) {
			formBody = "action=all_orgs&apiId="+api.getId();
		} else {
			formBody = "action=orgs&apiId="+api.getId();
			for(Organization org : grantAccessToOrgs) {
				formBody += "&grantOrgId="+org.getId();
			}
		}
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath()+"/proxies/grantaccess").build();			
			entity = new StringEntity(formBody, ContentType.APPLICATION_FORM_URLENCODED);
			apiCall = new POSTRequest(entity, uri, true);
			httpResponse = apiCall.execute();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode != 204){
				String response = EntityUtils.toString(httpResponse.getEntity());
				if((statusCode==403 || statusCode==404) && response.contains("Unknown API")) {
					LOG.warn("Got unexpected error: 'Unknown API' while creating API-Access ... Try again in "+cmd.getRetryDelay()+" milliseconds. (you may set -retryDelay <milliseconds>)");
					Thread.sleep(cmd.getRetryDelay());
					httpResponse = apiCall.execute();
					statusCode = httpResponse.getStatusLine().getStatusCode();
					if(statusCode != 204) {
						LOG.error("Error granting access to API: '"+api.getName()+"' (ID: "+api.getId()+"). Received Status-Code: " +statusCode + ", Response: " + response);
						throw new AppException("Error granting API access. Received Status-Code: " +statusCode, ErrorCode.API_MANAGER_COMMUNICATION);
					} else {
						LOG.info("Successfully created API-Access on retry. Received Status-Code: " +statusCode );
					}
				}
			}
			// Update the actual state to reflect, which organizations now really have access to the API (this also includes prev. added orgs)
			if(api.getClientOrganizations()==null) api.setClientOrganizations(new ArrayList<Organization>());
			api.getClientOrganizations().addAll(grantAccessToOrgs);
		} catch (Exception e) {
			LOG.error("grantAccessToOrgs: '"+grantAccessToOrgs+"'");
			LOG.error("allOrgs: '"+allOrgs+"'");
			throw new AppException("Can't grant access to organization.", ErrorCode.ACCESS_ORGANIZATION_ERR, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	private String getFilterFields(APIFilter filter) {
		String filterFields = "[";
		if(LOG.isDebugEnabled()) {
			if(filter.getApiPath()!=null) filterFields += "apiPath=" + filter.getApiPath();
			if(filter.getVhost()!=null) filterFields += " vHost=" + filter.getVhost();
			if(filter.getQueryStringVersion()!=null) filterFields += " queryString=" + filter.getQueryStringVersion();
		} else if (LOG.isTraceEnabled()) {
			filterFields += " filter=" + filter;
		}
		filterFields += "]";
		return filterFields;
	}
	
	private String extractUsername(String url) {
		String[] temp = url.split("@");
		if(temp.length==2) {
			return temp[0].substring(0, temp[0].indexOf("/"));
		}
		return null;
	}
	
	private String extractPassword(String url) {
		String[] temp = url.split("@");
		if(temp.length==2) {
			return temp[0].substring(temp[0].indexOf("/")+1);
		}
		return null;
	}
	
	private String extractURI(String url) throws AppException
	{
		String[] temp = url.split("@");
		if(temp.length==1) {
			return temp[0];
		} else if(temp.length==2) {
			return temp[1];
		} else {
			throw new AppException("WSDL-URL has an invalid format. ", ErrorCode.CANT_READ_WSDL_FILE);
		}
	}
	
	public APIManagerAPIAdapter setAPIManagerResponse(APIFilter filter, String apiManagerResponse) {
		this.apiManagerResponse.put(filter, apiManagerResponse);
		return this;
	}
	
	public APIManagerAPIAdapter setAPIManagerResponse(String apiId, Image image) {
		this.imagesResponse.put(apiId, image);
		return this;
	}
}
