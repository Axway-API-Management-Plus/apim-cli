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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter.Builder.APIType;
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
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.Utils;
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
				addOriginalAPIDefinitionFromAPIM(api, filter.isIncludeOriginalAPIDefinition());
				addImageFromAPIM(api, filter.isIncludeImage());
				if(logProgress && apis.size()>5) Utils.progressPercentage(i, apis.size(), "Initializing APIs");
			}
			addCustomProperties(apis, filter);
			if(logProgress && apis.size()>5) System.out.print("\n");
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
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				if(statusCode == 403 && filter.getId()!=null) {
					ErrorState.getInstance().setError("Unable to find API with ID: "+filter.getId()+". Please have in mind during API-Update the ID is re-created!", ErrorCode.UNKNOWN_API, false);
					apiManagerResponse.put(filter, "[]");
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

	private List<API> filterAPIs(APIFilter filter) throws AppException, JsonParseException, JsonMappingException, IOException {
		List<API> apis = mapper.readValue(this.apiManagerResponse.get(filter), new TypeReference<List<API>>(){});
		List<API> foundAPIs = new ArrayList<API>();
		if(filter.getApiPath()==null && filter.getVhost()==null && filter.getQueryStringVersion()==null && apis.size()==1 && filter.getPolicyName()==null) {
			return apis;
		}
		for(API api : apis) {
			if(!filter.filter(api)) continue; 
			foundAPIs.add(api);
		}
		if(foundAPIs.size()!=0) {
			String dbgCrit = "";
			if(foundAPIs.size()>1) 
				dbgCrit = " (apiPath: '"+filter.getApiPath()+"', filter: "+filter+", vhost: '"+filter.getVhost()+"', requestedType: "+filter.getApiType()+")";
			LOG.debug("Found: "+foundAPIs.size()+" exposed API(s)" + dbgCrit);
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
		if(!includeImage) return;
		Image image = new Image();
			image = new Image();
			URI uri;
			HttpResponse httpResponse = null;
			try {
				uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/proxies/"+api.getId()+"/image").build();
				RestAPICall getRequest = new GETRequest(uri, null);
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
	
	private void addQuotaConfiguration(API api, boolean addQuota) throws AppException {
		if(!addQuota || !APIManagerAdapter.hasAdminAccount()) return;
		APIQuota applicationQuota = null;
		APIQuota sytemQuota = null;
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
	
	private void addExistingClientAppQuotas(API api, boolean addQuota) throws AppException {
		if(!addQuota || !APIManagerAdapter.hasAdminAccount()) return;
		if(api.getApplications()==null || api.getApplications().size()==0) return;
		for(ClientApplication app : api.getApplications()) {
			APIQuota appQuota = apim.quotaAdapter.getQuotaForAPI(app.getId(), null);
			app.setAppQuota(appQuota);
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
	
	
	
	private void addClientOrganizations(API api, boolean addClientOrganizations) throws AppException {
		if(!addClientOrganizations || !APIManagerAdapter.hasAdminAccount()) return;
		List<Organization> grantedOrgs;
		List<Organization> allOrgs = apim.orgAdapter.getAllOrgs();
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
	
	public void addClientApplications(API api, APIFilter filter) throws AppException {
		if(!filter.isIncludeClientApplications()) return;
		List<ClientApplication> existingClientApps = new ArrayList<ClientApplication>();
		List<ClientApplication> apps = null;
		// With version >7.7 we can retrieve the subscribed apps directly
		if(APIManagerAdapter.hasAPIManagerVersion("7.7")) {
			apps = apim.appAdapter.getAppsSubscribedWithAPI(api.getId());
			api.setApplications(apps);
		} else {
			apps = apim.appAdapter.getApplications(new ClientAppFilter.Builder()
					.includeQuotas(filter.isIncludeClientAppQuota())
					.build(), false);
			for(ClientApplication app : apps) {
				List<APIAccess> APIAccess = apim.accessAdapter.getAPIAccess(app.getId(), APIManagerAPIAccessAdapter.Type.applications, true);
				app.setApiAccess(APIAccess);
				for(APIAccess access : APIAccess) {
					if(access.getApiId().equals(api.getId())) {
						existingClientApps.add(app);
					}
				}
				api.setApplications(existingClientApps);
			}
		}		
	}
	
	private static void addOriginalAPIDefinitionFromAPIM(API api, boolean includeOriginalAPIDefinition) throws AppException {
		if(!includeOriginalAPIDefinition) return;
		URI uri;
		APISpecification apiDefinition;
		HttpResponse httpResponse = null;
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
