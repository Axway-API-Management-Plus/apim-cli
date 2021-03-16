package com.axway.apim.api.export.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.adapter.apis.APIFilter.Builder.APIType;
import com.axway.apim.adapter.apis.APIFilter.METHOD_TRANSLATION;
import com.axway.apim.adapter.apis.APIFilter.POLICY_TRANSLATION;
import com.axway.apim.adapter.apis.APIManagerPoliciesAdapter.PolicyType;
import com.axway.apim.api.API;
import com.axway.apim.api.export.ExportAPI;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.model.CustomProperties.Type;
import com.axway.apim.api.model.DeviceType;
import com.axway.apim.api.model.InboundProfile;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.api.model.SecurityDevice;
import com.axway.apim.api.model.SecurityProfile;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.Utils;

public abstract class APIResultHandler {

	protected static Logger LOG = LoggerFactory.getLogger(APIResultHandler.class);
	
	APIExportParams params;
	
	boolean hasError = false;
	
	public enum APIListImpl {
		JSON_EXPORTER(JsonAPIExporter.class),
		CONSOLE_EXPORTER(ConsoleAPIExporter.class),
		CSV_EXPORTER(CSVAPIExporter.class),
		API_DELETE_HANDLER(DeleteAPIHandler.class),
		API_PUBLISH_HANDLER(PublishAPIHandler.class),
		API_UNPUBLISH_HANDLER(UnpublishAPIHandler.class), 
		API_CHANGE_HANDLER(APIChangeHandler.class),
		API_APPROVE_HANDLER(ApproveAPIHandler.class),
		API_UPGRADE_ACCESS_HANDLE(UpgradeAccessAPIHandler.class),
		API_GRANT_ACCESS_HANDLER(GrantAccessAPIHandler.class);
		
		private final Class<APIResultHandler> implClass;
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private APIListImpl(Class clazz) {
			this.implClass = clazz;
		}

		public Class<APIResultHandler> getClazz() {
			return implClass;
		}
	}

	public APIResultHandler(APIExportParams params) {
		this.params = params;
	}
	
	public static APIResultHandler create(APIListImpl exportImpl, APIExportParams params) throws AppException {
		try {
			Object[] intArgs = new Object[] { params };
			Constructor<APIResultHandler> constructor = exportImpl.getClazz().getConstructor(new Class[]{APIExportParams.class});
			APIResultHandler exporter = constructor.newInstance(intArgs);
			return exporter;
		} catch (Exception e) {
			throw new AppException("Error initializing API export handler", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
	
	public abstract void execute(List<API> apis) throws AppException;
	
	public boolean hasError() {
		return this.hasError;
	}
	
	public abstract APIFilter getFilter();
	
	protected Builder getBaseAPIFilterBuilder() {
		Builder builder = new APIFilter.Builder(APIType.CUSTOM)
				.hasVHost(params.getVhost())
				.hasApiPath(params.getApiPath())
				.hasPolicyName(params.getPolicy())
				.hasId(params.getId())
				.hasName(params.getName())
				.hasOrganization(params.getOrganization())
				.hasTag(params.getTag())
				.hasState(params.getState())
				.hasBackendBasepath(params.getBackend())
				.hasInboundSecurity(params.getInboundSecurity())
				.hasOutboundAuthentication(params.getOutboundAuthentication())
				.includeCustomProperties(getAPICustomProperties())
				.translateMethods(METHOD_TRANSLATION.AS_NAME)
				.translatePolicies(POLICY_TRANSLATION.TO_NAME)
				.useFEAPIDefinition(params.isUseFEAPIDefinition())
				.failOnError(false);
		return builder;
	}
	
	protected List<String> getAPICustomProperties() {
		try {
			return APIManagerAdapter.getInstance().customPropertiesAdapter.getCustomPropertyNames(Type.api);
		} catch (AppException e) {
			LOG.error("Error reading custom properties configuration from API-Manager");
			return null;
		}
	}
    
    protected static String getBackendPath(API api) {
		ExportAPI exportAPI = new ExportAPI(api);
		return exportAPI.getBackendBasepath();
	}
    
    protected static String getUsedSecurity(API api) {
		List<String> usedSecurity = new ArrayList<String>();
		Map<String, SecurityProfile> secProfilesMappedByName = new HashMap<String, SecurityProfile>();
		try {
			for(SecurityProfile secProfile : api.getSecurityProfiles()) {
				secProfilesMappedByName.put(secProfile.getName(), secProfile);
			}
		
			Iterator<InboundProfile> it;
			it = api.getInboundProfiles().values().iterator();

		while(it.hasNext()) {
			InboundProfile profile = it.next();
			SecurityProfile usedSecProfile = secProfilesMappedByName.get(profile.getSecurityProfile());
			// If Security-Profile null only happens for method overrides, then they are using the API-Default --> Skip this InboundProfile
			if(usedSecProfile==null) continue;
			for(SecurityDevice device : usedSecProfile.getDevices()) {
				if(device.getType()==DeviceType.authPolicy) {
					String authenticationPolicy = device.getProperties().get("authenticationPolicy");
					usedSecurity.add(Utils.getExternalPolicyName(authenticationPolicy));
				} else {
					usedSecurity.add(""+device.getType().getName());
				}
			}
		}
		String result = usedSecurity.toString().replace("[", "").replace("]", "");
		return result;
		} catch (AppException e) {
			LOG.error("Error getting security information for API", e);
			return "Err";
		}
	}
	
	protected static List<String> getUsedPolicies(API api, PolicyType type) {
		return getUsedPolicies(api).get(type);
	}
	
	protected static Map<PolicyType, List<String>> getUsedPolicies(API api) {
		Iterator<OutboundProfile> it;
		Map<PolicyType, List<String>> result = new HashMap<PolicyType, List<String>>();
		List<String> requestPolicies = new ArrayList<String>();
		List<String> routingPolicies = new ArrayList<String>();
		List<String> responsePolicies = new ArrayList<String>();
		List<String> faultHandlerPolicies = new ArrayList<String>();
		it = api.getOutboundProfiles().values().iterator();
		
		while(it.hasNext()) {
			OutboundProfile profile = it.next();
			if(profile.getRequestPolicy()!=null && profile.getRequestPolicy().getName()!=null) {
				requestPolicies.add(profile.getRequestPolicy().getName());
			}
			if(profile.getRouteType().equals("policy") && profile.getRoutePolicy()!=null && profile.getRoutePolicy().getName()!=null) {
				routingPolicies.add(profile.getRoutePolicy().getName());
			}
			if(profile.getResponsePolicy()!=null && profile.getResponsePolicy().getName()!=null) {
				responsePolicies.add(profile.getResponsePolicy().getName());
			}
			if(profile.getFaultHandlerPolicy()!=null && profile.getFaultHandlerPolicy().getName()!=null) {
				faultHandlerPolicies.add(profile.getFaultHandlerPolicy().getName());
			}
		}
		result.put(PolicyType.REQUEST, requestPolicies);
		result.put(PolicyType.ROUTING, routingPolicies);
		result.put(PolicyType.RESPONSE, responsePolicies);
		result.put(PolicyType.FAULT_HANDLER, faultHandlerPolicies);
		return result;
	}
	
	protected static String getCustomProps(API api) {
		if(api.getCustomProperties()==null) return "N/A";
		Iterator<String> it = api.getCustomProperties().keySet().iterator();
		List<String> props = new ArrayList<String>();
		while(it.hasNext()) {
			String property = it.next();
			String value = api.getCustomProperties().get(property);
			props.add(property + ": " + value);
		}
		return props.toString().replace("[", "").replace("]", "");
	}
	
	protected static String getTags(API api) {
		if(api.getTags()==null) return "None";
		Iterator<String> it = api.getTags().keySet().iterator();
		List<String> tags = new ArrayList<String>();
		while(it.hasNext()) {
			String tagGroup = it.next();
			String[] tagValues = api.getTags().get(tagGroup);
			tags.add(tagGroup + ": " + Arrays.toString(tagValues));
		}
		return tags.toString().replace("[", "").replace("]", "");
	}
	
	protected static List<String> getGrantedOrganizations(API api) {
		List<String> grantedOrgs = new ArrayList<String>();
		try {
			if(api.getClientOrganizations()==null) return grantedOrgs;
			for(Organization org : api.getClientOrganizations()) {
				grantedOrgs.add(org.getName());
			}
			return grantedOrgs;
		} catch (Exception e) {
			LOG.error("Error getting API client organization");
			return grantedOrgs;
		}
	}
}
