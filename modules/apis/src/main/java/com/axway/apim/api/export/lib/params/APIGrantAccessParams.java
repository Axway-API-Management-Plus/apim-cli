package com.axway.apim.api.export.lib.params;

import java.util.List;

import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.Parameters;

public class APIGrantAccessParams extends APIExportParams implements Parameters, APIFilterParams {
	
	private List<API> apis;
	private List<Organization> orgs;
	
	private String orgId;
	private String orgName;
	
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public OrgFilter getOrganizationFilter() {
		return new OrgFilter.Builder()
				.hasId(orgId)
				.hasName(orgName)
				.build();
	}
	
	public APIFilter getAPIFilter() {
		return new APIFilter.Builder()
				.hasApiId(getId())
				.hasName(getName())
				.hasVHost(getVhost())
				.hasOrganization(getOrganization())
				.hasState(API.STATE_PUBLISHED) // Only published APIs are considered
				.build();
	}
	

	public List<API> getApis() {
		return apis;
	}
	public void setApis(List<API> apis) {
		this.apis = apis;
	}
	public List<Organization> getOrgs() {
		return orgs;
	}
	public void setOrgs(List<Organization> orgs) {
		this.orgs = orgs;
	}
}
