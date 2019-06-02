package com.axway.apim.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.tasks.UpdateAPIStatus;
import com.axway.apim.actions.tasks.UpgradeAccessToNewerAPI;
import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.APIChangeState;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.state.APIBaseDefinition;
import com.axway.apim.swagger.api.state.IAPI;

/**
 * This class is used by the {@link APIManagerAdapter#applyChanges(APIChangeState)} to re-create an API. 
 * It's called, when an existing API is found, by at least one changed property can't be applied to the existing 
 * API.</br>
 * In that case, the desired API must be re-imported, completely updated (proxy, image, Quota, etc.), 
 * actual subscription must be taken over. It basically performs the same steps as when creating a new API, but 
 * having this separated in this class simplifies the code. 
 * 
 * @author cwiechmann@axway.com
 */
public class RecreateToUpdateAPI {
	
	static Logger LOG = LoggerFactory.getLogger(RecreateToUpdateAPI.class);

	public void execute(APIChangeState changes) throws AppException {
		
		IAPI actual = changes.getActualAPI();
		IAPI desired = changes.getDesiredAPI();
		
		// 1. Create BE- and FE-API (API-Proxy) / Including updating all belonging props!
		// This also includes all CONFIGURED application subscriptions and client-orgs
		// But not potentially existing Subscriptions or manually created Client-Orgs
		CreateNewAPI createNewAPI = new CreateNewAPI();
		createNewAPI.execute(changes);
		
		// 2. Create a new temp Desired-API-Definition, which will be used to delete the old API
		IAPI tempDesiredDeletedAPI = new APIBaseDefinition();
		
		if(actual.getState().equals(IAPI.STATE_PUBLISHED)) {
			// In case, the existing API is already in use (Published), we have to grant access to our new imported API
			new UpgradeAccessToNewerAPI(changes.getIntransitAPI(), changes.getActualAPI()).execute();
		}
		LOG.info("New API created. Going to delete old API.");
		// Delete the existing old API!
		((APIBaseDefinition)tempDesiredDeletedAPI).setStatus(IAPI.STATE_DELETED);
		new UpdateAPIStatus(tempDesiredDeletedAPI, actual).execute(true);
	}

}
