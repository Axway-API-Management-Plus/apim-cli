package com.axway.apim.api.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.ImageComparision;

public class Image {

	private byte[] imageContent = null;
	
	private String filename = null;
	
	private boolean isValid = true;
	
	private String baseFilename = null;
	
	private String contentType = null;
	
	private String fileExtension = null;
	
	public byte[] getImageContent() {
		return imageContent;
	}
	
	public Image() {
		super();
	}

	public Image(String filename) {
		super();
		this.filename = filename;
	}



	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof Image) {
			Image otherImage = (Image)other;
			// If the hashCode of both images are the same, it's completely the same image.
			if((Arrays.hashCode(this.imageContent)) == Arrays.hashCode(otherImage.getImageContent())) {
				return true;
			} else {
				// 
				return ImageComparision.compare(this.imageContent, otherImage.getImageContent());
			}
		} else {
			return false;
		}
	}
	
	public InputStream getInputStream() {
		return new ByteArrayInputStream(this.imageContent);
	}
	
	public String getFilename() {
		if(filename.indexOf("/")!=-1) {
			return filename;
		} else {
			return filename.substring(filename.lastIndexOf("/")+1);
		}
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getFileExtension() {
		if(this.fileExtension==null) {
			if(contentType==null) {
				fileExtension = ".unknown";
			} else if(contentType.toLowerCase().contains("jpeg") || contentType.toLowerCase().contains("jpg")) {
				fileExtension = ".jpg";
			} else if(contentType.toLowerCase().contains("png")) {
				fileExtension = ".jpg";
			} else if(contentType.toLowerCase().contains("gif")) {
				fileExtension = ".gif";
			} else {
				fileExtension = ".unknown";
			}
		}
		return fileExtension;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setImageContent(byte[] imageContent) {
		this.imageContent = imageContent;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public String getBaseFilename() {
		return baseFilename;
	}

	public void setBaseFilename(String baseFilename) {
		if(baseFilename.indexOf(".")==-1) {
			baseFilename += getFileExtension();
		}
		this.baseFilename = baseFilename;
	}

	@Override
	public String toString() {
		return "APIImage [bytes=" + this.imageContent.length + "]";
	}
	
	public static Image createImageFromFile(File file) throws AppException {
		Image image = new Image();
		try {
			image.setBaseFilename(file.getName());
			InputStream is = Image.class.getClass().getResourceAsStream(file.getCanonicalPath());
			if(file.exists()) {
				//LOG.debug("Loading image from: '"+file.getCanonicalFile()+"'");
				image.setImageContent(IOUtils.toByteArray(new FileInputStream(file)));
				return image;
			} else if(is!=null) {
				//LOG.debug("Trying to load image from classpath");
				// Try to read it from classpath
				image.setImageContent(IOUtils.toByteArray(is));
				return image;
			} else {
				throw new AppException("Image not found in filesystem ('"+file+"') or Classpath.", ErrorCode.UNXPECTED_ERROR);
			}
		} catch (Exception e) {
			throw new AppException("Can't read image-file: "+file+" from filesystem or classpath.", ErrorCode.UNXPECTED_ERROR, e);
		}
	}
}
