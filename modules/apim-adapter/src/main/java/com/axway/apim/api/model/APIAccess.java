package com.axway.apim.api.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonFilter;

import java.util.Objects;

@JsonFilter("APIAccessFilter")
public class APIAccess {
	String id;
	
	String apiId;
	
	String createdBy;
	
	String state;
	
	Long createdOn;
	
	String apiName;
	
	String apiVersion;
	
	boolean enabled = true;

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

	public Long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Long createdOn) {
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
			if(otherApiAccess.getApiId()!=null) {
				return StringUtils.equals(otherApiAccess.getApiId(), this.getApiId());
			} else {
			return 
					StringUtils.equals(otherApiAccess.getApiId(), this.getApiId()) &&
					StringUtils.equals(otherApiAccess.getApiName(), this.getApiName()) &&
					StringUtils.equals(otherApiAccess.getApiVersion(), this.getApiVersion())
					;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "APIAccess [apiName=" + apiName + ", apiVersion=" + apiVersion + ", id=" + id + ", apiId=" + apiId + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(apiId, apiName, apiVersion);
	}
}
