package com.axway.apim.adapter.apis.jackson;

import org.apache.commons.lang.StringUtils;

import com.axway.apim.api.API;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

public class StateSerializer extends BeanPropertyWriter {
	
	private boolean serializerStateAsDeprecated = true;
	
    private final BeanPropertyWriter writer;

	
	private static final long serialVersionUID = 1L;
	
    public StateSerializer(BeanPropertyWriter writer, boolean serializerStateAsDeprecated) {
        super(writer);
        this.writer = writer;
        this.serializerStateAsDeprecated = serializerStateAsDeprecated;
    }

    @Override
    public void serializeAsField(Object bean,
                                 JsonGenerator gen,
                                 SerializerProvider prov) throws Exception {
        Object value = writer.get(bean);
        if (value != null && value instanceof String) {
        	if(value.equals(API.STATE_DEPRECATED)) {
        		gen.writeStringField(writer.getName(), API.STATE_PUBLISHED);
        	} else {
        		gen.writeStringField(writer.getName(), (String)value);
        	}
        }
    }
}
