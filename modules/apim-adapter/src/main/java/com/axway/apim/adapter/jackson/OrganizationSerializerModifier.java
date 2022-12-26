package com.axway.apim.adapter.jackson;

import com.axway.apim.api.model.Organization;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class OrganizationSerializerModifier extends BeanSerializerModifier {
	
	boolean serializeAsName;
	public OrganizationSerializerModifier(boolean serializeAsName) {
		super();
		this.serializeAsName = serializeAsName;
	}

	@Override
	public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
		if (beanDesc.getBeanClass() == Organization.class) {
			return new OrganizationSerializer(serializeAsName);
		}
		return super.modifySerializer(config, beanDesc, serializer);
	}
}
