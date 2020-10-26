package com.axway.apim.adapter.jackson;

import java.io.IOException;

import com.axway.apim.api.model.User;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class UserSerializer extends StdSerializer<User> {
	
	private static final long serialVersionUID = 1L;
	
	boolean serializeAsName = false;
	
	public UserSerializer(boolean serializeAsName) {
		this(null);
		this.serializeAsName = serializeAsName;
	}

	public UserSerializer(Class<User> user) {
		super(user);
	}

	@Override
	public void serialize(User user, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		if(serializeAsName) {
			jgen.writeString(user.getLoginName());
		} else {
			jgen.writeString(user.getId());
		}
	}
	
	@Override
	public Class<User> handledType() {
		return User.class;
	}
}
