package com.axway.apim.setup.lib;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.StandardExportParams;

public class APIManagerSetupExportParams extends StandardExportParams {
	
	public enum Type {
		config, 
		alerts, 
		remotehosts, 
		policies,
		customProperties
	}
	
	private Boolean exportConfig = true;
	private Boolean exportAlerts = true;
	private Boolean exportRemoteHosts = true;
	private Boolean exportPolicies = true;
	private Boolean exportCustomProperties = true;
	
	private String RemoteHostName;
	private String RemoteHostId;

	public APIManagerSetupExportParams() {
	}
	
	public static synchronized APIManagerSetupExportParams getInstance() {
		return (APIManagerSetupExportParams)CoreParameters.getInstance();
	}

	public Boolean isExportConfig() {
		return exportConfig;
	}

	public Boolean isExportAlerts() {
		return exportAlerts;
	}

	public Boolean isExportRemoteHosts() {
		return exportRemoteHosts;
	}
	
	public Boolean isExportPolicies() {
		return exportPolicies;
	}
	
	public Boolean isExportCustomProperties() {
		return exportCustomProperties;
	}

	public String getRemoteHostName() {
		return RemoteHostName;
	}

	public void setRemoteHostName(String remoteHostName) {
		RemoteHostName = remoteHostName;
	}

	public String getRemoteHostId() {
		return RemoteHostId;
	}

	public void setRemoteHostId(String remoteHostId) {
		RemoteHostId = remoteHostId;
	}

	public void setExportConfig(Boolean exportConfig) {
		this.exportConfig = exportConfig;
	}

	public void setExportAlerts(Boolean exportAlerts) {
		this.exportAlerts = exportAlerts;
	}

	public void setExportRemoteHosts(Boolean exportRemoteHosts) {
		this.exportRemoteHosts = exportRemoteHosts;
	}

	public void setConfigType(String configType) {
		if(configType==null) return;
		// If a configType is given, set all types to false
		exportConfig = false;
		exportAlerts = false;
		exportRemoteHosts = false;
		exportPolicies = false;
		exportCustomProperties = false;
		String[] givenTypes = configType.split(",");
		for(String givenType : givenTypes) {
			Type type = Type.valueOf(givenType.trim());
			switch(type) {
			case config:
				exportConfig = true;
				break;
			case alerts:
				exportAlerts = true;
				break;
			case remotehosts:
				exportRemoteHosts = true;
				break;
			case policies:
				exportPolicies = true;
				break;
			case customProperties:
				exportCustomProperties = true;
				break;
			}
		}
	}
}
