package com.axway.apim.api.export.lib.params;

import java.util.List;

import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.adapter.client.apps.ClientAppFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.error.AppException;

public class APIGrantAccessParams extends APIExportParams implements Parameters, APIFilterParams {

	private List<API> apis;
	private List<Organization> orgs;

    private ClientApplication clientApplication;

	private String orgId;
	private String orgName;

    private String appId;
    private String appName;

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

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public OrgFilter getOrganizationFilter() {
		return new OrgFilter.Builder()
				.hasId(orgId)
				.hasName(orgName)
				.build();
	}

    public ClientAppFilter getApplicationFilter() throws AppException {
        return new ClientAppFilter.Builder()
            .hasId(appId)
            .hasName(appName)
            .build();
    }


    public APIFilter getAPIFilter() {
		return new APIFilter.Builder()
				.hasId(getId())
				.hasApiPath(getApiPath())
				.hasName(getName())
				.hasVHost(getVhost())
				.hasOrganization(getOrganization())
				.hasBackendBasepath(getBackend())
				.hasPolicyName(getPolicy())
				.hasInboundSecurity(getInboundSecurity())
				.hasTag(getTag())
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

    public ClientApplication getClientApplication() {
        return clientApplication;
    }

    public void setClientApplication(ClientApplication clientApplication) {
        this.clientApplication = clientApplication;
    }
}
