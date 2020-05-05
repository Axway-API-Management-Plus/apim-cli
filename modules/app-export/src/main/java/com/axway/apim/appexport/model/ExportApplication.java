package com.axway.apim.appexport.model;

import com.axway.apim.api.model.ClientApplication;
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
	public String getOrganizationId() {
		return clientApp.getOrganizationId();
	}

	public String getName() {
		return clientApp.getName();
	}
}
