package com.axway.apim.adapter.jackson;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.errorHandling.AppException;
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
		User user = null;
		// Deserialization depends on the direction 
		// ConfigFile contains the LoginName / API-Manager provides the userId
		if(isUseLoginName(ctxt)) {
			// Try to initialize this user based on the loginname
			try {
				user = APIManagerAdapter.getInstance().userAdapter.getUserForLoginName(node.asText());
			} catch (AppException e) {
				LOG.error("Error reading user with loginName: " + node.asText() + " from API-Manager.");
			}
			if(user!=null) return user;
			// User might be null in some situations, create a new user with base init
			user = new User();
			user.setLoginName(node.asText());
			return user;
		} else {
			// Try to initialize this user based on the User-ID
			try {
				user = APIManagerAdapter.getInstance().userAdapter.getUserForId(node.asText());
			} catch (AppException e) {
				LOG.error("Error reading user with ID: " + node.asText() + " from API-Manager.");
			}
			if(user!=null) return user;
			// This this should never happen, as the ID must be a valid API-Manager User-ID
			LOG.error("User with ID: " + node.asText() + " not found.");
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
