package com.axway.apim.api.specification;

import com.axway.apim.api.API;
import com.axway.apim.lib.error.AppException;
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
        // Not required
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
        LOG.error("API: {} has a unknown/invalid API-Specification: {}", this.apiName, APISpecificationFactory.getContentStart(this.apiSpecificationContent));
        return this.apiSpecificationContent;
    }

    @Override
    public void updateBasePath(String basePath, String host) {
        // Not required
    }

    @Override
    public String getDescription() {
        return "";
    }
}
