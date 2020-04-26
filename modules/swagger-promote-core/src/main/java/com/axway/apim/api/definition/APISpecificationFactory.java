package com.axway.apim.api.definition;

import com.axway.apim.lib.errorHandling.AppException;

public class APISpecificationFactory {
	

	
	public static APISpecification getAPISpecification(byte[] apiSpecificationContent, String apiDefinitionSource, String backendBasepath) throws AppException {
		APISpecification spec;
		if(apiDefinitionSource.toLowerCase().endsWith("?wsdl") ||
				apiDefinitionSource.toLowerCase().endsWith(".wsdl") ||
				apiDefinitionSource.toLowerCase().endsWith("?singlewsdl")) {
			spec = new WSDLSpecification(apiSpecificationContent, backendBasepath);
		} else {
			spec = new Swagger20Specification(apiSpecificationContent, backendBasepath);
		}
		spec.setApiSpecificationFile(apiDefinitionSource);
		return spec;
	}
}
