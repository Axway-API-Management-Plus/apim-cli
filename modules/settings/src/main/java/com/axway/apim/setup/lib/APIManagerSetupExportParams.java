package com.axway.apim.setup.lib;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.StandardExportParams;

public class APIManagerSetupExportParams extends StandardExportParams {

    public enum Type {
        CONFIG,
        ALERTS,
        REMOTEHOSTS,
        POLICIES,
        CUSTOMPROPERTIES,
        GLOBALQUOTAS
    }

    private boolean exportConfig = true;
    private boolean exportAlerts = true;
    private boolean exportRemoteHosts = true;
    private boolean exportPolicies = true;
    private boolean exportCustomProperties = true;

    private boolean exportQuotas = true;

    private String remoteHostName;
    private String remoteHostId;

    public APIManagerSetupExportParams() {
        super();
    }

    public static synchronized APIManagerSetupExportParams getInstance() {
        return (APIManagerSetupExportParams) CoreParameters.getInstance();
    }

    public boolean isExportConfig() {
        return exportConfig;
    }

    public boolean isExportAlerts() {
        return exportAlerts;
    }

    public boolean isExportRemoteHosts() {
        return exportRemoteHosts;
    }

    public boolean isExportPolicies() {
        return exportPolicies;
    }

    public boolean isExportCustomProperties() {
        return exportCustomProperties;
    }

    public String getRemoteHostName() {
        return remoteHostName;
    }

    public void setRemoteHostName(String remoteHostName) {
        this.remoteHostName = remoteHostName;
    }

    public String getRemoteHostId() {
        return remoteHostId;
    }

    public void setRemoteHostId(String remoteHostId) {
        this.remoteHostId = remoteHostId;
    }

    public boolean isExportQuotas() {
        return exportQuotas;
    }

    public void setConfigType(String configType) {
        if (configType == null) return;
        // If a configType is given, set all types to false
        exportConfig = false;
        exportAlerts = false;
        exportRemoteHosts = false;
        exportPolicies = false;
        exportCustomProperties = false;
        exportQuotas = false;
        String[] givenTypes = configType.split(",");
        for (String givenType : givenTypes) {
            Type type = Type.valueOf(givenType.trim().toUpperCase());
            switch (type) {
                case CONFIG:
                    exportConfig = true;
                    break;
                case ALERTS:
                    exportAlerts = true;
                    break;
                case REMOTEHOSTS:
                    exportRemoteHosts = true;
                    break;
                case POLICIES:
                    exportPolicies = true;
                    break;
                case CUSTOMPROPERTIES:
                    exportCustomProperties = true;
                    break;
                case GLOBALQUOTAS:
                    exportQuotas = true;
                    break;
            }
        }
    }
}
