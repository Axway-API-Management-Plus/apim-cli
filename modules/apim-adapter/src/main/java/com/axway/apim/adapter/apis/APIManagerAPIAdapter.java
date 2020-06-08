package com.axway.apim.adapter.apis;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter.METHOD_TRANSLATION;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.definition.APISpecification;
import com.axway.apim.api.definition.APISpecificationFactory;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.Profile;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerAPIAdapter extends APIAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerAPIAdapter.class);

	Map<APIFilter, String> apiManagerResponse = new HashMap<APIFilter, String>();
	
	Map<String, Image> imagesResponse = new HashMap<String, Image>();
	
	ObjectMapper mapper = new ObjectMapper();
	
	CommandParameters cmd = CommandParameters.getInstance();
	
	APIManagerAdapter apim; 

	APIManagerAPIAdapter() throws AppException {
	}
	
	@Override
	public boolean readConfig(Object config) throws AppException {
		if(config instanceof APIManagerAdapter) {
			this.apim = (APIManagerAdapter)config;
			return true;
		}
		return false;
	}

	@Override
	public List<API> getAPIs(APIFilter filter, boolean logMessage) throws AppException {
		List<API> apis = new ArrayList<API>();
		try {
			_readAPIsFromAPIManager(filter);
			apis = filterAPIs(filter, logMessage);
			translateMethodIds(apis, filter.getTranslateMethodMode());
			addQuotaConfiguration(apis, filter.isIncludeQuotas());
			addClientOrganizations(apis, filter.isIncludeClientOrganizations());
			addClientApplications(apis, filter.isIncludeClientApplications());
			addExistingClientAppQuotas(apis, filter.isIncludeQuotas());
			addCustomProperties(apis, filter);
			addOriginalAPIDefinitionFromAPIM(apis, filter.isIncludeOriginalAPIDefinition());
			addImageFromAPIM(apis, filter.isIncludeImage()); 
		} catch (IOException e) {
			throw new AppException("Cant reads API from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		return apis;
	}
	
	public API getAPI(APIFilter filter, boolean logMessage) throws AppException {
		List<API> foundAPIs = getAPIs(filter, logMessage);
		return uniqueAPI(foundAPIs);
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
			RestAPICall getRequest = new GETRequest(uri, null);
			httpResponse = getRequest.execute();
			String response = EntityUtils.toString(httpResponse.getEntity());
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
		URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/"+filter.getApiType() + requestedId)
				.addParameters(filter.getFilters())
				.build();
		return uri;
	}
	
	private API uniqueAPI(List<API> foundAPIs) throws AppException {
		if(foundAPIs.size()>1) {
			throw new AppException("No unique API found", ErrorCode.UNKNOWN_API);
		}
		if(foundAPIs.size()==0) return null;
		return foundAPIs.get(0);
	}

	private List<API> filterAPIs(APIFilter filter, boolean logMessage) throws AppException, JsonParseException, JsonMappingException, IOException {
		List<API> apis = mapper.readValue(this.apiManagerResponse.get(filter), new TypeReference<List<API>>(){});
		List<API> foundAPIs = new ArrayList<API>();
		if(filter.getApiPath()==null && filter.getVhost()==null && filter.getQueryStringVersion()==null && apis.size()==1) {
			return apis;
		}
			for(API api : apis) {
				if(filter.getApiPath()==null && filter.getVhost()==null && filter.getQueryStringVersion()==null) { // Nothing given to filter out.
					foundAPIs.add(api);
					continue;
				}
				// Before 7.7, we have to filter out APIs manually!
				if(!APIManagerAdapter.hasAPIManagerVersion("7.7")) {
					if(filter.getApiPath().contains("*")) {
						Pattern pattern = Pattern.compile(filter.getApiPath().replace("*", ".*"));
						Matcher matcher = pattern.matcher(api.getPath());
						if(!matcher.matches()) {
							continue;
						}
					} else {
						if(filter.getApiPath()!=null && !filter.getApiPath().equals(api.getPath())) continue;
					}
				}
				if(filter.getApiType().equals(APIManagerAdapter.TYPE_FRONT_END)) {
					if(filter.getVhost()!=null && !filter.getVhost().equals(api.getVhost())) continue;
					if(filter.getQueryStringVersion()!=null && !filter.getQueryStringVersion().equals(api.getApiRoutingKey())) continue;
				}
				foundAPIs.add(api);
			}
			if(foundAPIs.size()!=0) {
				String dbgCrit = "";
				if(foundAPIs.size()>1) 
					dbgCrit = " (apiPath: '"+filter.getApiPath()+"', filter: "+filter+", vhost: '"+filter.getVhost()+"', requestedType: "+filter.getApiType()+")";
				LOG.info("Found: "+foundAPIs.size()+" exposed API(s)" + dbgCrit);
				return foundAPIs;
			}
			LOG.info("No existing API found based on filter: " + getFilterFields(filter));
			return foundAPIs;
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
	 * @param <profile> An Outbound- or InboundProfile
	 * @param apis in which the methods should be translated
	 * @param mode translation direction 
	 * @throws AppException
	 */
	public <profile> void translateMethodIds(List<API> apis, METHOD_TRANSLATION mode) throws AppException {
		if(mode == METHOD_TRANSLATION.NONE) return;
		for(API api : apis) {
			if(api.getOutboundProfiles()!=null) _translateMethodIds(api.getOutboundProfiles(), mode, Arrays.asList(api.getId()));
			if(api.getInboundProfiles()!=null) _translateMethodIds(api.getInboundProfiles(), mode, Arrays.asList(api.getId()));
		}
	}
	
	public <profile> void translateMethodIds(List<API> apis, List<String> apiIds, METHOD_TRANSLATION mode) throws AppException {
		if(mode == METHOD_TRANSLATION.NONE) return; 
		for(API api : apis) {
			if(api.getOutboundProfiles()!=null) _translateMethodIds(api.getOutboundProfiles(), mode, apiIds);
			if(api.getInboundProfiles()!=null) _translateMethodIds(api.getInboundProfiles(), mode, apiIds);
		}
	}
	
	private void addImageFromAPIM(List<API> apis, boolean includeImage) throws AppException {
		if(!includeImage) return;
		Image image = new Image();
		for(API api : apis) {
			image = new Image();
			URI uri;
			HttpResponse httpResponse = null;
			try {
				uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/proxies/"+api.getId()+"/image").build();
				RestAPICall getRequest = new GETRequest(uri, null);
				httpResponse = getRequest.execute();
				if(httpResponse == null || httpResponse.getEntity() == null || httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
					api.setImage(null);
					continue; // no Image found in API-Manager
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
	}
	
	private <ProfileType> void _translateMethodIds(Map<String, ProfileType> profiles, METHOD_TRANSLATION mode, List<String> apiIds) throws AppException {
		Map<String, ProfileType> updatedEntries = new LinkedHashMap<String, ProfileType>();
		
		if(profiles!=null) {
			//List<APIMethod> methods = apim.methodAdapter.getAllMethodsForAPI(apiId);
			Iterator<String> keys = profiles.keySet().iterator();
			while(keys.hasNext()) {
				String key = keys.next();
				if(key.equals("_default")) continue;
				APIMethod method = null;
				for(String apiId : apiIds) {
					if(mode==METHOD_TRANSLATION.AS_NAME) {
						method = apim.methodAdapter.getMethodForId(apiId, key);
					} else {
						method = apim.methodAdapter.getMethodForName(apiId, key);
					}
					if(method!=null) break;
				}
				ProfileType profileWithType = profiles.get(key);
				Profile profile = (Profile)profileWithType;
				profile.setApiMethodId(method.getId());
				profile.setApiMethodName(method.getName());
				profile.setApiId(method.getVirtualizedApiId());
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
	
	private void addQuotaConfiguration(List<API> apis, boolean addQuota) throws AppException {
		if(!addQuota || !APIManagerAdapter.hasAdminAccount()) return;
		APIQuota applicationQuota = null;
		APIQuota sytemQuota = null;
		for(API api : apis) {			
			try {
				applicationQuota = apim.quotaAdapter.getQuotaForAPI(APIManagerQuotaAdapter.Quota.APPLICATION_DEFAULT.getQuotaId(), api.getId()); // Get the Application-Default-Quota
				sytemQuota = apim.quotaAdapter.getQuotaForAPI(APIManagerQuotaAdapter.Quota.SYSTEM_DEFAULT.getQuotaId(), api.getId()); // Get the Application-Default-QuotagetQuotaFromAPIManager(); // Get the System-Default-Quota
				api.setApplicationQuota(applicationQuota);
				api.setSystemQuota(sytemQuota);
			} catch (AppException e) {
				LOG.error("Application-Default quota response: '"+applicationQuota+"'");
				LOG.error("System-Default quota response: '"+sytemQuota+"'");
				throw e;
			}
		}
	}
	
	private void addExistingClientAppQuotas(List<API> apis, boolean addQuota) throws AppException {
		if(!addQuota || !APIManagerAdapter.hasAdminAccount()) return;
		for(API api : apis) {
			if(api.getApplications()==null || api.getApplications().size()==0) return;
			for(ClientApplication app : api.getApplications()) {
				APIQuota appQuota = apim.quotaAdapter.getQuotaForAPI(app.getId(), null);
				app.setAppQuota(appQuota);
			}
		}
	}
	
	private void addCustomProperties(List<API> apis, APIFilter filter) throws IOException {
		if(filter.getCustomProperties() == null) return;
		Map<String, String> customProperties = new LinkedHashMap<String, String>();
		Iterator<String> it = filter.getCustomProperties().keySet().iterator();
		Map<String, JsonNode> apiAsJsonMappedWithId = new HashMap<String, JsonNode>();
		JsonNode jsonPayload = mapper.readTree(this.apiManagerResponse.get(filter));
		// Create a map for each API containing the JSON-Payload
		for(JsonNode node : jsonPayload) {
			String apiId = node.get("id").asText();
			apiAsJsonMappedWithId.put(apiId, node);
		}
		for(API api : apis) {
			JsonNode node = apiAsJsonMappedWithId.get(api.getId());
			while(it.hasNext()) {
				String customPropKey = it.next();
				JsonNode value = node.get(customPropKey);
				String customPropValue = (value == null) ? null : value.asText();
				customProperties.put(customPropKey, customPropValue);
			}
			api.setCustomProperties(customProperties);
		}
	}
	
	
	
	private void addClientOrganizations(List<API> apis, boolean addClientOrganizations) throws AppException {
		if(!addClientOrganizations || !APIManagerAdapter.hasAdminAccount()) return;
		/*if(desiredAPI.getState().equals(IAPI.STATE_UNPUBLISHED)) {
			LOG.info("Ignoring Client-Organizations, as desired API-State is Unpublished!");
			return;
		}
		if(desiredAPI.getClientOrganizations()==null && desiredAPI.getApplications()==null 
				&& CommandParameters.getInstance().getClientOrgsMode().equals(CommandParameters.MODE_REPLACE)) return;*/
		List<Organization> grantedOrgs;
		List<Organization> allOrgs = apim.orgAdapter.getAllOrgs();
		for(API api : apis) {
			grantedOrgs = new ArrayList<Organization>();
			for(Organization org : allOrgs) {
				List<APIAccess> orgAPIAccess = apim.accessAdapter.getAPIAccess(org.getId(), APIManagerAPIAccessAdapter.Type.organizations);
				for(APIAccess access : orgAPIAccess) {
					if(access.getApiId().equals(api.getId())) {
						grantedOrgs.add(org);
					}
				}
			}
			api.setClientOrganizations(grantedOrgs);
		}
	}
	
	public void addClientApplications(List<API> apis, boolean addClientApplication) throws AppException {
		if(!addClientApplication) return;
		List<ClientApplication> existingClientApps = new ArrayList<ClientApplication>();
		List<ClientApplication> apps = null;
		// With version >7.7 we can retrieve the subscribed apps directly
		if(APIManagerAdapter.hasAPIManagerVersion("7.7")) {
			for(API api : apis) {
				apps = apim.appAdapter.getAppsSubscribedWithAPI(api.getId());
				api.setApplications(apps);
			}
		} else {
			apps = apim.appAdapter.getApplications(new ClientAppFilter.Builder().build());
			for(ClientApplication app : apps) {
				List<APIAccess> APIAccess = apim.accessAdapter.getAPIAccess(app.getId(), APIManagerAPIAccessAdapter.Type.applications);
				app.setApiAccess(APIAccess);
				for(API api : apis) {
					for(APIAccess access : APIAccess) {
						if(access.getApiId().equals(api.getId())) {
							existingClientApps.add(app);
						}
					}
					api.setApplications(existingClientApps);
				}
			}
		}		
	}
	
	private static void addOriginalAPIDefinitionFromAPIM(List<API> apis, boolean includeOriginalAPIDefinition) throws AppException {
		if(!includeOriginalAPIDefinition) return;
		URI uri;
		APISpecification apiDefinition;
		HttpResponse httpResponse = null;
		for(API api : apis) {
			try {
				uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/apirepo/"+api.getApiId()+"/download")
						.setParameter("original", "true").build();
				RestAPICall getRequest = new GETRequest(uri, null);
				httpResponse=getRequest.execute();
				String res = EntityUtils.toString(httpResponse.getEntity(),StandardCharsets.UTF_8);
				String origFilename = "Unkown filename";
				if(httpResponse.containsHeader("Content-Disposition")) {
					origFilename = httpResponse.getHeaders("Content-Disposition")[0].getValue();
				}
				apiDefinition = APISpecificationFactory.getAPISpecification(res.getBytes(StandardCharsets.UTF_8), origFilename.substring(origFilename.indexOf("filename=")+9), null);
				api.setAPIDefinition(apiDefinition);
			} catch (Exception e) {
				throw new AppException("Can't read Swagger-File.", ErrorCode.CANT_READ_API_DEFINITION_FILE, e);
			} finally {
				try {
					if(httpResponse!=null) 
						((CloseableHttpResponse)httpResponse).close();
				} catch (Exception ignore) {}
			}
		}
	}
	
	private String getFilterFields(APIFilter filter) {
		String filterFields = "[";
		if(filter.getApiPath()!=null) filterFields += "apiPath=" + filter.getApiPath();
		if(filter.getVhost()!=null) filterFields += " vHost=" + filter.getVhost();
		if(filter.getQueryStringVersion()!=null) filterFields += " queryString=" + filter.getQueryStringVersion();
		if(filter!=null) filterFields += " filter=" + filter;
		filterFields += "]";
		return filterFields;
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
