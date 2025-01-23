package com.axway.apim.apiimport.lib.params;

import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardImportParams;

public class APIImportDatParams extends StandardImportParams implements Parameters {
    private String datPassword;
    private String orgName;
    private String apiDefinition;

    public String getDatPassword() {
        return datPassword;
    }

    public void setDatPassword(String datPassword) {
        this.datPassword = datPassword;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getApiDefinition() {
        return apiDefinition;
    }

    public void setApiDefinition(String apiDefinition) {
        this.apiDefinition = apiDefinition;
    }
}
