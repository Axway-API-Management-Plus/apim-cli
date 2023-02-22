package com.axway.apim.api.specification;

import com.axway.apim.api.API;
import com.axway.apim.api.model.ServiceProfile;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

public class WSDLSpecification extends APISpecification {

	private final Logger LOG = LoggerFactory.getLogger(WSDLSpecification.class);

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		return APISpecType.WSDL_API;
	}
	
	@Override
	public byte[] getApiSpecificationContent() {
		return this.apiSpecificationContent;
	}

	@Override
	public void updateBasePath(String basePath, String host) {

	}


	@Override
	public void configureBasePath(String backendBasePath, API api) throws AppException {
		// For SOAP services, the WSDL has not been adapted in any case, 
		// so the necessary service profile is to be created here.
		if(backendBasePath!=null) {
			ServiceProfile serviceProfile = new ServiceProfile();
			serviceProfile.setBasePath(backendBasePath);
			if(api.getServiceProfiles() == null) {
				api.setServiceProfiles(new LinkedHashMap<>());
			}
			api.getServiceProfiles().put("_default", serviceProfile);
		}
	}
	
	
	@Override
	public boolean parse(byte[] apiSpecificationContent) throws AppException {
		super.parse(apiSpecificationContent);
		if(apiSpecificationFile.toLowerCase().endsWith(".url")) {
			apiSpecificationFile = Utils.getAPIDefinitionUriFromFile(apiSpecificationFile);
		}
		if(apiSpecificationFile.toLowerCase().endsWith("?wsdl") ||
				apiSpecificationFile.toLowerCase().endsWith(".wsdl") ||
				apiSpecificationFile.toLowerCase().endsWith("?singlewsdl")) {
			return true;
		}
		if(new String(apiSpecificationContent, 0, 500).contains("wsdl")) {
			return true;
		}
		LOG.debug("No WSDL specification. Specification doesn't contain wsdl in the first 500 characters.");
		return false;
	}
	
	@Override
	public String getDescription() {
		return "";
	}
}
