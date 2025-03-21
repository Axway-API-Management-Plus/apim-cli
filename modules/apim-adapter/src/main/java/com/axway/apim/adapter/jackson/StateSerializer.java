package com.axway.apim.adapter.jackson;

import com.axway.apim.lib.utils.Constants;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

public class StateSerializer extends BeanPropertyWriter {


    private final BeanPropertyWriter writer;


	private static final long serialVersionUID = 1L;

    public StateSerializer(BeanPropertyWriter writer) {
        super(writer);
        this.writer = writer;
    }

    @Override
    public void serializeAsField(Object bean,
                                 JsonGenerator gen,
                                 SerializerProvider prov) throws Exception {
        Object value = writer.get(bean);
        if (value instanceof String) {
        	if(value.equals(Constants.API_DEPRECATED)) {
        		gen.writeStringField(writer.getName(), Constants.API_PUBLISHED);
        	} else {
        		gen.writeStringField(writer.getName(), (String)value);
        	}
        }
    }
}
