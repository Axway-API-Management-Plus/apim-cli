package com.axway.apim.apiimport.rollback;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder.APIType;
import com.axway.apim.api.API;
import com.axway.apim.api.APIBaseDefinition;
import com.axway.apim.lib.errorHandling.AppException;

public class RollbackBackendAPI extends AbstractRollbackAction implements RollbackAction {
	
	/** This is the API to be deleted */
	API rollbackAPI;

	public RollbackBackendAPI(API rollbackAPI) {
		super();
		this.rollbackAPI = rollbackAPI;
		this.executeOrder = 20;
		this.name = "Backend-API";
	}

	@Override
	public void rollback() throws AppException {
		try {
			APIManagerAdapter.getInstance().apiAdapter.deleteBackendAPI(rollbackAPI);
			/*
			 * API-Manager 7.7 creates unfortunately two APIs at the same time, when importing a backend-API 
			 * having both schemas: https & http. 
			 * On top to that problem, only ONE backend-API-ID is returned when creating the BE-API-ID. The following 
			 * code tries to find the other Backend-API, which has been created almost at the same time.
			 */
			if(APIManagerAdapter.hasAPIManagerVersion("7.7")) {
				rolledBack = true;
				Long beAPICreatedOn = Long.parseLong( ((APIBaseDefinition)rollbackAPI).getCreatedOn() );
				// The createdOn of the API we are looking for, should be almost created at the same time, as the code runs internally in API-Manager.
				beAPICreatedOn = beAPICreatedOn - 1000;
				List<NameValuePair> filters = new ArrayList<NameValuePair>();
				filters.add(new BasicNameValuePair("field", "name"));
				filters.add(new BasicNameValuePair("op", "like"));
				filters.add(new BasicNameValuePair("value", rollbackAPI.getName()+ " HTTP"));
				// Filter on the createdOn date to execlude potentially already existing APIs with the same name,
				// as we only want to rollback the API which has been inserted by the actual tool run
				filters.add(new BasicNameValuePair("field", "createdOn"));
				filters.add(new BasicNameValuePair("op", "gt"));
				filters.add(new BasicNameValuePair("value", (beAPICreatedOn).toString())); // Ignore all other APIs some time ago
				API existingBEAPI = APIManagerAdapter.getInstance().apiAdapter.getAPI(new APIFilter.Builder(APIType.CUSTOM, true).useFilter(filters).build(), false);
				if(existingBEAPI!=null && existingBEAPI.getId()!=null) {
					APIManagerAdapter.getInstance().apiAdapter.deleteBackendAPI(existingBEAPI);
				}
			}
		} catch (Exception e) {
			LOG.error("Error while deleteting BE-API with ID: '"+rollbackAPI.getApiId()+"' to roll it back", e);
		}
		
	}
}
