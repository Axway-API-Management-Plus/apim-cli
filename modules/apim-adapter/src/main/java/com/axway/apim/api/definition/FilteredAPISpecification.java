package com.axway.apim.api.definition;

import com.axway.apim.api.API;
import com.axway.apim.lib.errorHandling.AppException;

public class FilteredAPISpecification extends APISpecification {
	
	private APISpecification apiSpecification;

	public FilteredAPISpecification(APISpecification apiSpecification)
			throws AppException {
		this.apiSpecification = apiSpecification;
	}

	@Override
	public void configureBasepath(String backendBasepath, API api) throws AppException {
		apiSpecification.configureBasepath(backendBasepath, api);
	}

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		return apiSpecification.getAPIDefinitionType();
	}

	@Override
	public byte[] getApiSpecificationContent() {
		return apiSpecification.getApiSpecificationContent();
	}
}
