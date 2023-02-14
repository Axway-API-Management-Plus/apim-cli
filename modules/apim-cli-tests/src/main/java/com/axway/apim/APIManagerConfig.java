package com.axway.apim;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class APIManagerConfig {
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	public static String enableQueryBasedRouting(String managerConfig, String versionParameter) throws IOException {
		// "apiRoutingKeyLocation": null, --> This is what we have
		// "apiRoutingKeyLocation":"query|ver", --> This is what we need
		JsonNode config = mapper.readTree(managerConfig);
		((ObjectNode) config).put("apiRoutingKeyEnabled", true);
		((ObjectNode) config).put("apiRoutingKeyLocation", "query|"+versionParameter);
		return mapper.writeValueAsString(config);
	}
	
	public static String disableQueryBasedRouting(String managerConfig) throws IOException {
		JsonNode config = mapper.readTree(managerConfig);
		((ObjectNode) config).put("apiRoutingKeyEnabled", false);
		return mapper.writeValueAsString(config);
	}
}
