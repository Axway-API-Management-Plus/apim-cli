package com.axway.apim.adapter.jackson;

import java.io.IOException;
import java.util.Base64;

import com.axway.apim.api.model.Image;
import com.axway.apim.lib.EnvironmentProperties;
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
    public void serialize(Image image, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        if (EnvironmentProperties.PRINT_CONFIG_CONSOLE) {
            byte[] imageData = image.getImageContent();
            Base64.Encoder encoder = Base64.getEncoder();
            String encodedData = encoder.encodeToString(imageData);
            String contentType = image.getContentType();
            jsonGenerator.writeString("data:" + contentType + ";base64," + encodedData);
        } else {
            jsonGenerator.writeString(image.getBaseFilename());
        }
    }
}
