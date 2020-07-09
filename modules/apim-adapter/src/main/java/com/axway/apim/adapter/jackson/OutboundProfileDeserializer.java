package com.axway.apim.adapter.jackson;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import com.axway.apim.api.model.AuthenticationProfile;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class OutboundProfileDeserializer extends StdDeserializer<AuthenticationProfile> {
	
	private static final long serialVersionUID = 1L;
	
	public OutboundProfileDeserializer() {
		this(null);
	}

	public OutboundProfileDeserializer(Class<AuthenticationProfile> policy) {
		super(policy);
	}

	@Override
	public AuthenticationProfile deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		String policy = node.asText();
		if(StringUtils.isEmpty(policy)) return null;
		return null;
	}
}
