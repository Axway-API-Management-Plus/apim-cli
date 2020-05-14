package com.axway.apim.adapter.apis;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.clientApps.APIMgrAppsAdapter;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.api.API;
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
	
	ObjectMapper mapper = APIManagerAdapter.mapper;
	
	CommandParameters params = CommandParameters.getInstance();
	
	APIManagerAPIMethodAdapter methodAdapter = new APIManagerAPIMethodAdapter();
	APIManagerPoliciesAdapter policiesAdapter = new APIManagerPoliciesAdapter();
	APIManagerQuotaAdapter quotaAdapter = new APIManagerQuotaAdapter();
	APIManagerOrganizationAdapter orgAdapter = new APIManagerOrganizationAdapter();
	APIManagerAPIAccessAdapter accessAdapter = new APIManagerAPIAccessAdapter();
	APIMgrAppsAdapter appAdapter = new APIMgrAppsAdapter();

	public APIManagerAPIAdapter() {

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
			if(this.apiManagerResponse==null) readAPIsFromAPIManager(filter);
			apis = filterAPIs(filter, logMessage);
			translateMethodIds(apis, filter.translateMethodMode);
			translatePolicies(apis, filter.translatePolicyMode);
			addQuotaConfiguration(apis, filter.includeQuotas);
			addClientOrganizations(apis, filter.includeClientOrganizations);
			addClientApplications(apis, filter.includeClientApplications);
			addExistingClientAppQuotas(apis, filter.includeQuotas);
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
	private void readAPIsFromAPIManager(APIFilter filter) throws AppException {
		CommandParameters cmd = CommandParameters.getInstance();
		URI uri;
		try {
			List<NameValuePair> usedFilters = new ArrayList<>();
			if(APIManagerAdapter.hasAPIManagerVersion("7.7") && filter.apiPath != null) { // Since 7.7 we can query the API-PATH directly if given
				usedFilters.add(new BasicNameValuePair("field", "path"));
				usedFilters.add(new BasicNameValuePair("op", "eq"));
				usedFilters.add(new BasicNameValuePair("value", filter.apiPath));
			} 
			if(filter != null) { usedFilters.addAll(filter.filters); } 
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/"+filter.type)
					.addParameters(usedFilters)
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
		if(filter.apiPath==null && filter.vhost==null && filter.queryStringVersion==null && apis.size()==1) {
			return apis;
		}
			for(API api : apis) {
				if(filter.apiPath==null && filter.vhost==null && filter.queryStringVersion==null) { // Nothing given to filter out.
					foundAPIs.add(api);
					continue;
				}
				if(filter.apiPath!=null && !filter.apiPath.equals(api.getPath())) continue;
				if(!filter.useBackendAPI) {
					if(filter.vhost!=null && !filter.vhost.equals(api.getVhost())) continue;
					if(filter.queryStringVersion!=null && !filter.queryStringVersion.equals(api.getApiRoutingKey())) continue;
				}
				if(filter.useBackendAPI) {
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
					dbgCrit = " (apiPath: '"+filter.apiPath+"', filter: "+filter+", vhost: '"+filter.vhost+"', requestedType: "+filter.type+")";
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
			List<APIMethod> methods = methodAdapter.getAllMethodsForAPI(apiId);
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
						profile.setRequestPolicy(policiesAdapter.getPolicyKey(profile.getRequestPolicy(), APIManagerPoliciesAdapter.REQUEST));
						profile.setRoutePolicy(policiesAdapter.getPolicyKey(profile.getRoutePolicy(), APIManagerPoliciesAdapter.ROUTING));
						profile.setResponsePolicy(policiesAdapter.getPolicyKey(profile.getResponsePolicy(), APIManagerPoliciesAdapter.RESPONSE));
						profile.setFaultHandlerPolicy(policiesAdapter.getPolicyKey(profile.getFaultHandlerPolicy(), APIManagerPoliciesAdapter.FAULT_HANDLER));						
					} else {
						profile.setRequestPolicy(policiesAdapter.getPolicyName(profile.getRequestPolicy(), APIManagerPoliciesAdapter.REQUEST));
						profile.setRoutePolicy(policiesAdapter.getPolicyName(profile.getRoutePolicy(), APIManagerPoliciesAdapter.ROUTING));
						profile.setResponsePolicy(policiesAdapter.getPolicyName(profile.getResponsePolicy(), APIManagerPoliciesAdapter.RESPONSE));
						profile.setFaultHandlerPolicy(policiesAdapter.getPolicyName(profile.getFaultHandlerPolicy(), APIManagerPoliciesAdapter.FAULT_HANDLER));
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
				applicationQuota = quotaAdapter.getQuotaForAPI(APIManagerQuotaAdapter.APPLICATION_DEFAULT_QUOTA, api.getId()); // Get the Application-Default-Quota
				sytemQuota = quotaAdapter.getQuotaForAPI(APIManagerQuotaAdapter.SYSTEM_API_QUOTA, api.getId()); // Get the Application-Default-QuotagetQuotaFromAPIManager(); // Get the System-Default-Quota
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
				APIQuota appQuota = this.quotaAdapter.getQuotaForAPI(app.getId(), null);
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
		List<String> grantedOrgs = new ArrayList<String>();
		List<Organization> allOrgs = this.orgAdapter.getAllOrgs();
		for(Organization org : allOrgs) {
			List<APIAccess> orgAPIAccess = accessAdapter.getAPIAccess(org.getId(), APIManagerAPIAccessAdapter.Type.organizations);
			for(API api : apis) {
				for(APIAccess access : orgAPIAccess) {
					if(access.getApiId().equals(api.getId())) {
						grantedOrgs.add(org.getName());
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
				apps = this.appAdapter.getAppsSubscribedWithAPI(api.getId());
				api.setApplications(apps);
			}
		} else {
			apps = this.appAdapter.getApplications(new ClientAppFilter.Builder().build());
			for(ClientApplication app : apps) {
				List<APIAccess> APIAccess = this.accessAdapter.getAPIAccess(app.getId(), APIManagerAPIAccessAdapter.Type.applications);
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
	
	private String getFilterFields(APIFilter filter) {
		String filterFields = "[";
		if(filter.apiPath!=null) filterFields += "apiPath=" + filter.apiPath;
		if(filter.vhost!=null) filterFields += " vHost=" + filter.vhost;
		if(filter.queryStringVersion!=null) filterFields += " queryString=" + filter.queryStringVersion;
		if(filter!=null) filterFields += " filter=" + filter;
		filterFields += "]";
		return filterFields;
	}
	
	public APIManagerAPIAdapter setAPIManagerResponse(String apiManagerResponse) {
		this.apiManagerResponse = apiManagerResponse;
		return this;
	}
}
