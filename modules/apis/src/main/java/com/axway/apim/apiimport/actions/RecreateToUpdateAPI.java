package com.axway.apim.apiimport.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.api.API;
import com.axway.apim.api.state.APIChangeState;
import com.axway.apim.apiimport.APIImportManager;
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
		
		API actualAPI = changes.getActualAPI();
		
		// 1. Create BE- and FE-API (API-Proxy) / Including updating all belonging props!
		// This also includes all CONFIGURED application subscriptions and client-orgs
		// But not potentially existing Subscriptions or manually created Client-Orgs
		LOG.info("Create new API to update existing: '"+actualAPI.getName()+"' (ID: "+actualAPI.getId()+")");
		
		CreateNewAPI createNewAPI = new CreateNewAPI();
		createNewAPI.execute(changes, true);

		LOG.info("New API successfuly created. Going to delete old API: '"+actualAPI.getName()+"' "+actualAPI.getVersion()+" (ID: "+actualAPI.getId()+")");
		// Delete the existing old API!
		new APIStatusManager().update(actualAPI, API.STATE_DELETED, true);
	}

}
