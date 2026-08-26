package com.axway.apim.adapter.jackson;

import com.axway.apim.lib.utils.Constants;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class StateSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.equals(Constants.API_DEPRECATED)) {
                gen.writeString(Constants.API_PUBLISHED);
            } else {
                gen.writeString(strValue);
            }
        }
    }
}
