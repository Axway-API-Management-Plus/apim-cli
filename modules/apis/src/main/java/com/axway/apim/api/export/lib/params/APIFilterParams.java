package com.axway.apim.api.export.lib.params;

public interface APIFilterParams {
	
	String getApiPath();

	void setApiPath(String apiPath);
	
	void setName(String name);
	
	void setOrganization(String organization);
	
	void setId(String id);
	void setPolicy(String policy);
	void setVhost(String vhost);
	void setState(String state);
	void setBackend(String backend);
	void setTag(String tag);
	void setInboundSecurity(String inboundsecurity);
	void setOutboundAuthentication(String outboundauthn);
}
