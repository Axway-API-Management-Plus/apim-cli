package com.axway.apim.appexport.model;

import java.util.List;

import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "name" })
public class ExportApplication extends ClientApplication {
	
	ClientApplication clientApp;

	public ExportApplication(ClientApplication clientApp) {
		super();
		this.clientApp = clientApp;
	}

	@JsonIgnore
	public String getOrganizationId() throws AppException {
		return clientApp.getOrganizationId();
	}

	public String getName() {
		return clientApp.getName();
	}

	public List<ClientAppCredential> getCredentials() {
		return clientApp.getCredentials();
	}

	@Override
	public String getDescription() {
		return clientApp.getDescription();
	}

	@Override
	public String getEmail() {
		return clientApp.getEmail();
	}

	@Override
	public String getPhone() {
		return clientApp.getPhone();
	}

	@Override
	public boolean isEnabled() {
		return clientApp.isEnabled();
	}

	@Override
	public String getState() {
		return clientApp.getState();
	}

	@Override
	public String getImage() {
		return clientApp.getImage();
	}

	@Override
	public String getOrganization() {
		return clientApp.getOrganization();
	}

	@Override
	public APIQuota getAppQuota() {
		return clientApp.getAppQuota();
	}
	
	
}
