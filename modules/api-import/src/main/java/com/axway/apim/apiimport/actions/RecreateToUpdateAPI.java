package com.axway.apim.apiimport.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.API;
import com.axway.apim.api.APIBaseDefinition;
import com.axway.apim.apiimport.APIImportManager;
import com.axway.apim.apiimport.DesiredAPI;
import com.axway.apim.apiimport.actions.tasks.UpdateAPIStatus;
import com.axway.apim.apiimport.state.APIChangeState;
import com.axway.apim.lib.errorHandling.AppException;

/**
 * This class is used by the {@link APIImportManager#applyChanges(APIChangeState)} to re-create an API. 
 * It's called, when an existing API is found, by at least one changed property can't be applied to the existing 
 * API.  
 * In that case, the desired API must be re-imported, completely updated (proxy, image, Quota, etc.), 
 * actual subscription must be taken over. It basically performs the same steps as when creating a new API, but 
 * having this separated in this class simplifies the code. 
 * 
 * @author cwiechmann@axway.com
 */
public class RecreateToUpdateAPI {
	
	static Logger LOG = LoggerFactory.getLogger(RecreateToUpdateAPI.class);

	public void execute(APIChangeState changes) throws AppException {
		
		API actual = changes.getActualAPI();
		API desired = changes.getDesiredAPI();
		
		// On Re-Creation we need to restore the orginal given methodNames for methodLevel override
		desired.setInboundProfiles(((DesiredAPI)desired).getOriginalInboundProfiles());
		desired.setOutboundProfiles(((DesiredAPI)desired).getOriginalOutboundProfiles());
		
		// 1. Create BE- and FE-API (API-Proxy) / Including updating all belonging props!
		// This also includes all CONFIGURED application subscriptions and client-orgs
		// But not potentially existing Subscriptions or manually created Client-Orgs
		CreateNewAPI createNewAPI = new CreateNewAPI();
		createNewAPI.execute(changes, true);
		
		// 2. Create a new temp Desired-API-Definition, which will be used to delete the old API
		API tempDesiredDeletedAPI = new APIBaseDefinition();

		LOG.info("New API created. Going to delete old API.");
		// Delete the existing old API!
		((APIBaseDefinition)tempDesiredDeletedAPI).setStatus(API.STATE_DELETED);
		new UpdateAPIStatus(tempDesiredDeletedAPI, actual).execute(true);
	}

}
