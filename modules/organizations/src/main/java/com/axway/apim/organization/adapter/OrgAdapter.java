package com.axway.apim.organization.adapter;

import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.Result;
import com.axway.apim.lib.error.AppException;

import java.util.List;

public abstract class OrgAdapter {

	List<Organization> orgs;

	protected Result result;

	protected OrgAdapter() {
	}

	public abstract void readConfig() throws AppException;

	public List<Organization> getOrganizations() throws AppException {
		if(this.orgs==null) readConfig();
		return this.orgs;
	}
}
