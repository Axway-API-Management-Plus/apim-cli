package com.axway.apim.api.model.apps;

import org.apache.commons.lang.StringUtils;

public class ExtClients extends ClientAppCredential {
	
	@Override
	public String getCredentialType() {
		return "extclients";
	}

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
			ExtClients otherOAuth = (ExtClients)other;
			return 
				StringUtils.equals(otherOAuth.getClientId(), this.getClientId()) && 
				super.equals(other);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "ExtClients [credentialType=" + credentialType + ", id=" + id + ", enabled=" + enabled + "]";
	}
}
