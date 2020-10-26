package com.axway.apim.adapter.jackson;

import java.io.IOException;

import com.axway.apim.api.model.Organization;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class OrganizationSerializer extends StdSerializer<Organization> {
	
	private static final long serialVersionUID = 1L;
	
	boolean serializeAsName = true;
	
	public OrganizationSerializer() {
		this(null);
	}
	
	public OrganizationSerializer(boolean serializeAsName) {
		this(null);
		this.serializeAsName = serializeAsName;
	}

	public OrganizationSerializer(Class<Organization> organization) {
		super(organization);
	}

	@Override
	public void serialize(Organization organization, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		if(serializeAsName) {
			jgen.writeString(organization.getName());
		} else {
			jgen.writeString(organization.getId());
		}
	}
}
