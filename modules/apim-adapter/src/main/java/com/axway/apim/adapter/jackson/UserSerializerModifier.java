package com.axway.apim.adapter.jackson;

import com.axway.apim.api.model.User;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class UserSerializerModifier extends BeanSerializerModifier {
	
	boolean serializeAsName;
	
	
	public UserSerializerModifier(boolean serializeAsName) {
		super();
		this.serializeAsName = serializeAsName;
	}

	@Override
	public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
		if (beanDesc.getBeanClass() == User.class) {
			return new UserSerializer(serializeAsName);
		}
		return super.modifySerializer(config, beanDesc, serializer);
	}

}
