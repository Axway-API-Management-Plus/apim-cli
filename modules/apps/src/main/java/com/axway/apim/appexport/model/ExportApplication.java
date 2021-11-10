package com.axway.apim.appexport.model;

import java.util.List;
import java.util.Map;

import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.apps.ApplicationPermission;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.ClientAppOauthResource;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.api.model.apps.ClientApplication.ApplicationState;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "name", "organization", "description", "state", "image", "enabled", "email", "phone", "credentials", "appQuota", "apis", "customProperties" })
public class ExportApplication {
	
	ClientApplication clientApp;

	public ExportApplication(ClientApplication clientApp) {
		super();
		this.clientApp = clientApp;
	}
	
	public String getOrganization() {
		return this.clientApp.getOrganization().getName();
	}

	public String getName() {
		return clientApp.getName();
	}

	public List<ClientAppCredential> getCredentials() {
		if(clientApp.getCredentials()==null || clientApp.getCredentials().size()==0) return null;
		return clientApp.getCredentials();
	}

	public String getDescription() {
		return clientApp.getDescription();
	}

	public String getEmail() {
		return clientApp.getEmail();
	}

	public String getPhone() {
		return clientApp.getPhone();
	}

	public boolean isEnabled() {
		return clientApp.isEnabled();
	}

	public ApplicationState getState() {
		return clientApp.getState();
	}

	public Image getImage() {
		return clientApp.getImage();
	}
	
	public List<ApplicationPermission> getPermissions() {
		return clientApp.getPermissions();
	}

	public APIQuota getAppQuota() {
		if(clientApp.getAppQuota()==null || clientApp.getAppQuota().getRestrictions()==null || clientApp.getAppQuota().getRestrictions().size()==0) return null;
		return clientApp.getAppQuota();
	}
	
	@JsonProperty("apis")
	public List<APIAccess> getAPIAccess() {
		if(clientApp.getApiAccess()==null || clientApp.getApiAccess().size()==0) return null;
		return clientApp.getApiAccess();
	}

	public Map<String, String> getCustomProperties() {
		return clientApp.getCustomProperties();
	}
	
	@JsonProperty("appScopes")
	public List<ClientAppOauthResource> getOauthResources() {
		if(clientApp.getOauthResources()==null || clientApp.getOauthResources().size()==0) return null;
		return clientApp.getOauthResources();
	}
}
