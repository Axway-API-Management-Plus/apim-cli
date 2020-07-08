package com.axway.apim.organization.adapter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.errorHandling.AppException;

public abstract class OrgAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(JSONOrgAdapter.class);
	
	List<Organization> orgs;

	public OrgAdapter() {
		// TODO Auto-generated constructor stub
	}
	
	public abstract boolean readConfig(Object config) throws AppException;
	
	public List<Organization> getOrganizations() throws AppException {
		return this.orgs;
	}
}
