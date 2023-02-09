package com.axway.apim.appexport.impl.jackson;

import java.io.File;
import java.io.IOException;

import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.OAuth;
import com.axway.apim.appexport.impl.JsonApplicationExporter;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class AppCredentialsSerializer extends StdSerializer<ClientAppCredential> {

	private final transient JsonSerializer<Object> defaultSerializer;
	
	private File localFolder;
	
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
		if(credential instanceof OAuth) {
			try {
				if(((OAuth)credential).getCert()!=null) {
					JsonApplicationExporter.storeCaCert(localFolder, ((OAuth)credential).getCert(), "app-oauth-cert.crt");
					((OAuth)credential).setCert("app-oauth-cert.crt");
				}
			} catch (AppException e) {
				throw new IOException("Can't write certificate file", e);
			}
		}
		defaultSerializer.serialize(credential, jgen, provider);
	}

	public void setExportFolder(File localFolder) {
		this.localFolder = localFolder;
	}
}
