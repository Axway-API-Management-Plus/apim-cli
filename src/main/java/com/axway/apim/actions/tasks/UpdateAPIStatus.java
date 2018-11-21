package com.axway.apim.actions.tasks;

import java.net.URI;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;

import com.axway.apim.actions.rest.DELRequest;
import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.APIBaseDefinition;
import com.axway.apim.swagger.api.APIManagerAPI;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;

public class UpdateAPIStatus extends AbstractAPIMTask implements IResponseParser {
	
	private String intent = "";
	
	public static HashMap<String, String[]> statusChangeMap = new HashMap<String, String[]>() {{
		put("unpublished", 	new String[] {"published", "deleted"});
		put("published", 	new String[] {"unpublished", "deprecated"});
		put("deleted", 		new String[] {});
		put("deprecated", 	new String[] {"unpublished", "undeprecated"});
	}};
	
	/**
	 * Maps the provided status to the REST-API endpoint to change the status!
	 */
	public static HashMap<String, String> statusEndpoint = new HashMap<String, String>() {{
		put("unpublished", 	"unpublish");
		put("published", 	"publish");
		put("deprecated", 	"deprecate");
		put("undeprecated", "undeprecate");
	}};
	
	

	public UpdateAPIStatus(IAPIDefinition desiredState, IAPIDefinition actualState, String intent) {
		super(desiredState, actualState);
		this.intent = intent;
		// TODO Auto-generated constructor stub
	}
	
	public UpdateAPIStatus(IAPIDefinition desiredState, IAPIDefinition actualState) {
		this(desiredState, actualState, "");
		// TODO Auto-generated constructor stub
	}
	
	
	public void execute() throws AppException {
		if(this.actualState.getStatus().equals(this.desiredState.getStatus())) {
			LOG.debug("Desired and actual status equals. No need to update status!");
			return;
		}
		LOG.info(this.intent + "Updating API-Status from: '" + this.actualState.getStatus() + "' to '" + this.desiredState.getStatus() + "'");
		
		URI uri;
		//ObjectMapper objectMapper = new ObjectMapper();

		Transaction context = Transaction.getInstance();
		
		RestAPICall apiCall;
		
		try {
			String[] possibleStatus = statusChangeMap.get(actualState.getStatus());
			String intermediateState = null;
			boolean statusMovePossible = false;
			for(String status : possibleStatus) {
				if(desiredState.getStatus().equals(status)) {
					statusMovePossible = true; // Direkt move to new state possible
					break;
				} else {
					String[] possibleStatus2 = statusChangeMap.get(status);
					if(possibleStatus2!=null) {
						for(String subStatus : possibleStatus2) {
							if(desiredState.getStatus().equals(subStatus)) {
								intermediateState = status;
								statusMovePossible = true;
								break;
							}
						}
					}
				}
			}
			if (statusMovePossible) {
				if(intermediateState!=null) {
					LOG.info("Required intermediate state: "+intermediateState);
					// In case, we can't process directly, we have to perform an intermediate state change
					IAPIDefinition desiredIntermediate = new APIBaseDefinition();
					desiredIntermediate.setStatus(intermediateState);
					UpdateAPIStatus intermediateStatusUpdate = new UpdateAPIStatus(desiredIntermediate, actualState, " ### ");
					intermediateStatusUpdate.execute();
				}
			} else {
				LOG.error(this.intent + "The status change from: " + actualState.getStatus() + " to " + desiredState.getStatus() + " is not possible!");
				throw new AppException("The status change from: '" + actualState.getStatus() + "' to '" + desiredState.getStatus() + "' is not possible!", ErrorCode.CANT_UPDATE_API_STATUS);
			}
			if(desiredState.getStatus().equals(IAPIDefinition.STATE_DELETED)) {
				uri = new URIBuilder(cmd.getAPIManagerURL())
						.setPath(RestAPICall.API_VERSION+"/proxies/"+actualState.getApiId())
						.build();
				apiCall = new DELRequest(uri, this);
				context.put("responseMessage", "'Old' FE-API deleted (API-Proxy)");
				apiCall.execute();
				// Additionally we need to delete the BE-API
				uri = new URIBuilder(cmd.getAPIManagerURL())
						.setPath(RestAPICall.API_VERSION+"/apirepo/"+((APIManagerAPI)actualState).getBackendApiId())
						.build();
				apiCall = new DELRequest(uri, this);
				context.put("responseMessage", "'Old' BE-API deleted.");
				apiCall.execute();
			} else {
				uri = new URIBuilder(cmd.getAPIManagerURL())
					.setPath(RestAPICall.API_VERSION+"/proxies/"+actualState.getApiId()+"/"+statusEndpoint.get(desiredState.getStatus()))
					.build();
			
				apiCall = new POSTRequest(null, uri, this);
				apiCall.setContentType("application/x-www-form-urlencoded");
				apiCall.execute();
			} 
		} catch (Exception e) {
			throw new AppException("The status change from: '" + actualState.getStatus() + "' to '" + desiredState.getStatus() + "' is not possible!", ErrorCode.CANT_UPDATE_API_STATUS, e);
		}
	}
	@Override
	public JsonNode parseResponse(HttpResponse response) throws AppException {
		Transaction context = Transaction.getInstance();
		if(context.get("responseMessage")!=null) {
			LOG.info(""+context.get("responseMessage"));
			return null;
		} else {
			String backendAPIId = JsonPath.parse(getJSONPayload(response)).read("$.id", String.class);
			Transaction.getInstance().put("backendAPIId", backendAPIId);
			// The action was successful, update the status!
			this.actualState.setStatus(desiredState.getStatus());
			LOG.info(this.intent + "Actual API state set to: " + this.actualState.getStatus());
			return null;
		}
	}
}
