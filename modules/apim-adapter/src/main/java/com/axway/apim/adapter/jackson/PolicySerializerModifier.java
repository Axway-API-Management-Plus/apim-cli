package com.axway.apim.adapter.jackson;

import com.axway.apim.adapter.jackson.PolicySerializer;
import com.axway.apim.api.model.Policy;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class PolicySerializerModifier extends BeanSerializerModifier {
	
	boolean serializeAsName = false;
	
	
	public PolicySerializerModifier(boolean serializeAsName) {
		super();
		this.serializeAsName = serializeAsName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
		if (beanDesc.getBeanClass() == Policy.class) {
			return new PolicySerializer(serializeAsName);
		}
		return super.modifySerializer(config, beanDesc, serializer);
	}

}
