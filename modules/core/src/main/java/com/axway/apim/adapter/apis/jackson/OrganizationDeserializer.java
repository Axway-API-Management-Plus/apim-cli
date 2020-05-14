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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class OrganizationDeserializer extends StdDeserializer<Organization> {
	
	private static final long serialVersionUID = 1L;
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
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
		String orgId = node.asText();
		try {
			Organization org = APIManagerAdapter.getInstance().orgAdapter.getOrg(new OrgFilter.Builder().hasId(orgId).build());
			return org;
		} catch (AppException e) {
			throw new IOException("Error reading organization", e);
		}
	}
}
