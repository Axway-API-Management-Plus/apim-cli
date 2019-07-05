package com.axway.apim.lib.rollback;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.axway.apim.actions.rest.DELRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.tasks.IResponseParser;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.swagger.APIManagerAdapter;
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
			RestAPICall apiCall = new DELRequest(uri, this, false);
			apiCall.execute();
			if(APIManagerAdapter.hasAPIManagerVersion("7.7")) {
				rolledBack = true;
				// There is very likely another BE-API, as API-Manager 7.7 is creating two Backend-API. One for HTTPS and one for HTTP
				List<NameValuePair> filters = new ArrayList<NameValuePair>();
				filters.add(new BasicNameValuePair("field", "name"));
				filters.add(new BasicNameValuePair("op", "like"));
				filters.add(new BasicNameValuePair("value", rollbackAPI.getName()+ " HTTP"));
				filters.add(new BasicNameValuePair("field", "createdOn"));
				filters.add(new BasicNameValuePair("op", "gt"));
				filters.add(new BasicNameValuePair("value", Long.toString(new Date().getTime()-120000))); // Ignore all API created more than 1 minute ago!
				JsonNode existingBEAPI = APIManagerAdapter.getInstance().getExistingAPI(null, filters, APIManagerAdapter.TYPE_BACK_END);
				if(existingBEAPI.get("id")!=null) {
					uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL())
							.setPath(RestAPICall.API_VERSION+"/apirepo/"+existingBEAPI.get("id").asText())
							.build();
					apiCall = new DELRequest(uri, this, false);
					apiCall.execute();
				}
			}
		} catch (Exception e) {
			LOG.error("Error while deleteting BE-API with ID: '"+rollbackAPI.getApiId()+"' to roll it back", e);
		}
		
	}

	@Override
	public JsonNode parseResponse(HttpResponse response) throws AppException {
		if(response.getStatusLine().getStatusCode()!=204) {
			rolledBack = false;
			try {
				LOG.error("Error while deleteting BE-API: '"+rollbackAPI.getApiId()+"' to roll it back: '"+EntityUtils.toString(response.getEntity())+"'");
			} catch (Exception e) {
				LOG.error("Error while deleteting FE-API: '"+rollbackAPI.getApiId()+"' to roll it back", e);
			}
			
		} else {
			rolledBack = true;
			LOG.debug("Successfully rolled back created BE-API: '"+rollbackAPI.getApiId()+"'");
		}
		
		return null;
	}
}
