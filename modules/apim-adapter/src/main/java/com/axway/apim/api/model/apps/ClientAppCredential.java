package com.axway.apim.api.model.apps;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"applicationId", "createdBy", "createdOn"})
@JsonFilter("ClientAppCredentialFilter")
public abstract class ClientAppCredential {
	
	String credentialType = null;
	String id;
	
	boolean enabled = true;
	
	String createdBy;
	
	String createdOn;
	
	String secret;
	
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

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof ClientAppCredential) {
			ClientAppCredential otherAppCredential = (ClientAppCredential)other;
			return 
					StringUtils.equals(otherAppCredential.getCredentialType(), this.getCredentialType() ) && 
					otherAppCredential.isEnabled()==this.isEnabled() && 
					StringUtils.equals(otherAppCredential.getSecret(), this.getSecret() ) &&
					Arrays.equals(otherAppCredential.getCorsOrigins(), this.getCorsOrigins());
		}
		return false;
	}	
}
