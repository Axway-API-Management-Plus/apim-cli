package com.axway.apim.api.definition;

import com.axway.apim.lib.errorHandling.AppException;

public class UnknownAPISpecification extends APISpecification {

	public UnknownAPISpecification(byte[] apiSpecificationContent, String backendBasepath) throws AppException {
		super(apiSpecificationContent, backendBasepath);
	}

	public UnknownAPISpecification() {
	}

	@Override
	protected void configureBasepath() throws AppException {
	}

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		return APISpecType.UNKNOWN;
	}

	@Override
	public boolean configure() throws AppException {
		return false;
	}

}
