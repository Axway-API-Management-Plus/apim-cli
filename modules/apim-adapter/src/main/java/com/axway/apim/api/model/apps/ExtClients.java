package com.axway.apim.api.model.apps;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

public class ExtClients extends ClientAppCredential {
	
	@Override
	public String getCredentialType() {
		return "extclients";
	}
	
	private String clientId;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		
		if(other instanceof ExtClients) {
			ExtClients otherOAuth = (ExtClients)other;
			return StringUtils.equals(otherOAuth.getClientId(), this.getClientId()) && 
				super.equals(other);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(clientId);
	}

	@Override
	public String toString() {
		return "ExtClients [credentialType=" + getCredentialType() + ", id=" + id + ", enabled=" + enabled + "clientId=" + clientId + ", secret=" + secret + ", cors=" + Arrays.toString(corsOrigins) + "]";
	}
}
