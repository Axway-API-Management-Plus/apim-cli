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

    public enum Params {
        /**
         * Don't load the organization from the API-Manager, only the organization ID is set. Used if the organization is not required
         * for all entities, which are deserialized. The organization can be loaded afterwards with APIManagerOrganizationAdapter.getOrgForId.
         * Only applies to the organizationId given by the API-Manager.
         */
        SKIP_LOOKUP
    }

    public OrganizationDeserializer() {
        this(null);
    }

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
            if (Boolean.TRUE.equals(context.getAttribute(Params.SKIP_LOOKUP))) {
                Organization organization = new Organization();
                organization.setId(node.asText());
                return organization;
            }
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
