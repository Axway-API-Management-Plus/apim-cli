package com.axway.apim.swagger.api.properties;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

public class APIImage {

	private byte[] imageContent = null;
	
	private String filename = null;
	
	private boolean isValid = true;

	public APIImage(byte[] imageContent, String filename) {
		this.imageContent = imageContent;
		if(imageContent == null) {
			this.isValid = false;
		}
		this.filename = filename;
	}	
	
	public byte[] getImageContent() {
		return imageContent;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof APIImage) {
			APIImage otherImage = (APIImage)other;
			return (Arrays.hashCode(this.imageContent)) == Arrays.hashCode(otherImage.getImageContent());
		} else {
			return false;
		}
	}
	
	public InputStream getInputStream() {
		return new ByteArrayInputStream(this.imageContent);
	}
	
	public String getFilename() {
		if(filename.indexOf("/")==-1) {
			return filename;
		} else {
			return filename.substring(filename.lastIndexOf("/")+1);
		}
	}

	public boolean isValid() {
		return isValid;
	}
}
