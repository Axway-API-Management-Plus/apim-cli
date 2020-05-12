package com.axway.apim.appimport.adapter.jackson;

import java.io.IOException;

import com.axway.apim.api.model.Image;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class ImageDeserializer extends StdDeserializer<Image> {
	
	private static final long serialVersionUID = 1L;
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public ImageDeserializer() {
		this(null);
	}

	public ImageDeserializer(Class<Image> image) {
		super(image);
	}

	@Override
	public Image deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		String credentialType = node.get("credentialType").asText();
		

		throw new RuntimeException("Unsupported credentialType: " + credentialType);
	}
}
