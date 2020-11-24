package com.axway.apim.apiimport.actions;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.api.API;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.apiimport.APIChangeState;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.CoreParameters.Mode;
import com.axway.apim.lib.errorHandling.AppException;

public class RepublishToUpdateAPI {
	
	static Logger LOG = LoggerFactory.getLogger(RepublishToUpdateAPI.class);

	public void execute(APIChangeState changes) throws AppException {
		
		API actualAPI = changes.getActualAPI();
		
		Mode clientAppsMode = CoreParameters.getInstance().getClientAppsMode();
		Mode clientOrgsMode =  CoreParameters.getInstance().getClientOrgsMode();
		// Get existing Orgs and Apps, as they will be lost when the API gets unpublished
		if(clientAppsMode==Mode.add && actualAPI.getApplications()!=null && changes.getDesiredAPI().getApplications()!=null) { 
			changes.getDesiredAPI().getApplications().addAll(actualAPI.getApplications());
			// Reset the applications to have them re-created 
			actualAPI.setApplications(new ArrayList<ClientApplication>());
		}
		if(clientOrgsMode==Mode.add && actualAPI.getClientOrganizations()!=null && changes.getDesiredAPI().getClientOrganizations()!=null) {
			// Take over existing organizations
			changes.getDesiredAPI().getClientOrganizations().addAll(actualAPI.getClientOrganizations());
			// Delete them, so that they are re-created
			actualAPI.setClientOrganizations(new ArrayList<Organization>());
		}
		
		// 1. Create BE- and FE-API (API-Proxy) / Including updating all belonging props!
		// This also includes all CONFIGURED application subscriptions and client-orgs
		// But not potentially existing Subscriptions or manually created Client-Orgs
		LOG.info("Unpublish existing API for update: '"+actualAPI.getName()+"' (ID: "+actualAPI.getId()+")");
		
		String actualState = changes.getActualAPI().getState();
		
		// Make sure the API state is restored during updateExisting
		if(!changes.getNonBreakingChanges().contains("state")) {
			changes.getNonBreakingChanges().add("state");
			changes.getDesiredAPI().setState(actualState);
		}
		
		APIStatusManager statusManager = new APIStatusManager();
		statusManager.update(actualAPI, API.STATE_UNPUBLISHED, true);
		
		UpdateExistingAPI updateExistingAPI = new UpdateExistingAPI();
		updateExistingAPI.execute(changes);
		
		LOG.debug("Existing API successfully updated: '"+actualAPI.getName()+"' (ID: "+actualAPI.getId()+")");
	}

}
