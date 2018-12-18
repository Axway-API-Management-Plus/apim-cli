package com.axway.apim.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.tasks.UpdateAPIStatus;
import com.axway.apim.actions.tasks.UpdateQuotaConfiguration;
import com.axway.apim.actions.tasks.UpgradeAccessToNewerAPI;
import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.APIChangeState;
import com.axway.apim.swagger.api.APIBaseDefinition;
import com.axway.apim.swagger.api.IAPIDefinition;

public class RecreateToUpdateAPI {
	
	static Logger LOG = LoggerFactory.getLogger(RecreateToUpdateAPI.class);

	public void execute(APIChangeState changes) throws AppException {
		
		IAPIDefinition actual = changes.getActualAPI();
		IAPIDefinition desired = changes.getDesiredAPI();
		
		// 1. Create BE- and FE-API (API-Proxy) / Including updating all belonging props!
		CreateNewAPI createNewAPI = new CreateNewAPI();
		createNewAPI.execute(changes);
		LOG.info("New API created. Going to delete old API.");
		
		// 2. Create a new temp API-Definition, which will be used to apply changes to the existing actual API
		IAPIDefinition newActualAPI = new APIBaseDefinition();
		
		if(actual.getState().equals(IAPIDefinition.STATE_PUBLISHED)) {
			// In case, the existing API is already in use (Published), we have to grant access to our new imported API
			new UpgradeAccessToNewerAPI(changes.getIntransitAPI(), changes.getActualAPI()).execute();
		}
		
		// Delete the existing old API!
		((APIBaseDefinition)newActualAPI).setStatus(IAPIDefinition.STATE_DELETED);
		new UpdateAPIStatus(newActualAPI, actual).execute();
		new UpdateQuotaConfiguration(newActualAPI, actual).execute();
	}

}
