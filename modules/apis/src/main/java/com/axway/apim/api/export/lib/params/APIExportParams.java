package com.axway.apim.api.export.lib.params;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardExportParams;

public class APIExportParams extends StandardExportParams implements APIFilterParams, Parameters {
	
	private String name;
	
	private String organization;
	
	private String id;
	
	private Boolean useFEAPIDefinition;
	
	private String vhost;
	
	private String apiPath;
	
	private String policy;
	
	private String state;
	
	private String backend;
	
	private String inboundSecurity;
	
	private String outboundAuthentication ;
	
	private String tag;
	
	public static synchronized APIExportParams getInstance() {
		return (APIExportParams)CoreParameters.getInstance();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVhost() {
		return vhost;
	}

	public void setVhost(String vhost) {
		this.vhost = vhost;
	}

	public String getApiPath() {
		return apiPath;
	}

	public void setApiPath(String apiPath) {
		this.apiPath = apiPath;
	}

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getBackend() {
		return backend;
	}

	public void setBackend(String backend) {
		this.backend = backend;
	}

	public String getInboundSecurity() {
		return inboundSecurity;
	}

	public void setInboundSecurity(String inboundSecurity) {
		this.inboundSecurity = inboundSecurity;
	}

	public String getOutboundAuthentication() {
		return outboundAuthentication;
	}

	public void setOutboundAuthentication(String outboundAuthentication) {
		this.outboundAuthentication = outboundAuthentication;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public boolean isUseFEAPIDefinition() {
		if(useFEAPIDefinition==null) return false;
		return useFEAPIDefinition.booleanValue();
	}

	public void setUseFEAPIDefinition(Boolean useFEAPIDefinition) {
		if(useFEAPIDefinition==null) return;
		this.useFEAPIDefinition = useFEAPIDefinition;
	}
}
