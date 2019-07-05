package com.axway.apim.metadata.export.beans;

import java.util.List;

import com.axway.apim.swagger.api.properties.applications.ClientApplication;
import com.axway.apim.swagger.api.properties.organization.Organization;
import com.axway.apim.swagger.api.state.IAPI;

public class APIManagerExportMetadata {
	
	private List<ClientApplication> allApps;
	private List<Organization> allOrgs;
	
	private List<IAPI> allAPIs;

	public List<ClientApplication> getAllApps() {
		return allApps;
	}

	public void setAllApps(List<ClientApplication> allApps) {
		this.allApps = allApps;
	}

	public List<Organization> getAllOrgs() {
		return allOrgs;
	}

	public void setAllOrgs(List<Organization> allOrgs) {
		this.allOrgs = allOrgs;
	}

	public List<IAPI> getAllAPIs() {
		return allAPIs;
	}

	public void setAllAPIs(List<IAPI> allAPIs) {
		this.allAPIs = allAPIs;
	}
}
