package com.axway.apim.organization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerOrganizationAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;

public class OrganizationImportManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(OrganizationImportManager.class);
	
	private final APIManagerOrganizationAdapter orgAdapter;
	
	public OrganizationImportManager() throws AppException {
		super();
		this.orgAdapter = APIManagerAdapter.getInstance().orgAdapter;
	}

	public void replicate(Organization desiredOrg, Organization actualOrg) throws AppException {
		if(actualOrg==null) {
			orgAdapter.createOrganization(desiredOrg);
		} else if(orgsAreEqual(desiredOrg, actualOrg)) {
			LOG.debug("No changes detected between Desired- and Actual-Organization: {}" , desiredOrg.getName());
			throw new AppException("No changes detected between Desired- and Actual-Org: "+desiredOrg.getName()+".", ErrorCode.NO_CHANGE);			
		} else {
			LOG.debug("Update existing organization: {}" , desiredOrg.getName());
			orgAdapter.updateOrganization(desiredOrg, actualOrg);
			LOG.info("Successfully replicated organization: {} into API-Manager", desiredOrg.getName());
		}
	}
	
	private static boolean orgsAreEqual(Organization desiredOrg, Organization actualOrg) {
		return desiredOrg.deepEquals(actualOrg);
	}
}
