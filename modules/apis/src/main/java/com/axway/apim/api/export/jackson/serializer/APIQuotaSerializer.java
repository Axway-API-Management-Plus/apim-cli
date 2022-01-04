package com.axway.apim.api.export.jackson.serializer;

import java.io.IOException;

import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.QuotaRestriction;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class APIQuotaSerializer extends StdSerializer<APIQuota> {
	
	private final JsonSerializer<Object> defaultSerializer;
	
	private static final long serialVersionUID = 1L;
	
	public APIQuotaSerializer(JsonSerializer<Object> defaultSerializer) {
		this(null, defaultSerializer);
	}

	public APIQuotaSerializer(Class<APIQuota> apiQuota, JsonSerializer<Object> defaultSerializer) {
		super(apiQuota);
		this.defaultSerializer = defaultSerializer;
		
	}

	@Override
	public void serialize(APIQuota apiQuota, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		// Set everything to null we want to have exported
		apiQuota.setDescription(null);
		apiQuota.setId(null);
		apiQuota.setName(null);
		apiQuota.setSystem(null);
		apiQuota.setType(null);
		for(QuotaRestriction restriction : apiQuota.getRestrictions()) {
			restriction.setApiId(null );
		}
		defaultSerializer.serialize(apiQuota, jgen, provider);
	}

}
