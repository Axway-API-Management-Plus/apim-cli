package com.axway.apim.api.definition;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class APISpecification {
	
	public static enum APISpecType {
		SWAGGGER_API_12("Swagger 1.2"),
		SWAGGGER_API_20("Swagger 2.0"),
		OPEN_API_30("Open API 3.0"),
		WSDL_API ("WSDL");
		
		String niceName;
		
		public String getNiceName() {
			return niceName;
		}

		APISpecType(String niceName) {
			this.niceName = niceName;
		}
	}
	
	static Logger LOG = LoggerFactory.getLogger(APISpecification.class);
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	protected String apiSpecificationFile = null;
	
	protected byte[] apiSpecificationContent = null;
	
	protected String backendBasepath;

	public APISpecification(byte[] apiSpecificationContent, String backendBasepath) throws AppException {
		super();
		this.apiSpecificationContent = apiSpecificationContent;
		this.backendBasepath = backendBasepath;
	}
	
	public APISpecification() {
		super();
	}

	public String getApiSpecificationFile() {
		return apiSpecificationFile;
	}

	public void setApiSpecificationFile(String apiSpecificationFile) {
		this.apiSpecificationFile = apiSpecificationFile;
	}

	public byte[] getApiSpecificationContent() {
		return apiSpecificationContent;
	}

	public void setApiSpecificationContent(byte[] apiSpecificationContent) {
		this.apiSpecificationContent = apiSpecificationContent;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof APISpecification) {
			APISpecification otherSwagger = (APISpecification)other;
			boolean rc = (Arrays.hashCode(this.apiSpecificationContent)) == Arrays.hashCode(otherSwagger.getApiSpecificationContent()); 
			if(!rc) {
				LOG.info("Detected API-Definition-Filesizes: API-Manager: " + this.apiSpecificationContent.length + " vs. Import: " + otherSwagger.getApiSpecificationContent().length);
			}
			return rc;
		} else {
			return false;
		}
	}
	
	protected abstract void configureBasepath() throws AppException;
	
	public abstract APISpecType getAPIDefinitionType() throws AppException;
	
	public abstract boolean configure() throws AppException;
}
