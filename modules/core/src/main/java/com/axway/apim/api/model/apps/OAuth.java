package com.axway.apim.api.model.apps;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.axway.apim.adapter.apis.jackson.JSONViews;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

public class OAuth extends ClientAppCredential {
	
	@JsonView(JSONViews.CredentialsBaseInformation.class)
	String cert;
	
	@JsonView(JSONViews.CredentialsBaseInformation.class)
	String type;
	
	@JsonView(JSONViews.CredentialsBaseInformation.class)
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

	@JsonView(JSONViews.CredentialsForExport.class)
	@JsonProperty("clientId")
	public String getClientId() {
		return id;
	}

	public void setClientId(String clientId) {
		this.id = clientId;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof OAuth) {
			OAuth otherOAuth = (OAuth)other;
			return 
				StringUtils.equals(otherOAuth.getClientId(), this.getClientId()) &&
				Arrays.equals(otherOAuth.getRedirectUrls(), this.getRedirectUrls()) && 
				super.equals(other);
		} else {
			return false;
		}
	}
}
