package com.axway.apim.lib.rollback;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.axway.apim.actions.rest.DELRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.actions.tasks.IResponseParser;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.state.APIBaseDefinition;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;

public class RollbackBackendAPI extends AbstractRollbackAction implements IResponseParser, RollbackAction {
	
	/** This is the API to be deleted */
	IAPI rollbackAPI;

	public RollbackBackendAPI(IAPI rollbackAPI) {
		super();
		this.rollbackAPI = rollbackAPI;
		this.executeOrder = 20;
		this.name = "Backend-API";
	}

	@Override
	public void rollback() throws AppException {
		try {
			URI uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL())
					.setPath(RestAPICall.API_VERSION+"/apirepo/"+rollbackAPI.getApiId())
					.build();
			Transaction.getInstance().put("apiIdToDelete", rollbackAPI.getApiId());
			RestAPICall apiCall = new DELRequest(uri, this, false);
			apiCall.execute();
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
				JsonNode existingBEAPI = APIManagerAdapter.getInstance().getExistingAPI(null, filters, APIManagerAdapter.TYPE_BACK_END, false);
				if(existingBEAPI.get("id")!=null) {
					uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL())
							.setPath(RestAPICall.API_VERSION+"/apirepo/"+existingBEAPI.get("id").asText())
							.build();
					Transaction.getInstance().put("apiIdToDelete", existingBEAPI.get("id"));
					apiCall = new DELRequest(uri, this, false);
					apiCall.execute();
				}
			}
		} catch (Exception e) {
			LOG.error("Error while deleteting BE-API with ID: '"+rollbackAPI.getApiId()+"' to roll it back", e);
		}
		
	}

	@Override
	public JsonNode parseResponse(HttpResponse httpResponse) throws AppException {
		try {
		if(httpResponse.getStatusLine().getStatusCode()!=204) {
			rolledBack = false;
			try {
				LOG.error("Error while deleteting BE-API: '"+Transaction.getInstance().get("apiIdToDelete")+"' to roll it back: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
			} catch (Exception e) {
				LOG.error("Error while deleteting FE-API: '"+Transaction.getInstance().get("apiIdToDelete")+"' to roll it back", e);
			}
			
		} else {
			rolledBack = true;
			LOG.debug("Successfully rolled back created BE-API: '"+rollbackAPI.getApiId()+"'");
		}
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
		return null;
	}
}
