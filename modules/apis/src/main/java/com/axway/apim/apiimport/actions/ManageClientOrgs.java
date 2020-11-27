package com.axway.apim.apiimport.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;

public class ManageClientOrgs {
	
	static Logger LOG = LoggerFactory.getLogger(ManageClientOrgs.class);
	
	APIManagerAdapter apiManager;
	
	private API desiredState;
	private API actualState;

	public ManageClientOrgs(API desiredState, API actualState) throws AppException {
		this.desiredState = desiredState;
		this.actualState = actualState;
		apiManager = APIManagerAdapter.getInstance();
		
	}

	public void execute(boolean reCreation) throws AppException {
		if(CoreParameters.getInstance().isIgnoreClientOrgs()) {
			LOG.info("Configured client organizations are ignored, as flag ignoreClientOrgs has been set.");
			return;
		}
		if(desiredState.getState().equals(API.STATE_UNPUBLISHED)) return;
		// The API isn't Re-Created (to take over manually created ClientOrgs) and there are no orgs configured - We can skip the rest
		if(desiredState.getClientOrganizations()==null && !reCreation) return;
		// From here, the assumption is that existing Org-Access has been upgraded already - We only have to take care about additional orgs
		if((desiredState).isRequestForAllOrgs()) {
			LOG.info("Granting permission to all organizations");
			apiManager.apiAdapter.grantClientOrganization(getMissingOrgs(desiredState.getClientOrganizations(), actualState.getClientOrganizations()), actualState, true);
		} else {
			List<Organization> missingDesiredOrgs = getMissingOrgs(desiredState.getClientOrganizations(), actualState.getClientOrganizations());
			List<Organization> removingActualOrgs = getMissingOrgs(actualState.getClientOrganizations(), desiredState.getClientOrganizations());
			if(removingActualOrgs.remove( desiredState.getOrganization())); // Don't try to remove the Owning-Organization
			if(missingDesiredOrgs.size()==0) {
				if(desiredState.getClientOrganizations()!=null) {
					LOG.info("All desired organizations: "+desiredState.getClientOrganizations()+" have already access. Nothing to do.");
				}
			} else {
				apiManager.apiAdapter.grantClientOrganization(missingDesiredOrgs, actualState, false);
			}
			if(removingActualOrgs.size()>0) {
				if(CoreParameters.getInstance().getClientOrgsMode().equals(CoreParameters.Mode.replace)) {
					LOG.info("Removing access for orgs: "+removingActualOrgs+" from API: " + actualState.getName());
					apiManager.accessAdapter.removeClientOrganization(removingActualOrgs, actualState.getId());
				} else {
					LOG.info("NOT removing access for existing orgs: "+removingActualOrgs+" from API: " + actualState.getName() + " as clientOrgsMode NOT set to replace.");
				}
			}
		}
	}
	
	private List<Organization> getMissingOrgs(List<Organization> orgs, List<Organization> referenceOrgs) throws AppException {
		List<Organization> missingOrgs = new ArrayList<Organization>();
		if(orgs==null) return missingOrgs;
		if(referenceOrgs==null) return orgs; // Take over all orgs as missing
		for(Organization org : orgs) {
			if(referenceOrgs.contains(org)) {
				continue;
			}
			Organization organization =  apiManager.orgAdapter.getOrgForName(org.getName());
			if(organization==null) {
				LOG.warn("Configured organizations: " + apiManager.orgAdapter.getAllOrgs());
				ErrorState.getInstance().setError("Unknown Org-Name: '" + org.getName() + "'", ErrorCode.UNKNOWN_ORGANIZATION, false);
				throw new AppException("Unknown Org-Name: '" + org.getName() + "'", ErrorCode.UNKNOWN_ORGANIZATION);
			}
			missingOrgs.add(organization);
		}
		return missingOrgs;
	}
}
