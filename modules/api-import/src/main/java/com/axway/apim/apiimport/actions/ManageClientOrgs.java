package com.axway.apim.apiimport.actions;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter.Type;
import com.axway.apim.adapter.apis.APIManagerOrganizationAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.Organization;
import com.axway.apim.apiimport.DesiredAPI;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.DELRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.axway.apim.lib.utils.rest.Transaction;
import com.fasterxml.jackson.databind.JsonNode;

public class ManageClientOrgs {
	
	static Logger LOG = LoggerFactory.getLogger(ManageClientOrgs.class);
	
	private static String MODE					= "MODE";
	private static String MODE_GRANT_ACCESS		= "MODE_GRANT_ACCESS";
	private static String MODE_REMOVE_ACCESS	= "MODE_REMOVE_ACCESS";
	
	APIManagerAdapter apiManager;
	
	private API desiredState;
	private API actualState;

	public ManageClientOrgs(API desiredState, API actualState) throws AppException {
		this.desiredState = desiredState;
		this.actualState = actualState;
		apiManager = APIManagerAdapter.getInstance();
		
	}

	public void execute(boolean reCreation) throws AppException {
		if(CommandParameters.getInstance().isIgnoreClientOrgs()) {
			LOG.info("Configured client organizations are ignored, as flag ignoreClientOrgs has been set.");
			return;
		}
		if(desiredState.getState().equals(API.STATE_UNPUBLISHED)) return;
		// The API isn't Re-Created (to take over manually created ClientOrgs) and there are no orgs configured - We can skip the rest
		if(desiredState.getClientOrganizations()==null && !reCreation) return;
		// From here, the assumption is that existing Org-Access has been upgraded already - We only have to take care about additional orgs
		if(((DesiredAPI)desiredState).isRequestForAllOrgs()) {
			LOG.info("Granting permission to all organizations");
			apiManager.apiAdapter.grantClientOrganization(getMissingOrgs(desiredState.getClientOrganizations(), actualState.getClientOrganizations()), actualState, true);
		} else {
			List<Organization> missingDesiredOrgs = getMissingOrgs(desiredState.getClientOrganizations(), actualState.getClientOrganizations());
			List<Organization> removingActualOrgs = getMissingOrgs(actualState.getClientOrganizations(), desiredState.getClientOrganizations());
			if(removingActualOrgs.remove( ((API)desiredState).getOrganization())); // Don't try to remove the Owning-Organization
			if(missingDesiredOrgs.size()==0) {
				if(desiredState.getClientOrganizations()!=null) {
					LOG.info("All desired organizations: "+desiredState.getClientOrganizations()+" have already access. Nothing to do.");
				}
			} else {
				apiManager.apiAdapter.grantClientOrganization(missingDesiredOrgs, actualState, false);
			}
			if(removingActualOrgs.size()>0) {
				if(CommandParameters.getInstance().getClientOrgsMode().equals(CommandParameters.MODE_REPLACE)) {
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
		if(orgs==null || referenceOrgs ==null) return missingOrgs;
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
