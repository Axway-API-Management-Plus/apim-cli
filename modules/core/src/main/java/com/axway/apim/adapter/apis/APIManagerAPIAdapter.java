package com.axway.apim.adapter.apis;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.definition.APISpecification;
import com.axway.apim.api.definition.APISpecificationFactory;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerAPIAdapter extends APIAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerAPIAdapter.class);

	String apiManagerResponse;
	
	ObjectMapper mapper = new ObjectMapper();
	
	CommandParameters params = CommandParameters.getInstance();
	
	APIManagerAdapter apim = APIManagerAdapter.getInstance(); 

	public APIManagerAPIAdapter() throws AppException {

	}
	
	@Override
	public boolean readConfig(Object config) throws AppException {
		if(config instanceof APIManagerAdapter && CommandParameters.getInstance()!=null) return true;
		return false;
	}

	@Override
	public List<API> getAPIs(APIFilter filter, boolean logMessage) throws AppException {
		List<API> apis = new ArrayList<API>();
		try {
			_readAPIsFromAPIManager(filter);
			apis = filterAPIs(filter, logMessage);
			translateMethodIds(apis, filter.getTranslateMethodMode());
			translatePolicies(apis, filter.getTranslatePolicyMode());
			addQuotaConfiguration(apis, filter.isIncludeQuotas());
			addClientOrganizations(apis, filter.isIncludeClientOrganizations());
			addClientApplications(apis, filter.isIncludeClientApplications());
			addExistingClientAppQuotas(apis, filter.isIncludeQuotas());
			addOriginalAPIDefinitionFromAPIM(apis, filter.isIncludeOriginalAPIDefinition());
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
		if(this.apiManagerResponse!=null) return;
		CommandParameters cmd = CommandParameters.getInstance();
		URI uri;
		try { 
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/"+filter.getApiType())
					.addParameters(filter.getFilters())
					.build();
			LOG.info("Sending request to find existing APIs: " + uri);
			RestAPICall getRequest = new GETRequest(uri, null);
			HttpResponse response = getRequest.execute();

			apiManagerResponse = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	private API uniqueAPI(List<API> foundAPIs) throws AppException {
		if(foundAPIs.size()>1) {
			throw new AppException("No unique API found", ErrorCode.UNKNOWN_API);
		}
		if(foundAPIs.size()==0) return null;
		return foundAPIs.get(0);
	}

	private List<API> filterAPIs(APIFilter filter, boolean logMessage) throws AppException, JsonParseException, JsonMappingException, IOException {
		List<API> apis = mapper.readValue(this.apiManagerResponse, new TypeReference<List<API>>(){});
		List<API> foundAPIs = new ArrayList<API>();
		if(filter.getApiPath()==null && filter.getVhost()==null && filter.getQueryStringVersion()==null && apis.size()==1) {
			return apis;
		}
			for(API api : apis) {
				if(filter.getApiPath()==null && filter.getVhost()==null && filter.getQueryStringVersion()==null) { // Nothing given to filter out.
					foundAPIs.add(api);
					continue;
				}
				if(filter.getApiPath()!=null && !filter.getApiPath().equals(api.getPath())) continue;
				if(filter.getApiType().equals(APIManagerAdapter.TYPE_FRONT_END)) {
					if(filter.getVhost()!=null && !filter.getVhost().equals(api.getVhost())) continue;
					if(filter.getQueryStringVersion()!=null && !filter.getQueryStringVersion().equals(api.getApiRoutingKey())) continue;
				}
				if(filter.getApiType().equals(APIManagerAdapter.TYPE_BACK_END)) {
					if(logMessage) 
						LOG.info("Found existing Backend-API with name: '"+api.getName()+"' (ID: '" + api.getId()+"')");														
				} else {
					if(logMessage)
						LOG.info("Found existing API on path: '"+api.getPath()+"' ("+api.getState()+") (ID: '" + api.getId()+"')");
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
	
	public <profile> void translateMethodIds(List<API> apis, int mode) throws AppException {
		if(mode == APIFilter.NO_TRANSLATION) return; 
		for(API api : apis) {
			if(api.getOutboundProfiles()!=null) _translateMethodIds(api.getOutboundProfiles(), mode, api.getId());
			if(api.getInboundProfiles()!=null) _translateMethodIds(api.getInboundProfiles(), mode, api.getId());
		}
	}
	
	private <profile> void _translateMethodIds(Map<String, profile> profiles, int mode, String apiId) throws AppException {
		Map<String, profile> updatedEntries = new LinkedHashMap<String, profile>();
		
		if(profiles!=null) {
			List<APIMethod> methods = apim.methodAdapter.getAllMethodsForAPI(apiId);
			Iterator<String> keys = profiles.keySet().iterator();
			while(keys.hasNext()) {
				String key = keys.next();
				if(key.equals("_default")) continue;
				for(APIMethod method : methods) {
					if(mode==APIFilter.METHODS_AS_NAME) {
						if(method.getId().equals(key)) { // Look for the methodId
							profile value = profiles.get(key);
							if(value instanceof OutboundProfile) {
								((OutboundProfile)value).setApiMethodId(method.getName()); // Put the name as the ID!
								((OutboundProfile)value).setApiId(method.getApiId());
							}
							updatedEntries.put(method.getName(), value);
							keys.remove();
							break;
						}						
					} else {
						if(method.getName().equals(key)) {
							profile value = profiles.get(key);
							if(value instanceof OutboundProfile) {
								((OutboundProfile)value).setApiMethodId(method.getApiMethodId());
								((OutboundProfile)value).setApiId(method.getApiId());
							}
							updatedEntries.put(method.getId(), profiles.get(key));
							keys.remove();
							break;
						}
					}
				}
			}
			profiles.putAll(updatedEntries);
		}
	}
	
	public <profile> void translatePolicies(List<API> apis, int mode) throws AppException {
		if(mode == APIFilter.NO_TRANSLATION) return; 
		for(API api : apis) {
			if(api.getOutboundProfiles()!=null) {
				Iterator<OutboundProfile> it = api.getOutboundProfiles().values().iterator();
				while(it.hasNext()) {
					OutboundProfile profile = it.next();
					if(mode == APIFilter.TO_INTERNAL_POLICY_NAME) {
						profile.setRequestPolicy(apim.policiesAdapter.getPolicyKey(profile.getRequestPolicy(), APIManagerPoliciesAdapter.REQUEST));
						profile.setRoutePolicy(apim.policiesAdapter.getPolicyKey(profile.getRoutePolicy(), APIManagerPoliciesAdapter.ROUTING));
						profile.setResponsePolicy(apim.policiesAdapter.getPolicyKey(profile.getResponsePolicy(), APIManagerPoliciesAdapter.RESPONSE));
						profile.setFaultHandlerPolicy(apim.policiesAdapter.getPolicyKey(profile.getFaultHandlerPolicy(), APIManagerPoliciesAdapter.FAULT_HANDLER));						
					} else {
						profile.setRequestPolicy(apim.policiesAdapter.getPolicyName(profile.getRequestPolicy(), APIManagerPoliciesAdapter.REQUEST));
						profile.setRoutePolicy(apim.policiesAdapter.getPolicyName(profile.getRoutePolicy(), APIManagerPoliciesAdapter.ROUTING));
						profile.setResponsePolicy(apim.policiesAdapter.getPolicyName(profile.getResponsePolicy(), APIManagerPoliciesAdapter.RESPONSE));
						profile.setFaultHandlerPolicy(apim.policiesAdapter.getPolicyName(profile.getFaultHandlerPolicy(), APIManagerPoliciesAdapter.FAULT_HANDLER));
					}
				}
			}
		}
	}
	
	private void addQuotaConfiguration(List<API> apis, boolean addQuota) throws AppException {
		if(!addQuota || !APIManagerAdapter.hasAdminAccount()) return;
		APIQuota applicationQuota = null;
		APIQuota sytemQuota = null;
		// No need to load quota, if not given in the desired API
		//if(desiredAPI!=null && (desiredAPI.getApplicationQuota() == null && desiredAPI.getSystemQuota() == null)) return;
		for(API api : apis) {			
			try {
				applicationQuota = apim.quotaAdapter.getQuotaForAPI(APIManagerQuotaAdapter.APPLICATION_DEFAULT_QUOTA, api.getId()); // Get the Application-Default-Quota
				sytemQuota = apim.quotaAdapter.getQuotaForAPI(APIManagerQuotaAdapter.SYSTEM_API_QUOTA, api.getId()); // Get the Application-Default-QuotagetQuotaFromAPIManager(); // Get the System-Default-Quota
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
	
	private void addClientOrganizations(List<API> apis, boolean addClientOrganizations) throws AppException {
		if(!addClientOrganizations || !APIManagerAdapter.hasAdminAccount()) return;
		/*if(desiredAPI.getState().equals(IAPI.STATE_UNPUBLISHED)) {
			LOG.info("Ignoring Client-Organizations, as desired API-State is Unpublished!");
			return;
		}
		if(desiredAPI.getClientOrganizations()==null && desiredAPI.getApplications()==null 
				&& CommandParameters.getInstance().getClientOrgsMode().equals(CommandParameters.MODE_REPLACE)) return;*/
		List<Organization> grantedOrgs = new ArrayList<Organization>();
		List<Organization> allOrgs = apim.orgAdapter.getAllOrgs();
		for(Organization org : allOrgs) {
			List<APIAccess> orgAPIAccess = apim.accessAdapter.getAPIAccess(org.getId(), APIManagerAPIAccessAdapter.Type.organizations);
			for(API api : apis) {
				for(APIAccess access : orgAPIAccess) {
					if(access.getApiId().equals(api.getId())) {
						grantedOrgs.add(org);
					}
				}
				api.setClientOrganizations(grantedOrgs);
			}
		}
	}
	
	public void addClientApplications(List<API> apis, boolean addClientApplication) throws AppException {
		if(!addClientApplication || !APIManagerAdapter.hasAdminAccount()) return;
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
	
	public APIManagerAPIAdapter setAPIManagerResponse(String apiManagerResponse) {
		this.apiManagerResponse = apiManagerResponse;
		return this;
	}
}
