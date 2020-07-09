package com.axway.apim.adapter.jackson;

import java.io.IOException;

import com.axway.apim.api.API;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class DeprecatedStateSerializer extends StdSerializer<String> {
	
	private static final long serialVersionUID = 1L;
	
	public DeprecatedStateSerializer() {
		this(null);
	}

	public DeprecatedStateSerializer(Class<String> state) {
		super(state);
	}

	@Override
	public void serialize(String state, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		if(state.equals(API.STATE_DEPRECATED)) {
			jgen.writeString(API.STATE_PUBLISHED);
		} else {
			jgen.writeString(state);
		}
	}
}
