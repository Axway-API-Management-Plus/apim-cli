package com.axway.apim.api.model.apps;

import org.apache.commons.lang.StringUtils;

public class APIKey extends ClientAppCredential {
	
	String deletedOn;
	
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
		return id;
	}

	public void setApiKey(String apiKey) {
		this.id = apiKey;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof APIKey) {
			APIKey otherAPIKey = (APIKey)other;
			return 
				StringUtils.equals(otherAPIKey.getApiKey(), this.getApiKey()) &&
				StringUtils.equals(otherAPIKey.getDeletedOn(), this.getDeletedOn()) && 
				super.equals(other);
		} else {
			return false;
		}
	}
}
