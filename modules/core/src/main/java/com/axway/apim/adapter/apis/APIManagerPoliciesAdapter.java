package com.axway.apim.adapter.apis;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Policy;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerPoliciesAdapter {
	
	public enum PolicyType {
		ROUTING ("routing", "routePolicy"), 
		REQUEST ("request", "requestPolicy"),
		RESPONSE ("response", "responsePolicy"),
		FAULT_HANDLER ("faultHandler", "faultHandlerPolicy"), 
		UNKNOWN ("unknown", "Unknown");
		
		private final String restAPIKey;
		private final String jsonKey;
		
		private static Map<String, PolicyType> jsonKeyToTypeMapping = null;
		
		private PolicyType(String restAPIKey, String jsonKey) {
			this.restAPIKey = restAPIKey;
			this.jsonKey = jsonKey;
		}

		public String getRestAPIKey() {
			return restAPIKey;
		}

		public String getJsonKey() {
			return jsonKey;
		}
		
		private static void initMapping() {
			jsonKeyToTypeMapping = new HashMap<String, PolicyType>();
			for(PolicyType type : values()) {
				jsonKeyToTypeMapping.put(type.getJsonKey(), type);
			}
		}
		
		public static PolicyType getTypeForJsonKey(String jsonKey) {
			if(jsonKeyToTypeMapping==null)
				initMapping();
			return jsonKeyToTypeMapping.get(jsonKey);
		}
	}
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerPoliciesAdapter.class);
	
	ObjectMapper mapper = APIManagerAdapter.mapper;

	public APIManagerPoliciesAdapter() {}
	
	Map<PolicyType, String> apiManagerResponse = new HashMap<PolicyType, String>();
	
	Map<PolicyType, List<Policy>> mappedPolicies = new HashMap<PolicyType, List<Policy>>();
	
	private void readPoliciesFromAPIManager(PolicyType type) throws AppException { 
		if(apiManagerResponse.get(type)!=null) return;
		CommandParameters cmd = CommandParameters.getInstance();
		HttpResponse httpResponse = null;
		try {
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/policies")
					.setParameter("type", type.getRestAPIKey()).build();
			RestAPICall getRequest = new GETRequest(uri, null);
			httpResponse = getRequest.execute();
			apiManagerResponse.put(type, EntityUtils.toString(httpResponse.getEntity()));

		} catch (Exception e) {
			throw new AppException("Can't initialize policies for type: " + type, ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	private void initPoliciesType(PolicyType type) throws AppException {
		if(this.mappedPolicies.get(type)==null) {
			readPoliciesFromAPIManager(type);
		}
		try {
			List<Policy> policies = mapper.readValue(apiManagerResponse.get(type), new TypeReference<List<Policy>>(){});
			mappedPolicies.put(type, policies);
		} catch (Exception e) {
			LOG.error("Error reading configured custom-policies. Can't parse response: " + apiManagerResponse.get(type));
			throw new AppException("Can't initialize policies for type: " + type, ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	public Policy getPolicyForName(PolicyType type, String name) throws AppException {
		initPoliciesType(type);
		List<Policy> policies = this.mappedPolicies.get(type);
		
		for(Policy policy : policies) {
			if(policy.getName().equals(name)) {
				return policy;
			}
		}
		LOG.error("Available "+type+" policies: " + policies);
		ErrorState.getInstance().setError("The policy: '" + name + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY, false);
		throw new AppException("The policy: '" + name + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY);
	}
	
	public Policy getPolicyForKey(PolicyType type, String key) throws AppException {
		initPoliciesType(type);
		List<Policy> policies = this.mappedPolicies.get(type);
		
		for(Policy policy : policies) {
			if(policy.getId().equals(key)) {
				return policy;
			}
		}
		LOG.error("Available "+type+" policies: " + policies);
		ErrorState.getInstance().setError("The policy: '" + key + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY, false);
		throw new AppException("The policy: '" + key + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY);
	}
}
