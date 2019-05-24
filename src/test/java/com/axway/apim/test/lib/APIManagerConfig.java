package com.axway.apim.test.lib;

public class APIManagerConfig {
	
	public static String enableQueryBasedRouting(String managerConfig, String versionParameter) {
		// "apiRoutingKeyLocation": null, --> This is what we have
		// "apiRoutingKeyLocation":"query|ver", --> This is what we need
		String updatedConfig;
		updatedConfig = managerConfig.replaceAll("\"apiRoutingKeyLocation\":.*,", "\"apiRoutingKeyLocation\": \"query|"+versionParameter+"\",");
		updatedConfig = updatedConfig.replaceAll("\"apiRoutingKeyEnabled\":.*}", "\"apiRoutingKeyEnabled\":true}");
		return updatedConfig;
	}
	
	public static String disableQueryBasedRouting(String managerConfig) {
		// "apiRoutingKeyLocation": null, --> This is what we have
		// "apiRoutingKeyLocation":"query|ver", --> This is what we need
		String updatedConfig;
		updatedConfig = managerConfig.replaceAll("\"apiRoutingKeyEnabled\":.*}", "\"apiRoutingKeyEnabled\":false}");
		return updatedConfig;
	}
}
