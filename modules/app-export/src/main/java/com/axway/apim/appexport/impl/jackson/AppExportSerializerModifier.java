package com.axway.apim.appexport.impl.jackson;

import com.axway.apim.api.model.apps.ClientAppCredential;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class AppExportSerializerModifier extends BeanSerializerModifier {
	
	

	@SuppressWarnings("unchecked")
	@Override
	public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
		if (beanDesc.getBeanClass().getSuperclass() == ClientAppCredential.class) {
			return new AppCredentialsSerializer((JsonSerializer<Object>) serializer);
		}
		return super.modifySerializer(config, beanDesc, serializer);
	}

}
