package com.axway.apim.appexport.impl.jackson;

import java.io.IOException;

import com.axway.apim.api.model.apps.ClientAppCredential;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class AppCredentialsSerializer extends StdSerializer<ClientAppCredential> {
	
	private final JsonSerializer<Object> defaultSerializer;
	
	private static final long serialVersionUID = 1L;
	
	public AppCredentialsSerializer(JsonSerializer<Object> defaultSerializer) {
		this(null, defaultSerializer);
	}

	public AppCredentialsSerializer(Class<ClientAppCredential> credential, JsonSerializer<Object> defaultSerializer) {
		super(credential);
		this.defaultSerializer = defaultSerializer;
		
	}

	@Override
	public void serialize(ClientAppCredential credential, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		// Set everything to null we want to have exported
		credential.setId(null);
		defaultSerializer.serialize(credential, jgen, provider);
	}

}
