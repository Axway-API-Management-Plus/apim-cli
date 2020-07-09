package com.axway.apim.organization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerOrganizationAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;

public class OrganizationImportManager {
	
	private static Logger LOG = LoggerFactory.getLogger(OrganizationImportManager.class);
	
	private APIManagerOrganizationAdapter orgAdapter;
	
	public OrganizationImportManager() throws AppException {
		super();
		this.orgAdapter = APIManagerAdapter.getInstance().orgAdapter;
	}

	public void replicate(Organization desiredOrg, Organization actualOrg) throws AppException {
		if(actualOrg==null) {
			orgAdapter.createOrganization(desiredOrg);
		} else if(orgsAreEqual(desiredOrg, actualOrg)) {
			LOG.debug("No changes detected between Desired- and Actual-Organization. Exiting now...");
			ErrorState.getInstance().setWarning("No changes detected between Desired- and Actual-Org.", ErrorCode.NO_CHANGE, false);
			throw new AppException("No changes detected between Desired- and Actual-Og.", ErrorCode.NO_CHANGE);			
		} else {
			LOG.debug("Update existing application");
			orgAdapter.updateOrganization(desiredOrg, actualOrg);
		}
	}
	
	private static boolean orgsAreEqual(Organization desiredOrg, Organization actualOrg) {
		return desiredOrg.deepEquals(actualOrg);
	}
}
