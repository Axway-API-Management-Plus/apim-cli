package com.axway.apim.api.definition;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;

public class WSDLSpecification extends APISpecification {
	
	JsonNode wsdl = null;
	
	public WSDLSpecification(byte[] apiSpecificationContent) throws AppException {
		super(apiSpecificationContent);
	}

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		return APISpecType.WSDL_API;
	}

	@Override
	public void configureBasepath(String backendBasepath) throws AppException {
		
	}
	
	
	@Override
	public boolean configure() throws AppException {
		if(apiSpecificationFile.toLowerCase().endsWith(".url")) {
			apiSpecificationFile = Utils.getAPIDefinitionUriFromFile(apiSpecificationFile);
		}
		if(apiSpecificationFile.toLowerCase().endsWith("?wsdl") ||
				apiSpecificationFile.toLowerCase().endsWith(".wsdl") ||
				apiSpecificationFile.toLowerCase().endsWith("?singlewsdl")) {
			return true;
		}
		if(new String(this.apiSpecificationContent, 0, 500).contains("wsdl")) {
			return true;
		}
		LOG.debug("No WSDL specification. Specification doesn't contain wsdl in the first 500 characters.");
		return false;
	}
}
