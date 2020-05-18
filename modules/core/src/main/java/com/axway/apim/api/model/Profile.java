package com.axway.apim.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class Profile {
	/**
	 * Used internally to save original API-Method ID as given by the API-Manager
	 */
	@JsonIgnore
	String apiMethodId;
	
	/**
	 * Stores the API-Method name 
	 */
	@JsonIgnore
	String apiMethodName;
	
	/**
	 * Used internally to save original API-ID as given by the API-Manager
	 */
	@JsonIgnore
	String apiId;

	public String getApiMethodId() {
		return apiMethodId;
	}

	public void setApiMethodId(String apiMethodId) {
		this.apiMethodId = apiMethodId;
	}

	public String getApiMethodName() {
		return apiMethodName;
	}

	public void setApiMethodName(String apiMethodName) {
		this.apiMethodName = apiMethodName;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}
}
