package com.axway.apim.api.definition;

import com.axway.apim.api.IAPI;
import com.axway.apim.lib.errorHandling.AppException;

public class Swagger12Specification extends APISpecification {

	@Override
	public int getAPIDefinitionType() throws AppException {
		return IAPI.SWAGGGER_API_12;
	}

	@Override
	protected void configureBasepath() throws AppException {
	}
}
