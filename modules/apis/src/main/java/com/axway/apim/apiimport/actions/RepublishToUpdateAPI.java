package com.axway.apim.apiimport.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.api.API;
import com.axway.apim.apiimport.APIChangeState;
import com.axway.apim.lib.errorHandling.AppException;

public class RepublishToUpdateAPI {
	
	static Logger LOG = LoggerFactory.getLogger(RepublishToUpdateAPI.class);

	public void execute(APIChangeState changes) throws AppException {
		
		API actualAPI = changes.getActualAPI();
		
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
