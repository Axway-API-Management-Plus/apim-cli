package com.axway.apim.api.model.apps;

public class ExtClients extends ClientAppCredential {
	
	String clientId;
	
	@Override
	public String getCredentialType() {
		return "extclients";
	}

	public ExtClients() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void setId(String id) {
		// Copy the field id into a more human readable field
		this.clientId = id;
		super.setId(id);
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
		this.id = clientId;
	}

}
