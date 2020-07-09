package com.axway.apim.api.export.jackson.serializer;

import java.io.IOException;

import com.axway.apim.api.model.Policy;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class PolicyToNameSerializer extends StdSerializer<Policy> {
	
	private static final long serialVersionUID = 1L;
	
	public PolicyToNameSerializer() {
		this(null);
	}

	public PolicyToNameSerializer(Class<Policy> policy) {
		super(policy);
	}

	@Override
	public void serialize(Policy policy, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		jgen.writeString(policy.getName());
	}

	@Override
	public Class<Policy> handledType() {
		return Policy.class;
	}
}
