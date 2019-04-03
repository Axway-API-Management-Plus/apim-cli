package com.axway.apim.swagger.api.properties;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.Utils;
import com.axway.apim.swagger.api.state.IAPI;

public class APIDefintion {
	
	static Logger LOG = LoggerFactory.getLogger(APIDefintion.class);
	
	private String apiDefinitionFile = null;
	
	private byte[] apiDefinitionContent = null;


	public APIDefintion(byte[] apiDefinitionContent) {
		this.apiDefinitionContent = apiDefinitionContent;
	}

	public String getAPIDefinitionFile() {
		return apiDefinitionFile;
	}

	public void setAPIDefinitionFile(String apiDefinitionFile) {
		this.apiDefinitionFile = apiDefinitionFile;
	}

	public byte[] getAPIDefinitionContent() {
		return apiDefinitionContent;
	}
	
	public int getAPIDefinitionType() throws AppException {
		String apiDefinitionSource = null;
		if(this.apiDefinitionFile.toLowerCase().endsWith(".url")) {
			apiDefinitionSource = Utils.getAPIDefinitionUriFromFile(this.apiDefinitionFile);
		} else {
			apiDefinitionSource = this.apiDefinitionFile;
		}
		if(apiDefinitionSource.toLowerCase().endsWith("?wsdl")) {
			return IAPI.WSDL_API;
		} else {
			return IAPI.SWAGGGER_API;
		}
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof APIDefintion) {
			APIDefintion otherSwagger = (APIDefintion)other;
			boolean rc = (Arrays.hashCode(this.apiDefinitionContent)) == Arrays.hashCode(otherSwagger.getAPIDefinitionContent()); 
			if(!rc) {
				LOG.info("Detected API-Definition-Filesizes: API-Manager: " + this.apiDefinitionContent.length + " vs. Import: " + otherSwagger.getAPIDefinitionContent().length);
			}
			return rc;
		} else {
			return false;
		}
	}

}
