package com.axway.apim.adapter.apis.jackson;

import java.io.IOException;

import com.axway.apim.api.model.Organization;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class OrganizationSerializer extends StdSerializer<Organization> {
	
	private static final long serialVersionUID = 1L;
	
	public OrganizationSerializer() {
		this(null);
	}

	public OrganizationSerializer(Class<Organization> organization) {
		super(organization);
	}

	@Override
	public void serialize(Organization organization, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		jgen.writeString(organization.getId());
	}
}
