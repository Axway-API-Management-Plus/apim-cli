package com.axway.apim.adapter.jackson;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerOrganizationAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class OrganizationDeserializer extends StdDeserializer<Organization> {

    private static final long serialVersionUID = 1L;

    public OrganizationDeserializer(Class<Organization> organization) {
        super(organization);
    }

    @Override
    public Organization deserialize(JsonParser jp, DeserializationContext context)
        throws IOException {
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        APIManagerOrganizationAdapter organizationAdapter = apiManagerAdapter.getOrgAdapter();
        JsonNode node = jp.getCodec().readTree(jp);
        // Deserialization depends on the direction
        if ("organizationId".equals(jp.currentName())) {
            // organizationId is given by API-Manager
            return organizationAdapter.getOrgForId(node.asText());
        } else {
            // Otherwise make sure the organization exists and try to load it
            Organization organization = organizationAdapter.getOrgForName(node.asText());
            if (organization == null && validateOrganization(context)) {
                throw new AppException("The given organization: '" + node.asText() + "' is unknown.", ErrorCode.UNKNOWN_ORGANIZATION);
            }
            return organization;
        }
    }

    private boolean validateOrganization(DeserializationContext ctxt) {
        // By default, the organization should be validated
        if (ctxt.getAttribute("validateOrganization") == null) return true;
        return (Boolean) ctxt.getAttribute("validateOrganization");
    }
}
