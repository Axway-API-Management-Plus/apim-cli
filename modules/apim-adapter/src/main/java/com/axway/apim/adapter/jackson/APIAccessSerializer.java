package com.axway.apim.adapter.jackson;

import java.io.IOException;
import java.util.List;

import com.axway.apim.api.model.APIAccess;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class APIAccessSerializer extends StdSerializer<List<APIAccess>> {
	
	private static final long serialVersionUID = 1L;
	
	public APIAccessSerializer() {
		this(null);
	}

	public APIAccessSerializer(Class<List<APIAccess>> apiAccess) {
		super(apiAccess);
	}

	@Override
	public void serialize(List<APIAccess> apiAccess, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		//provider.getConfig().
		jgen.writeStartArray();
		for(APIAccess access : apiAccess) {
			jgen.writeString(access.getApiId());
		}
		jgen.writeEndArray();
	}
}
