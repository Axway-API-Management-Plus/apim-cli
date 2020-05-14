package com.axway.apim.adapter.apis;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerPoliciesAdapter {
	
	public static String ROUTING = "routing";
	public static String REQUEST = "request";
	public static String RESPONSE = "response";
	public static String FAULT_HANDLER = "faultHandler";
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerPoliciesAdapter.class);
	
	ObjectMapper mapper = APIManagerAdapter.mapper;

	public APIManagerPoliciesAdapter() {}
	
	Map<String, String> apiManagerResponse = new HashMap<String, String>();
	
	
	/** A map with all policies (First level the type of the policy) */
	private Map<String, Map<String, String>> policiesMappedByName = new HashMap<String, Map<String, String>>();
	private Map<String, Map<String, String>> policiesMappedByKey = new HashMap<String, Map<String, String>>();
	
	private void readPoliciesFromAPIManager(String type) throws AppException { 
		if(apiManagerResponse.get(type)!=null) return;
		CommandParameters cmd = CommandParameters.getInstance();
		HttpResponse httpResponse = null;
		try {
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/policies")
					.setParameter("type", type).build();
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
	
	private void initPoliciesType(String type) throws AppException {
		readPoliciesFromAPIManager(type);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> policiesByName = new HashMap<String, String>();
		Map<String, String> policiesByKey = new HashMap<String, String>();
		try {
			JsonNode jsonResponse = mapper.readTree(apiManagerResponse.get(type));
			for(JsonNode node : jsonResponse) {
				policiesByName.put(node.get("name").asText(), node.get("id").asText());
				policiesByKey.put(node.get("id").asText(), node.get("name").asText());
			}
		} catch (Exception e) {
			LOG.error("Error reading configured custom-policies. Can't parse response: " + apiManagerResponse.get(type));
			throw new AppException("Can't initialize policies for type: " + type, ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		this.policiesMappedByName.put(type, policiesByName);
		this.policiesMappedByKey.put(type, policiesByKey);
	}
	
	public String getPolicyName(String policyKey, String type) throws AppException {
		if(policyKey == null) return policyKey; // Do nothing if no policy is configured
		if(policiesMappedByKey.get(type) == null) initPoliciesType(type);
		Map<String, String> policiesForType = this.policiesMappedByKey.get(type);
		if(policiesForType.get(policyKey) == null) {
			LOG.error("Available "+type+" policies: " + policiesForType.keySet());
			ErrorState.getInstance().setError("The policy: '" + policyKey + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY, false);
			throw new AppException("The policy: '" + policyKey + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY);
		}
		return policiesForType.get(policyKey);
	}
	
	public String getPolicyKey(String policyName, String type) throws AppException {
		if(policyName == null) return policyName; // Do nothing if no policy is configured
		if(policiesMappedByName.get(type) == null) initPoliciesType(type);
		Map<String, String> policiesForType = this.policiesMappedByName.get(type);
		if(policiesForType.get(policyName) == null) {
			LOG.error("Available "+type+" policies: " + policiesMappedByName.keySet());
			ErrorState.getInstance().setError("The policy: '" + policyName + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY, false);
			throw new AppException("The policy: '" + policyName + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY);
		}
		return policiesForType.get(policyName);
	}
}
