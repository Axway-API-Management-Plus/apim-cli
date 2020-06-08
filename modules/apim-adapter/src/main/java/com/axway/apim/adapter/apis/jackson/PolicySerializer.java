package com.axway.apim.adapter.apis.jackson;

import java.io.IOException;

import com.axway.apim.api.model.Policy;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class PolicySerializer extends StdSerializer<Policy> {
	
	private static final long serialVersionUID = 1L;
	
	public PolicySerializer() {
		this(null);
	}

	public PolicySerializer(Class<Policy> policy) {
		super(policy);
	}

	@Override
	public void serialize(Policy policy, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		jgen.writeString(policy.getId());
	}
}
