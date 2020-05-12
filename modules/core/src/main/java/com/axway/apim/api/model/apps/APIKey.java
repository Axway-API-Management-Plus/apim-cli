package com.axway.apim.api.model.apps;

public class APIKey extends ClientAppCredential {
	
	String deletedOn;
	
	String apiKey;
	
	@Override
	public String getCredentialType() {
		return "apikeys";
	}

	public String getDeletedOn() {
		return deletedOn;
	}

	public void setDeletedOn(String deletedOn) {
		this.deletedOn = deletedOn;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
}
