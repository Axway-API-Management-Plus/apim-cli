package com.axway.apim.appimport.lib;

import com.axway.apim.adapter.apis.jackson.JSONViews;
import com.axway.apim.adapter.apis.jackson.OrganizationDeserializer;
import com.axway.apim.adapter.apis.jackson.OrganizationSerializer;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.apps.ClientApplication;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DesiredClientApp extends ClientApplication {
	
	@JsonDeserialize( using = OrganizationDeserializer.class)
	@JsonSerialize (using = OrganizationSerializer.class)
	@JsonProperty(value = "organizationId")
	@JsonAlias({ "organization" })
	@JsonView(JSONViews.ApplicationBase.class)
	protected Organization organization = null;
}
