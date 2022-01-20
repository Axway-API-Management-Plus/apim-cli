package com.axway.apim.api.definition;

import java.util.LinkedHashMap;

import com.axway.apim.api.API;
import com.axway.apim.api.model.ServiceProfile;
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
	public void configureBasepath(String backendBasepath, API api) throws AppException {
		// For SOAP services, the WSDL has not been adapted in any case, 
		// so the necessary service profile is to be created here.
		if(backendBasepath!=null) {
			ServiceProfile serviceProfile = new ServiceProfile();
			serviceProfile.setBasePath(backendBasepath);
			if(api.getServiceProfiles() == null) {
				api.setServiceProfiles(new LinkedHashMap<String, ServiceProfile>());
			}
			api.getServiceProfiles().put("_default", serviceProfile);
		}
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
