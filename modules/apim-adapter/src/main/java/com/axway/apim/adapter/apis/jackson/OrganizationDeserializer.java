package com.axway.apim.adapter.apis.jackson;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class OrganizationDeserializer extends StdDeserializer<Organization> {
	
	static Logger LOG = LoggerFactory.getLogger(OrganizationDeserializer.class);
	
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
		try {
			// Deserialization depends on the direction
			if("organizationId".equals(jp.currentName())) {
				// organizationId is given by API-Manager
				return APIManagerAdapter.getInstance().orgAdapter.getOrgForId(node.asText());
			} else {
				Organization organization;
				// organization name is given in the config file
				// If we don't have an Admin-Account don't try to load the organization!
				if(!APIManagerAdapter.hasAdminAccount()) {
					User user = APIManagerAdapter.getCurrentUser(false);
					organization = APIManagerAdapter.getInstance().orgAdapter.getOrgForId(user.getOrganizationId());
					if(!node.asText().equals(organization.getName())) {
						LOG.warn("The given API-Organization is invalid as OrgAdmin user: '"+user.getName()+"' belongs to organization: '" + organization.getName() + "'. API will be registered with OrgAdmin organization.");
					}
					return organization;
				}
				// Otherwise make sure the organization exists and try to load it
				organization = APIManagerAdapter.getInstance().orgAdapter.getOrgForName(node.asText());
				if(organization==null) {
					ErrorState.getInstance().setError("The given organization: '"+node.asText()+"' is unknown.", ErrorCode.UNKNOWN_ORGANIZATION, false);
					throw new AppException("The given organization: '"+node.asText()+"' is unknown.", ErrorCode.UNKNOWN_ORGANIZATION);
				}
				return organization;
			}
		} catch (AppException e) {
			throw new IOException("Error reading organization", e);
		}
	}
}
