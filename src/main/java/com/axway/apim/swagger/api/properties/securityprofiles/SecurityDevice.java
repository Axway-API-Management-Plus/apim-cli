package com.axway.apim.swagger.api.properties.securityprofiles;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SecurityDevice {
	
	protected static Logger LOG = LoggerFactory.getLogger(SecurityDevice.class);
	
	private static Map<String, String> oauthTokenStores;
	private static Map<String, String> oauthInfoPolicies;
	
	String name;
	
	String type;
	
	String order;
	
	Map<String, String> properties;

	public SecurityDevice() throws AppException {
		super();
		if(SecurityDevice.oauthTokenStores == null) { 
			SecurityDevice.oauthTokenStores = initTokenStores();
			SecurityDevice.oauthInfoPolicies = initOAuthInfoPolicies();
		}
		
		this.properties = new LinkedHashMap<String, String>();
	}
	
	private static Map<String, String> initTokenStores() throws AppException { 
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> tokenStores = new HashMap<String, String>();
		CommandParameters cmd = CommandParameters.getInstance();
		InputStream response = null;
		try {
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/tokenstores").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			response = getRequest.execute().getEntity().getContent();
			
			JsonNode jsonResponse;
			try {
				jsonResponse = mapper.readTree(response);
				for(JsonNode node : jsonResponse) {
					tokenStores.put(node.get("name").asText(), node.get("id").asText());
				}
			} catch (IOException e) {
				throw new AppException("Can't find Tokenstore: ....", ErrorCode.API_MANAGER_COMMUNICATION, e);
			}
		} catch (Exception e) {
			throw new AppException("Can't find Tokenstore: ....", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				response.close();
			} catch (Exception ignore) { }
		}
		return tokenStores;
	}
	
	private static Map<String, String> initOAuthInfoPolicies() throws AppException { 
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> tokenStores = new HashMap<String, String>();
		CommandParameters cmd = CommandParameters.getInstance();
		try {
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/policies")
					.setParameter("type", "oauthtokeninfo").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();
			
			JsonNode jsonResponse;
			try {
				jsonResponse = mapper.readTree(response);
				for(JsonNode node : jsonResponse) {
					tokenStores.put(node.get("name").asText(), node.get("id").asText());
				}
			} catch (IOException e) {
				throw new AppException("Can't initialize Tokeninformation-Policies", ErrorCode.API_MANAGER_COMMUNICATION, e);
			}
		} catch (Exception e) {
			throw new AppException("Can't initialize Tokeninformation-Policies", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		return tokenStores;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	@Override
	public String toString() {
		return "SecurityDevice [type=" + type + "]";
	}

	public Map<String, String> getProperties() throws AppException {
		if(this.type.equals("oauth")) {
			String tokenStore = (String)properties.get("tokenStore");
			if(tokenStore.startsWith("<key")) return properties;
			String esTokenStore = oauthTokenStores.get(tokenStore);
			if(esTokenStore == null) {
				LOG.error("Available token stores: " + oauthTokenStores.keySet());
				throw new AppException("The tokenstore: '" + tokenStore + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY, false);
			} else {
				properties.put("tokenStore", esTokenStore);
			}
		} else if(this.type.equals("oauthExternal")) {
			String infoPolicy = (String)properties.get("tokenStore"); // The token-info-policy is stored in the tokenStore as well
			if(infoPolicy.startsWith("<key")) return properties;
			String esInfoPolicy = oauthInfoPolicies.get(infoPolicy);
			if(esInfoPolicy == null) {
				LOG.error("Available information policies: " + oauthInfoPolicies.keySet());
				throw new AppException("The Information-Policy: '" + infoPolicy + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY, false);
			} else {
				properties.put("tokenStore", esInfoPolicy);
				properties.put("oauth.token.client_id", "${oauth.token.client_id}");
				properties.put("oauth.token.scopes", "${oauth.token.scopes}");
				properties.put("oauth.token.valid", "${oauth.token.valid}");
			}
		}
		
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof SecurityDevice) {
			SecurityDevice otherSecurityDevice = (SecurityDevice)other;
			if(!StringUtils.equals(otherSecurityDevice.getName(), this.getName())) return false;
			if(!StringUtils.equals(otherSecurityDevice.getType(), this.getType())) return false;
			if(!StringUtils.equals(otherSecurityDevice.getOrder(), this.getOrder())) return false;
			try {
				if(!otherSecurityDevice.getProperties().equals(this.getProperties())) return false;
			} catch (AppException e) {
				LOG.error("Cant compare SecurityDevices", e);
				return false;
			}
		}
		return true;
	}
}
