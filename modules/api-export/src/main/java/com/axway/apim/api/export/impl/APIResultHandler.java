package com.axway.apim.api.export.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIManagerAdapter.CUSTOM_PROP_TYPE;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder;
import com.axway.apim.adapter.apis.APIFilter.Builder.APIType;
import com.axway.apim.adapter.apis.APIManagerPoliciesAdapter.PolicyType;
import com.axway.apim.adapter.apis.APIFilter.METHOD_TRANSLATION;
import com.axway.apim.adapter.apis.APIFilter.POLICY_TRANSLATION;
import com.axway.apim.api.API;
import com.axway.apim.api.export.ExportAPI;
import com.axway.apim.api.export.lib.APIExportParams;
import com.axway.apim.api.model.InboundProfile;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.api.model.Policy;
import com.axway.apim.api.model.SecurityDevice;
import com.axway.apim.api.model.SecurityProfile;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public abstract class APIResultHandler {

	protected static Logger LOG = LoggerFactory.getLogger(APIResultHandler.class);
	
	APIExportParams params;
	
	boolean hasError = false;
	
	public enum APIListImpl {
		JSON_EXPORTER(JsonAPIExporter.class),
		CONSOLE_EXPORTER(ConsoleAPIExporter.class),
		CSV_EXPORTER(CSVAPIExporter.class),
		API_DELETE_HANDLER(DeleteAPIHandler.class),
		API_UNPUBLISH_HANDLER(UnpublishAPIHandler.class);
		
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
			Constructor<APIResultHandler> constructor =
					exportImpl.getClazz().getConstructor(new Class[]{APIExportParams.class});
			APIResultHandler exporter = constructor.newInstance(intArgs);
			return exporter;
		} catch (Exception e) {
			throw new AppException("Error initializing application exporter", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
	
	public abstract void execute(List<API> apis) throws AppException;
	
	public boolean hasError() {
		return this.hasError;
	}
	
	public abstract APIFilter getFilter();
	
	protected Builder getBaseAPIFilterBuilder() {
		Builder builder = new APIFilter.Builder(APIType.CUSTOM)
				.hasVHost(params.getValue("vhost"))
				.hasApiPath(params.getValue("api-path"))
				.hasPolicyName(params.getValue("policy"))
				.hasId(params.getValue("id"))
				.hasName(params.getValue("name"))
				.hasState(params.getValue("state"))
				.hasBackendBasepath(params.getValue("backend"))
				.includeCustomProperties(APIManagerAdapter.getAllConfiguredCustomProperties(CUSTOM_PROP_TYPE.api))
				.translateMethods(METHOD_TRANSLATION.AS_NAME)
				.translatePolicies(POLICY_TRANSLATION.TO_NAME)
				.useFEAPIDefinition(params.isUseFEAPIDefinition());
		return builder;
	}
	
    protected static boolean askYesNo(String question) {
        return askYesNo(question, "[Y]", "[N]");
    }

    protected static boolean askYesNo(String question, String positive, String negative) {
        Scanner input = new Scanner(System.in);
        // Convert everything to upper case for simplicity...
        positive = positive.toUpperCase();
        negative = negative.toUpperCase();
        String answer;
        do {
            System.out.print(question+ " ");
            answer = input.next().trim().toUpperCase();
        } while (!answer.matches(positive) && !answer.matches(negative));
        input.close();
        // Assess if we match a positive response
        return answer.matches(positive);
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
			for(SecurityDevice device : usedSecProfile.getDevices()) {
				usedSecurity.add(""+device.getType());
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
		try {
			it = api.getOutboundProfiles().values().iterator();
		} catch (AppException e) {
			LOG.error("Error getting policy information for API", e);
			return result;
		}
		
		while(it.hasNext()) {
			OutboundProfile profile = it.next();
			if(profile.getRouteType().equals("proxy")) continue;
			if(profile.getRequestPolicy()!=null && profile.getRequestPolicy().getName()!=null) {
				requestPolicies.add(profile.getRequestPolicy().getName());
			}
			if(profile.getRoutePolicy()!=null && profile.getRoutePolicy().getName()!=null) {
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
