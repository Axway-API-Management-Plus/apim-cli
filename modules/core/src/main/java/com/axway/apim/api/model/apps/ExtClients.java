package com.axway.apim.api.model.apps;

public class ExtClients extends ClientAppCredential {
	
	String clientId;
	
	@Override
	public String getCredentialType() {
		return "extclients";
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	
}
