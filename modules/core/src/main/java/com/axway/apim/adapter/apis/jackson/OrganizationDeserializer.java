package com.axway.apim.adapter.apis.jackson;

import java.io.IOException;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class OrganizationDeserializer extends StdDeserializer<Organization> {
	
	private static final long serialVersionUID = 1L;
	
	public OrganizationDeserializer() {
		this(null);
	}

	public OrganizationDeserializer(Class<Organization> organization) {
		super(organization);
	}

	@Override
	public Organization deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		OrgFilter filter;
		try {
			// Deserialization depends on the direction
			if("organizationId".equals(jp.currentName())) {
				// organizationId is given by API-Manager
				return APIManagerAdapter.getInstance().orgAdapter.getOrgForId(node.asText());
			} else {
				// organization name is given in the config file
				return APIManagerAdapter.getInstance().orgAdapter.getOrgForName(node.asText());
			}
		} catch (AppException e) {
			throw new IOException("Error reading organization", e);
		}
	}
}
