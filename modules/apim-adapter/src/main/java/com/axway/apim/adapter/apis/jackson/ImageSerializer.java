package com.axway.apim.adapter.apis.jackson;

import java.io.IOException;

import com.axway.apim.api.model.Image;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ImageSerializer extends StdSerializer<Image> {
	
	private static final long serialVersionUID = 1L;

	public ImageSerializer() {
		this(null);
	}

	public ImageSerializer(Class<Image> t) {
		super(t);
	}

	@Override
	public void serialize(Image image, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		jgen.writeString(image.getBaseFilename());
	}
	

}
