package com.axway.apim.api.definition;

import com.axway.apim.lib.errorHandling.AppException;

public class APISpecificationFactory {
	

	
	public static APISpecification getAPISpecification(byte[] apiSpecificationContent, String apiDefinitionSource, String backendBasepath) throws AppException {
		if(apiDefinitionSource.toLowerCase().endsWith("?wsdl") ||
				apiDefinitionSource.toLowerCase().endsWith(".wsdl") ||
				apiDefinitionSource.toLowerCase().endsWith("?singlewsdl")) {
			return new WSDLSpecification(apiSpecificationContent, backendBasepath);
		} else {
			return new Swagger20Specification(apiSpecificationContent, backendBasepath);
		}
	
	}
}
