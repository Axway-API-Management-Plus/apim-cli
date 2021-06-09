package com.axway.apim.adapter.jackson;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
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
		Organization organization;
		// Deserialization depends on the direction
		if("organizationId".equals(jp.currentName())) {
			// APIManagerAdapter is not yet initialized
			if(!APIManagerAdapter.initialized) {
				organization = new Organization();
				organization.setId(node.asText());
				return organization;
			}				
			// organizationId is given by API-Manager
			return APIManagerAdapter.getInstance().orgAdapter.getOrgForId(node.asText());
		} else {
			// APIManagerAdapter is not yet initialized
			if(APIManagerAdapter.apiManagerVersion==null) {
				organization = new Organization();
				organization.setName(node.asText());
				return organization;
			}
			// organization name is given in the config file
			// If we don't have an Admin-Account don't try to load the organization!
			if(!APIManagerAdapter.hasAdminAccount()) {
				User user = APIManagerAdapter.getCurrentUser(false);
				if(!node.asText().equals(user.getOrganization().getName())) {
					LOG.warn("The given API-Organization is invalid as OrgAdmin user: '"+user.getName()+"' belongs to organization: '" + user.getOrganization().getName() + "'. API will be registered with OrgAdmin organization.");
				}
				return user.getOrganization();
			}
			// Otherwise make sure the organization exists and try to load it
			organization = APIManagerAdapter.getInstance().orgAdapter.getOrgForName(node.asText());
			if(organization==null && validateOrganization(ctxt)) {
				throw new AppException("The given organization: '"+node.asText()+"' is unknown.", ErrorCode.UNKNOWN_ORGANIZATION);
			}
			return organization;
		}
	}
	
	private boolean validateOrganization(DeserializationContext ctxt) {
		// By default the organization should be validated
		if(ctxt.getAttribute("validateOrganization")==null) return true;
		return (Boolean)ctxt.getAttribute("validateOrganization");
	}
}
