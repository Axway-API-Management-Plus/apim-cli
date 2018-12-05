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
import com.axway.apim.swagger.api.properties.outboundprofiles.OutboundProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SecurityDevice {
	
	protected static Logger LOG = LoggerFactory.getLogger(SecurityDevice.class);
	
	private static Map<String, String> oauthTokenStores;
	
	String name;
	
	String type;
	
	String order;
	
	Map<String, Object> properties;

	public SecurityDevice() throws AppException {
		super();
		if(SecurityDevice.oauthTokenStores == null) { 
			SecurityDevice.oauthTokenStores = initTokenStores();
		}
		this.properties = new LinkedHashMap<String, Object>();
	}
	
	private static Map<String, String> initTokenStores() throws AppException { 
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> tokenStores = new HashMap<String, String>();
		CommandParameters cmd = CommandParameters.getInstance();
		try {
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/tokenstores").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();
			
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

	public Map<String, Object> getProperties() throws AppException {
		if(this.type.equals("oauth")) {
			Object tokenStore = properties.get("tokenStore");
			String esTokenStore = oauthTokenStores.get(tokenStore);
			if(esTokenStore == null) {
				LOG.error("Available token stores: " + oauthTokenStores.keySet());
				throw new AppException("The tokenstore: '" + tokenStore + "' is not configured in this API-Manager", ErrorCode.UNKNOWN_CUSTOM_POLICY, false);
			} else {
				properties.put("tokenStore", esTokenStore);
			}
		}
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
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
