package com.axway.apim.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class Profile {
	/**  it
	 * Used internally to save original API-Method ID as given by the API-Manager
	 */
	@JsonIgnore
	String apiOperationId;
	
	/**
	 * Stores the API-Method name 
	 */
	@JsonIgnore
	String apiOperationName;
	
	/**
	 * Used internally to save original API-ID as given by the API-Manager
	 */
	@JsonIgnore
	String apiId;

	public String getApiOperationId() {
		return apiOperationId;
	}

	public void setApiOperationId(String apiOperationId) {
		this.apiOperationId = apiOperationId;
	}

	public String getApiOperationName() {
		return apiOperationName;
	}

	public void setApiOperationName(String apiOperationName) {
		this.apiOperationName = apiOperationName;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}
}
