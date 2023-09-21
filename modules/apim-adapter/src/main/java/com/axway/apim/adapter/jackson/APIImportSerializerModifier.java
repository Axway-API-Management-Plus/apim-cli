package com.axway.apim.adapter.jackson;

import java.util.List;

import com.axway.apim.api.model.Policy;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class APIImportSerializerModifier extends BeanSerializerModifier {


	public APIImportSerializerModifier() {
		super();
	}

	@Override
	public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
			List<BeanPropertyWriter> beanProperties) {
		for(int i=0; i<beanProperties.size();i++) {
			BeanPropertyWriter writer = beanProperties.get(i);
			if(writer.getName().equals("state")) {
				beanProperties.set(i, new StateSerializer(writer));
				break;
			}
		}
		return beanProperties;
	}

	@Override
	public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
		if (beanDesc.getBeanClass() == Policy.class) {
			return new PolicySerializer(false);
		}
		return super.modifySerializer(config, beanDesc, serializer);
	}
}
