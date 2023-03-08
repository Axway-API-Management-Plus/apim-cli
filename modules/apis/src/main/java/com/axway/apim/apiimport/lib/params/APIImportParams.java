package com.axway.apim.apiimport.lib.params;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardImportParams;

public class APIImportParams extends StandardImportParams implements Parameters {

    private Boolean forceUpdate;
    private Boolean useFEAPIDefinition;
    private Boolean validateRemoteHost;
    private Boolean updateOnly = false;
    private Boolean changeOrganization = false;
    private String apiDefinition;
    public static synchronized APIImportParams getInstance() {
        return (APIImportParams) CoreParameters.getInstance();
    }

    public Boolean isUseFEAPIDefinition() {
        if (useFEAPIDefinition == null) return false;
        return useFEAPIDefinition;
    }

    public void setUseFEAPIDefinition(Boolean useFEAPIDefinition) {
        if (useFEAPIDefinition == null) return;
        this.useFEAPIDefinition = useFEAPIDefinition;
    }

    public Boolean isForceUpdate() {
        if (forceUpdate == null) return false;
        return forceUpdate;
    }

    public void setForceUpdate(Boolean forceUpdate) {
        if (forceUpdate == null) return;
        this.forceUpdate = forceUpdate;
    }

    public Boolean isValidateRemoteHost() {
        return validateRemoteHost;
    }

    public void setValidateRemoteHost(Boolean validateRemoteHost) {
        this.validateRemoteHost = validateRemoteHost;
    }

    public String getApiDefinition() {
        return apiDefinition;
    }

    public void setApiDefinition(String apiDefinition) {
        this.apiDefinition = apiDefinition;
    }

    public Boolean isUpdateOnly() {
        return updateOnly;
    }

    public void setUpdateOnly(Boolean updateOnly) {
        this.updateOnly = updateOnly;
    }

    public Boolean isChangeOrganization() {
        return changeOrganization;
    }

    public void setChangeOrganization(Boolean changeOrganization) {
        if (changeOrganization == null) return;
        this.changeOrganization = changeOrganization;
    }
}
