package com.axway.apim.appexport.impl.jackson;

import java.io.IOException;

import com.axway.apim.api.model.APIQuota;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class AppQuotaSerializer extends StdSerializer<APIQuota> {
	
	private final JsonSerializer<Object> defaultSerializer;
	
	private static final long serialVersionUID = 1L;
	
	public AppQuotaSerializer(JsonSerializer<Object> defaultSerializer) {
		this(null, defaultSerializer);
	}

	public AppQuotaSerializer(Class<APIQuota> quota, JsonSerializer<Object> defaultSerializer) {
		super(quota);
		this.defaultSerializer = defaultSerializer;
		
	}

	@Override
	public void serialize(APIQuota quota, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		// Set everything to null we don't want to have exported
		quota.setId(null);
		quota.setType(null);
		quota.setDescription(null);
		quota.setSystem(null);
		quota.setName(null);
		defaultSerializer.serialize(quota, jgen, provider);
	}
}
