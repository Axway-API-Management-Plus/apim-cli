package com.axway.apim.swagger.api.properties.apiAccess;

public class APIAccess {
	String id;
	
	String apiId;
	
	String createdBy;
	
	String state;
	
	String createdOn;
	
	String enabled;

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

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return "APIAccess [id=" + id + ", apiId=" + apiId + ", createdBy=" + createdBy + ", state=" + state
				+ ", createdOn=" + createdOn + ", enabled=" + enabled + "]";
	}
}
