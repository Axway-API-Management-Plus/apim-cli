package com.axway.apim.apiimport.lib.params;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardImportParams;

public class APIImportParams extends StandardImportParams implements Parameters {

    private boolean forceUpdate;
    private boolean useFEAPIDefinition;
    private boolean validateRemoteHost;
    private boolean updateOnly;
    private boolean changeOrganization;
    private boolean referenceAPIRetire;
    private boolean referenceAPIDeprecate;
    private String referenceAPIRetirementDate;


    private String apiDefinition;
    public static synchronized APIImportParams getInstance() {
        return (APIImportParams) CoreParameters.getInstance();
    }
    public boolean isUseFEAPIDefinition() {
        return useFEAPIDefinition;
    }
    public void setUseFEAPIDefinition(boolean useFEAPIDefinition) {
        this.useFEAPIDefinition = useFEAPIDefinition;
    }
    public boolean isForceUpdate() {
        return forceUpdate;
    }
    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }
    public boolean isValidateRemoteHost() {
        return validateRemoteHost;
    }
    public void setValidateRemoteHost(boolean validateRemoteHost) {
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
    public void setUpdateOnly(boolean updateOnly) {
        this.updateOnly = updateOnly;
    }
    public boolean isChangeOrganization() {
        return changeOrganization;
    }
    public void setChangeOrganization(boolean changeOrganization) {
        this.changeOrganization = changeOrganization;
    }

    public boolean isReferenceAPIRetire() {
        return referenceAPIRetire;
    }

    public void setReferenceAPIRetire(boolean referenceAPIRetire) {
        this.referenceAPIRetire = referenceAPIRetire;
    }

    public String getReferenceAPIRetirementDate() {
        return referenceAPIRetirementDate;
    }

    public void setReferenceAPIRetirementDate(String referenceAPIRetirementDate) {
        this.referenceAPIRetirementDate = referenceAPIRetirementDate;
    }

    public boolean isReferenceAPIDeprecate() {
        return referenceAPIDeprecate;
    }

    public void setReferenceAPIDeprecate(boolean referenceAPIDeprecate) {
        this.referenceAPIDeprecate = referenceAPIDeprecate;
    }
}
