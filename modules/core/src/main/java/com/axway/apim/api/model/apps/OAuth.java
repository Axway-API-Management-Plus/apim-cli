package com.axway.apim.api.model.apps;

public class OAuth extends ClientAppCredential {
	
	String cert;
	
	String type;
	
	String clientId;
	
	String[] redirectUrls;

	@Override
	public String getCredentialType() {
		return "oauth";
	}

	public String getCert() {
		return cert;
	}

	public void setCert(String cert) {
		this.cert = cert;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String[] getRedirectUrls() {
		return redirectUrls;
	}

	public void setRedirectUrls(String[] redirectUrls) {
		this.redirectUrls = redirectUrls;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}
