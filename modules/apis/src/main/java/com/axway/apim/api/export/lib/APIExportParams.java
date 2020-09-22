package com.axway.apim.api.export.lib;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.StandardExportParams;

public class APIExportParams extends StandardExportParams {
	
	private String name;
	
	private String id;
	
	private boolean useFEAPIDefinition;
	
	private String vhost;
	
	private String apiPath;
	
	private String policy;
	
	private String state;
	
	private String backend;
	
	public static synchronized APIExportParams getInstance() {
		return (APIExportParams)CoreParameters.getInstance();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public boolean isUseFEAPIDefinition() {
		return useFEAPIDefinition;
	}

	public void setUseFEAPIDefinition(boolean useFEAPIDefinition) {
		this.useFEAPIDefinition = useFEAPIDefinition;
	}
}
