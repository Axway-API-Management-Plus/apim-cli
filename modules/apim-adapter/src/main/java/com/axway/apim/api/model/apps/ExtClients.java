package com.axway.apim.api.model.apps;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.clientApps.APIMgrAppsAdapter;
import com.fasterxml.jackson.annotation.JsonProperty;

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
	public String toString() {
		return "ExtClients [credentialType=" + getCredentialType() + ", id=" + id + ", enabled=" + enabled + "clientId=" + clientId + ", secret=" + secret + ", cors=" + Arrays.toString(corsOrigins) + "]";
	}
}
