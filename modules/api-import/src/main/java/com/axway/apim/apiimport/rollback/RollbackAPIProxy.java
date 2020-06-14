package com.axway.apim.apiimport.rollback;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.lib.errorHandling.AppException;

public class RollbackAPIProxy extends AbstractRollbackAction implements RollbackAction {

	/** This is the API to be deleted */
	API rollbackAPI;

	public RollbackAPIProxy(API rollbackAPI) {
		super();
		this.rollbackAPI = rollbackAPI;
		executeOrder = 10;
		this.name = "Frontend-API";
	}

	@Override
	public void rollback() throws AppException {
		if(rollbackAPI.getActualState()!=null && rollbackAPI.getActualState().equals(API.STATE_PUBLISHED)) {
			new APIStatusManager().update(rollbackAPI, API.STATE_UNPUBLISHED, true);
		}
		try {
			if(rollbackAPI.getId()!=null) { // We already have an ID to the FE-API can delete it directly
				LOG.info("Rollback FE-API: '"+this.rollbackAPI.getName()+"' (ID: '"+this.rollbackAPI.getId()+"')");
				APIManagerAdapter.getInstance().apiAdapter.deleteAPIProxy(this.rollbackAPI);
			} else {
				// As we don't have the FE-API ID, try to find the FE-API, based on the BE-API-ID
				APIFilter filter = new APIFilter.Builder().hasApiId(rollbackAPI.getApiId()).build(); 
				API existingAPI = APIManagerAdapter.getInstance().apiAdapter.getAPI(filter, false);// The path is not set at this point, hence we provide null 
				LOG.info("Rollback FE-API: '"+existingAPI.getName()+"' (ID: '"+existingAPI.getId()+"')");
				APIManagerAdapter.getInstance().apiAdapter.deleteAPIProxy(existingAPI);
			}
		} catch (Exception e) {
			LOG.error("Error while deleting FE-API with ID: '"+this.rollbackAPI.getId()+"' to roll it back", e);
		}
	}	
}
