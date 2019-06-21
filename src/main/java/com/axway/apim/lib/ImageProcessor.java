package com.axway.apim.lib;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
/*import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import com.vordel.api.common.BadRequestException;
import com.vordel.apiportal.api.portal.annotation.ImageMediaTypeUtils;
import com.vordel.common.base64.Decoder;
import com.vordel.common.base64.Encoder;*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageProcessor {
	
	private static Logger LOG = LoggerFactory.getLogger(ImageProcessor.class);
    
	private byte[] imageBytes;
	Map<String, String> responseMap = new HashMap<String, String>();
	
	/**
	 * Creates an image processor object using the given ByteArray and JPG-Image-Format as default. 
	 * @param in contains the image content as ByteArray 
	 */
	public ImageProcessor(byte[] in) {
	    this(in, "jpg");
	}

	/**
	 * Creates an image processor object using the given ByteArray and Image-Format. 
	 * @param in contains the image content as ByteArray 
	 * @param formatName what kind of image format (jpg, png, etc.)
	 */
    public ImageProcessor(byte[] in, String formatName) {
        try (final ByteArrayInputStream bis = new ByteArrayInputStream(in);
                final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            final BufferedImage is = ImageIO.read(bis);
            if (is != null) {
                final BufferedImage img = new BufferedImage(is.getWidth(), is.getHeight(), BufferedImage.TYPE_INT_RGB);
                final Graphics2D gfx = img.createGraphics();
                gfx.setColor(Color.WHITE);
                gfx.fillRect(0, 0, is.getWidth(), is.getHeight());
                gfx.drawRenderedImage(is, null);
                gfx.dispose();
                ImageIO.write(img, formatName, os);
                imageBytes = os.toByteArray();
            }
        } catch (IOException ex) {
            LOG.error("Error processing image", ex);
        }
    }

	public byte[] getImage() {
		return imageBytes;
	}
/*
	public String getImageString() {
		return Encoder.encode(imageBytes);
	}*/
}
