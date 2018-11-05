package com.axway.apim.swagger.api.properties;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APISwaggerDefinion {
	
	static Logger LOG = LoggerFactory.getLogger(APISwaggerDefinion.class);
	
	private String swaggerFile = null;
	
	private byte[] swaggerContent = null;
	
	//private String pathToSwaggerFile = null;

	public APISwaggerDefinion(byte[] swaggerContent) {
		this.swaggerContent = swaggerContent;
	}

	public String getSwaggerFile() {
		return swaggerFile;
	}

	public void setSwaggerFile(String swaggerFile) {
		this.swaggerFile = swaggerFile;
	}

	public byte[] getSwaggerContent() {
		return swaggerContent;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof APISwaggerDefinion) {
			APISwaggerDefinion otherSwagger = (APISwaggerDefinion)other;
			return (Arrays.hashCode(this.swaggerContent)) == Arrays.hashCode(otherSwagger.getSwaggerContent());
		} else {
			return false;
		}
	}

}
