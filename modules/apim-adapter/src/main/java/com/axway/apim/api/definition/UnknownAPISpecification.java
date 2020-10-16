package com.axway.apim.api.definition;

import java.net.URL;

import com.axway.apim.lib.errorHandling.AppException;

public class UnknownAPISpecification extends APISpecification {
	
	String apiName;

	public UnknownAPISpecification(byte[] apiSpecificationContent, String apiName) throws AppException {
		super(apiSpecificationContent);
		this.apiName = apiName;
	}

	public UnknownAPISpecification() {
	}

	@Override
	public void configureBasepath(URL backendBasepath) throws AppException {
	}

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		return APISpecType.UNKNOWN;
	}

	@Override
	public boolean configure() throws AppException {
		return false;
	}

	@Override
	public byte[] getApiSpecificationContent() {
		LOG.error("API: '" + this.apiName + "' has a unkown/invalid API-Specification: " + APISpecificationFactory.getContentStart(this.apiSpecificationContent) );
		return super.getApiSpecificationContent();
	}
	
	
}
