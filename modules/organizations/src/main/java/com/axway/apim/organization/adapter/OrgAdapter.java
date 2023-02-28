package com.axway.apim.organization.adapter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.Result;
import com.axway.apim.lib.error.AppException;

public abstract class OrgAdapter {
	
	static Logger LOG = LoggerFactory.getLogger(OrgConfigAdapter.class);
	
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
