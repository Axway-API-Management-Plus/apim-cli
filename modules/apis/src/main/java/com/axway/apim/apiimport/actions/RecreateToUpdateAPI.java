package com.axway.apim.apiimport.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.api.API;
import com.axway.apim.apiimport.APIChangeState;
import com.axway.apim.lib.error.AppException;

/**
 * This class is used by the APIImportManager#applyChanges(APIChangeState, boolean) to re-create an API. 
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
		LOG.info("Create new API to update existing: {} (ID: {})", actualAPI.getName(), actualAPI.getId());
		
		CreateNewAPI createNewAPI = new CreateNewAPI();
		createNewAPI.execute(changes, true);

		LOG.info("New API successfully created. Going to delete old API: {} {} (ID: {})", actualAPI.getName(), actualAPI.getVersion(), actualAPI.getId());
		// Delete the existing old API!
		new APIStatusManager().update(actualAPI, API.STATE_DELETED, true);
		
		// Maintain the Ehcache 
		// All cached entities referencing this API must be updated with the correct API-ID
		APIManagerAdapter.cacheManager.flipApiId(changes.getActualAPI().getId(), createNewAPI.getCreatedAPI().getId());
	}

}
