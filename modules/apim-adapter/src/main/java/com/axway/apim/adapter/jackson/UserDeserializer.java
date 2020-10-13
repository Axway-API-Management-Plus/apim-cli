package com.axway.apim.adapter.jackson;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.model.User;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class UserDeserializer extends StdDeserializer<User> {
	
	public static enum DeserializeParams {
		useLoginName
	}
	
	static Logger LOG = LoggerFactory.getLogger(UserDeserializer.class);
	
	private static final long serialVersionUID = 1L;
	
	public UserDeserializer() {
		this(null);
	}

	public UserDeserializer(Class<User> user) {
		super(user);
	}

	@Override
	public User deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		User user;
		// Deserialization depends on the direction 
		// ConfigFile contains the LoginName / API-Manager provides the userId
		if(isUseLoginName(ctxt)) {
			user = new User();
			user.setLoginName(node.asText());
			return user;
		} else {
			user = new User();
			user.setId(node.asText());
			return user;
		}
	}
	
	private Boolean isUseLoginName(DeserializationContext ctxt) {
		if(ctxt.getAttribute(DeserializeParams.useLoginName)==null) return false;
		return (Boolean)ctxt.getAttribute(DeserializeParams.useLoginName);
	}
}
