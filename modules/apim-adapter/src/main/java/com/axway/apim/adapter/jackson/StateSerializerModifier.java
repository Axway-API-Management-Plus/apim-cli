package com.axway.apim.adapter.jackson;

import java.util.List;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class StateSerializerModifier extends BeanSerializerModifier {
	
	boolean serializeAsDeprecated = true;

	public StateSerializerModifier(boolean serializeAsDeprecated) {
		super();
		this.serializeAsDeprecated = serializeAsDeprecated;
	}

	@Override
	public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
			List<BeanPropertyWriter> beanProperties) {
		for(int i=0; i<beanProperties.size();i++) {
			BeanPropertyWriter writer = beanProperties.get(i);
			if(writer.getName().equals("state")) {
				beanProperties.set(i, new StateSerializer(writer, serializeAsDeprecated));
				break;
			}
		}
		return beanProperties;
	}
}
