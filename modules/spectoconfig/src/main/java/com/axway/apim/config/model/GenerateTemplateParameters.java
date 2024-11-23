package com.axway.apim.config.model;

import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardExportParams;

public class GenerateTemplateParameters extends StandardExportParams implements Parameters {

    private String apiDefinition;
    private String config;
    private String backendAuthType;
    private String frontendAuthType;
    private boolean includeMethods;
    private boolean inboundPerMethodOverride;

    public String getApiDefinition() {
        return apiDefinition;
    }

    public void setApiDefinition(String apiDefinition) {
        this.apiDefinition = apiDefinition;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getBackendAuthType() {
        return backendAuthType;
    }

    public void setBackendAuthType(String backendAuthType) {
        this.backendAuthType = backendAuthType;
    }

    public String getFrontendAuthType() {
        return frontendAuthType;
    }

    public void setFrontendAuthType(String frontendAuthType) {
        this.frontendAuthType = frontendAuthType;
    }

    public boolean isIncludeMethods() {
        return includeMethods;
    }

    public void setIncludeMethods(boolean includeMethods) {
        this.includeMethods = includeMethods;
    }

    public boolean isInboundPerMethodOverride() {
        return inboundPerMethodOverride;
    }

    public void setInboundPerMethodOverride(boolean inboundPerMethodOverride) {
        this.inboundPerMethodOverride = inboundPerMethodOverride;
    }
}
