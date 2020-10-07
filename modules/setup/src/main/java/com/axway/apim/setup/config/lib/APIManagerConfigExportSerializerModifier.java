package com.axway.apim.setup.config.lib;

import com.axway.apim.adapter.jackson.PolicySerializer;
import com.axway.apim.api.model.Policy;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class APIManagerConfigExportSerializerModifier extends BeanSerializerModifier {
	
	@SuppressWarnings("unchecked")
	@Override
	public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
		if (beanDesc.getBeanClass() == Policy.class) {
			return new PolicySerializer(true);
		}
		return super.modifySerializer(config, beanDesc, serializer);
	}

}
