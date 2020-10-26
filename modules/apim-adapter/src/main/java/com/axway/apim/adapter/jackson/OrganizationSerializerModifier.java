package com.axway.apim.adapter.jackson;

import com.axway.apim.api.model.Organization;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class OrganizationSerializerModifier extends BeanSerializerModifier {
	
	boolean serializeAsName = false;
	
	
	public OrganizationSerializerModifier(boolean serializeAsName) {
		super();
		this.serializeAsName = serializeAsName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
		if (beanDesc.getBeanClass() == Organization.class) {
			return new OrganizationSerializer(serializeAsName);
		}
		return super.modifySerializer(config, beanDesc, serializer);
	}

}
