package com.axway.apim.api.definition;

import com.axway.apim.api.API;
import com.axway.apim.lib.errorHandling.AppException;

public class UnknownAPISpecification extends APISpecification {
	
	String apiName;

	public UnknownAPISpecification(String apiName) throws AppException {
		this.apiName = apiName;
	}

	public UnknownAPISpecification() {
	}

	@Override
	public void configureBasepath(String backendBasepath, API api) throws AppException {
	}

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		return APISpecType.UNKNOWN;
	}

	@Override
	public boolean parse(byte[] apiSpecificationContent) throws AppException {
		return false;
	}

	@Override
	public byte[] getApiSpecificationContent() {
		LOG.error("API: '" + this.apiName + "' has a unkown/invalid API-Specification: " + APISpecificationFactory.getContentStart(this.apiSpecificationContent) );
		return super.getApiSpecificationContent();
	}
	
	
}
