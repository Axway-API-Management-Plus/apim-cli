package com.axway.apim.promote.mvn.plugin;

/**
 * Mojo class for API and associated config file
 *
 */
public class Api {
    private String apiSpecification;

    private String apiConfig;

    /**
     * @return api specification file path
     */
    public String getApiSpecification() {
        return apiSpecification;
    }

    public Api setApiSpecification(final String apiSpecification) {
        this.apiSpecification = apiSpecification;
        return this;
    }

    /**
     *
     * @return api configuration (contract) file path
     */
    public String getApiConfig() {
        return apiConfig;
    }

    public Api setApiConfig(final String apiConfig) {
        this.apiConfig = apiConfig;
        return this;
    }

    public String toString() {
       return String.format("API specification: %s \n API apiConfig: %s", getApiSpecification(), getApiConfig());
    }
}
