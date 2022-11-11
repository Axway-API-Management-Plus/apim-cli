package com.axway.apim.api.apiSpecification;

import com.axway.apim.api.API;
import com.axway.apim.lib.errorHandling.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnknownAPISpecification extends APISpecification {

	private final Logger LOG = LoggerFactory.getLogger(UnknownAPISpecification.class);
	String apiName;

	public UnknownAPISpecification(String apiName) {
		this.apiName = apiName;
	}

	@Override
	public void configureBasePath(String backendBasePath, API api) throws AppException {
	}

	@Override
	public APISpecType getAPIDefinitionType() throws AppException {
		return APISpecType.UNKNOWN;
	}

	@Override
	public boolean parse(byte[] apiSpecificationContent) throws AppException {
		return false;
	}

	@Override
	public byte[] getApiSpecificationContent() {
		LOG.error("API: '" + this.apiName + "' has a unkown/invalid API-Specification: " + APISpecificationFactory.getContentStart(this.apiSpecificationContent) );
		return this.apiSpecificationContent;
	}

	@Override
	public String getDescription() {
		return "";
	}
}
