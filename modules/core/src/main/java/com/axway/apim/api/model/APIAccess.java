package com.axway.apim.api.model;

import org.apache.commons.lang.StringUtils;

import com.axway.apim.adapter.apis.jackson.JSONViews.APIAccessForAPIManager;
import com.axway.apim.adapter.apis.jackson.JSONViews.APIAccessForExport;
import com.fasterxml.jackson.annotation.JsonView;

public class APIAccess {
	@JsonView(APIAccessForAPIManager.class)
	String id;
	
	@JsonView(APIAccessForAPIManager.class)
	String apiId;
	
	@JsonView(APIAccessForAPIManager.class)
	String createdBy;
	
	@JsonView(APIAccessForAPIManager.class)
	String state;
	
	@JsonView(APIAccessForAPIManager.class)
	String createdOn;
	
	@JsonView(APIAccessForExport.class)
	String apiName;
	
	@JsonView(APIAccessForExport.class)
	String apiVersion;
	
	@JsonView(APIAccessForAPIManager.class)
	boolean enabled;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof APIAccess) {
			APIAccess otherApiAccess = (APIAccess)other;
			return 
					StringUtils.equals(otherApiAccess.getApiId(), this.getApiId()) &&
					StringUtils.equals(otherApiAccess.getApiName(), this.getApiName()) &&
					StringUtils.equals(otherApiAccess.getApiVersion(), this.getApiVersion())
					;
		}
		return false;
	}

	@Override
	public String toString() {
		return "APIAccess [apiName=" + apiName + ", apiVersion=" + apiVersion + ", id=" + id + ", apiId=" + apiId + "]";
	}
}
