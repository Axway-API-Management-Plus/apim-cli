package com.axway.apim.api.model.apps;

import com.axway.apim.adapter.clientApps.jackson.JSONViews;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

@JsonIgnoreProperties(value = {"applicationId", "createdBy", "createdOn"})
public abstract class ClientAppCredential {
	
	String credentialType = null;
	@JsonView(JSONViews.CredentialsBase.class)
	String id;
	
	@JsonView(JSONViews.CredentialsBase.class)
	boolean enabled;
	
	@JsonView(JSONViews.CredentialsBase.class)
	String createdBy;
	
	@JsonView(JSONViews.CredentialsBase.class)
	String createdOn;
	
	@JsonView(JSONViews.CredentialsBase.class)
	String secret;
	
	@JsonView(JSONViews.CredentialsBase.class)
	String[] corsOrigins;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String[] getCorsOrigins() {
		return corsOrigins;
	}

	public void setCorsOrigins(String[] corsOrigins) {
		this.corsOrigins = corsOrigins;
	}

	public abstract String getCredentialType();
}
