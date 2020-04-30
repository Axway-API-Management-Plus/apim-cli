package com.axway.apim.api.export.jackson.serializer;

import com.axway.apim.api.model.APIQuota;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class AIPQuotaSerializerModifier extends BeanSerializerModifier {
	
	

	@SuppressWarnings("unchecked")
	@Override
	public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
		if (beanDesc.getBeanClass() == APIQuota.class) {
			return new APIQuotaSerializer((JsonSerializer<Object>) serializer);
		}
		return super.modifySerializer(config, beanDesc, serializer);
	}

}
