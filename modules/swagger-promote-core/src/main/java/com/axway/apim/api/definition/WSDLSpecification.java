package com.axway.apim.api.definition;

import com.axway.apim.api.IAPI;
import com.axway.apim.lib.errorHandling.AppException;

public class WSDLSpecification extends APISpecification {
	
	public WSDLSpecification(byte[] apiSpecificationContent, String backendBasepath) throws AppException {
		super(apiSpecificationContent, backendBasepath);
	}

	@Override
	public int getAPIDefinitionType() throws AppException {
		return IAPI.WSDL_API;
	}

	@Override
	protected void configureBasepath() throws AppException {
		
	}
}
