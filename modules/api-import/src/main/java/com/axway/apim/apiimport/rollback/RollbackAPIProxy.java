package com.axway.apim.apiimport.rollback;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.APIBaseDefinition;
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
		if(rollbackAPI.getState()!=null && rollbackAPI.getState().equals(API.STATE_PUBLISHED)) {
			API tempDesiredDeletedAPI = new APIBaseDefinition();
			((APIBaseDefinition)tempDesiredDeletedAPI).setStatus(API.STATE_UNPUBLISHED);
			new APIStatusManager().update(tempDesiredDeletedAPI, rollbackAPI, true);
		}
		try {
			if(rollbackAPI.getId()!=null) { // We already have an ID to the FE-API can delete it directly
				LOG.info("Rollback FE-API: '"+this.rollbackAPI.getName()+"' (ID: '"+this.rollbackAPI.getId()+"')");
				APIManagerAdapter.getInstance().apiAdapter.deleteAPIProxy(this.rollbackAPI);
			} else { // But during initial creation of the FE-API, in case of an error we don't even get the ID
				List<NameValuePair> filters = new ArrayList<NameValuePair>();
				filters.add(new BasicNameValuePair("field", "apiid"));
				filters.add(new BasicNameValuePair("op", "eq"));
				filters.add(new BasicNameValuePair("value", rollbackAPI.getApiId())); // To find the FE-API, we are using the BE-API-ID
				API existingAPI = APIManagerAdapter.getInstance().apiAdapter.getAPI(new APIFilter.Builder().useFilter(filters).build(), false);// The path is not set at this point, hence we provide null 
				LOG.info("Rollback FE-API: '"+existingAPI.getName()+"' (ID: '"+existingAPI.getId()+"')");
				APIManagerAdapter.getInstance().apiAdapter.deleteAPIProxy(existingAPI);
			}
		} catch (Exception e) {
			LOG.error("Error while deleteting FE-API with ID: '"+this.rollbackAPI.getId()+"' to roll it back", e);
		}
	}	
}
