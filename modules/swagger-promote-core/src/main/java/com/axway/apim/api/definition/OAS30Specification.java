package com.axway.apim.api.definition;

import com.axway.apim.api.IAPI;
import com.axway.apim.lib.errorHandling.AppException;

public class OAS30Specification extends APISpecification {

	@Override
	public int getAPIDefinitionType() throws AppException {
		return IAPI.OPEN_API_30;
	}

	@Override
	protected void configureBasepath() throws AppException {
	}
}
