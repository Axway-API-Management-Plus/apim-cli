package com.axway.apim.lib.rollback;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.axway.apim.actions.tasks.IResponseParser;
import com.axway.apim.actions.tasks.UpdateAPIStatus;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.Proxies;
import com.axway.apim.api.state.APIBaseDefinition;
import com.axway.apim.api.state.IAPI;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.rest.DELRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.databind.JsonNode;

public class RollbackAPIProxy extends AbstractRollbackAction implements IResponseParser, RollbackAction {

	/** This is the API to be deleted */
	IAPI rollbackAPI;

	public RollbackAPIProxy(IAPI rollbackAPI) {
		super();
		this.rollbackAPI = rollbackAPI;
		executeOrder = 10;
		this.name = "Frontend-API";
	}

	@Override
	public void rollback() throws AppException {
		if(rollbackAPI.getState()!=null && rollbackAPI.getState().equals(IAPI.STATE_PUBLISHED)) {
			IAPI tempDesiredDeletedAPI = new APIBaseDefinition();
			((APIBaseDefinition)tempDesiredDeletedAPI).setStatus(IAPI.STATE_UNPUBLISHED);
			new UpdateAPIStatus(tempDesiredDeletedAPI, rollbackAPI).execute(true);
		}
		URI uri;
		try {
			if(rollbackAPI.getId()!=null) { // We already have an ID to the FE-API can delete it directly
				LOG.info("Rollback FE-API: '"+this.rollbackAPI.getName()+"' (ID: '"+this.rollbackAPI.getId()+"')");
				uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL())
						.setPath(RestAPICall.API_VERSION+"/proxies/"+this.rollbackAPI.getId())
						.build();
			} else { // But during initial creation of the FE-API, in case of an error we don't even get the ID
				List<NameValuePair> filters = new ArrayList<NameValuePair>();
				filters.add(new BasicNameValuePair("field", "apiid"));
				filters.add(new BasicNameValuePair("op", "eq"));
				filters.add(new BasicNameValuePair("value", rollbackAPI.getApiId())); // To find the FE-API, we are using the BE-API-ID
				JsonNode existingAPI = new Proxies.Builder(APIManagerAdapter.TYPE_FRONT_END).useFilter(filters).build().getAPI(false); // The path is not set at this point, hence we provide null 
				LOG.info("Rollback FE-API: '"+existingAPI.get("name").asText()+"' (ID: '"+existingAPI.get("id").asText()+"')");
				uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL())
						.setPath(RestAPICall.API_VERSION+"/proxies/"+existingAPI.get("id").asText())
						.build();
			}
			RestAPICall apiCall = new DELRequest(uri, this, false);
			apiCall.execute();
		} catch (Exception e) {
			LOG.error("Error while deleteting FE-API with ID: '"+this.rollbackAPI.getId()+"' to roll it back", e);
		}
	}

	@Override
	public JsonNode parseResponse(HttpResponse httpResponse) throws AppException {
		try {
			if(httpResponse.getStatusLine().getStatusCode()!=204) {
				try {
					LOG.error("Error while deleteting FE-API: '"+this.rollbackAPI.getId()+"' to roll it back: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
				} catch (Exception e) {
					LOG.error("Error while deleteting FE-API: '"+this.rollbackAPI.getId()+"' to roll it back", e);
				}
			} else {
				rolledBack = true;
				LOG.debug("Successfully rolled back created FE-API: '"+this.rollbackAPI.getId()+"'");
			}
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
		return null;
	}
	
	
}
