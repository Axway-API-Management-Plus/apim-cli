package com.axway.apim.adapter.jackson;

import java.io.IOException;

import com.axway.apim.api.model.apps.APIKey;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.ExtClients;
import com.axway.apim.api.model.apps.OAuth;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class AppCredentialsDeserializer extends StdDeserializer<ClientAppCredential> {
	
	private static final long serialVersionUID = 1L;
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public AppCredentialsDeserializer() {
		this(null);
	}

	public AppCredentialsDeserializer(Class<ClientAppCredential> credential) {
		super(credential);
	}

	@Override
	public ClientAppCredential deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		String credentialType = node.get("credentialType").asText();
		ClientAppCredential cred;
		try {
			switch (credentialType) {
				case "oauth":
					cred = objectMapper.treeToValue(node, OAuth.class);
					cred.setId(node.get("clientId").asText());
					break;
				case "extclients":
					cred = objectMapper.treeToValue(node, ExtClients.class);
					cred.setId(node.get("clientId").asText());
					break;
				case "apikeys":
					cred = objectMapper.treeToValue(node, APIKey.class);
					cred.setId(node.get("apiKey").asText());
					break;
				default:
					throw new RuntimeException("Unsupported credentialType: " + credentialType);
			}
		} catch (NullPointerException e) {
			throw new IOException("Cannot parse application credential type: " + credentialType + ". Please make sure 'clientId' or 'apiKey' is given.");
		}
		return cred;
	}
}
