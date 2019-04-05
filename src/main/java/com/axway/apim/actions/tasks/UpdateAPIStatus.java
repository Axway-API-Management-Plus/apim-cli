package com.axway.apim.actions.tasks;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;

import com.axway.apim.actions.rest.DELRequest;
import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.lib.ErrorState;
import com.axway.apim.swagger.api.state.APIBaseDefinition;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateAPIStatus extends AbstractAPIMTask implements IResponseParser {
	
	private String intent = "";
	
	public static HashMap<String, String[]> statusChangeMap = new HashMap<String, String[]>() {{
		put("unpublished", 	new String[] {"published", "deleted"});
		put("published", 	new String[] {"unpublished", "deprecated"});
		put("deleted", 		new String[] {});
		put("deprecated", 	new String[] {"unpublished", "undeprecated"});
	}};
	
	/**
	 * Maps the actual API-State to all desired states, which requires an enforcement as it be break the API. 
	 */
	private static HashMap<String, List<String>> statusChangeRequiresEnforce = new HashMap<String, List<String>>() {{
		put("published",  Arrays.asList(new String[] {"unpublished", "deleted"}));
		put("deprecated", Arrays.asList(new String[] {"unpublished", "deleted"}));
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
	
	

	public UpdateAPIStatus(IAPI desiredState, IAPI actualState, String intent) {
		super(desiredState, actualState);
		this.intent = intent;
	}
	
	public UpdateAPIStatus(IAPI desiredState, IAPI actualState) {
		this(desiredState, actualState, "");
	}
	
	public void execute() throws AppException {
		if(CommandParameters.getInstance().isEnforceBreakingChange()) {
			execute(true);
		} else {
			execute(false);
		}
	}
	
	
	public void execute(boolean enforceBreakingChange) throws AppException {
		if(this.desiredState.getState().equals(this.actualState.getState())) {
			LOG.debug("Desired and actual status equal. No need to update status!");
			return;
		}
		LOG.info(this.intent + "Updating API-Status from: '" + this.actualState.getState() + "' to '" + this.desiredState.getState() + "'");
		if(!enforceBreakingChange) { 
			if(statusChangeRequiresEnforce.get(this.actualState.getState())!=null && 
					statusChangeRequiresEnforce.get(this.actualState.getState()).contains(this.desiredState.getState())) {
				ErrorState.getInstance().setError("Status change from actual status: '"+actualState.getState()+"' to desired status: '"+desiredState.getState()+"' "
						+ "is breaking. Enforce change with option: -f true", ErrorCode.BREAKING_CHANGE_DETECTED, false);
				throw new AppException("Status change from actual status: '"+actualState.getState()+"' to desired status: '"+desiredState.getState()+"' "
						+ "is breaking. Enforce change with option: -f true", ErrorCode.BREAKING_CHANGE_DETECTED);
			}
		}
		
		URI uri;

		Transaction context = Transaction.getInstance();
		
		RestAPICall apiCall;
		
		try {
			String[] possibleStatus = statusChangeMap.get(actualState.getState());
			String intermediateState = null;
			boolean statusMovePossible = false;
			for(String status : possibleStatus) {
				if(desiredState.getState().equals(status)) {
					statusMovePossible = true; // Direct move to new state possible
					break;
				} else {
					String[] possibleStatus2 = statusChangeMap.get(status);
					if(possibleStatus2!=null) {
						for(String subStatus : possibleStatus2) {
							if(desiredState.getState().equals(subStatus)) {
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
					IAPI desiredIntermediate = new APIBaseDefinition();
					desiredIntermediate.setState(intermediateState);
					UpdateAPIStatus intermediateStatusUpdate = new UpdateAPIStatus(desiredIntermediate, actualState, " ### ");
					intermediateStatusUpdate.execute(enforceBreakingChange);
				}
			} else {
				LOG.error(this.intent + "The status change from: " + actualState.getState() + " to " + desiredState.getState() + " is not possible!");
				throw new AppException("The status change from: '" + actualState.getState() + "' to '" + desiredState.getState() + "' is not possible!", ErrorCode.CANT_UPDATE_API_STATUS);
			}
			if(desiredState.getState().equals(IAPI.STATE_DELETED)) {
				uri = new URIBuilder(cmd.getAPIManagerURL())
						.setPath(RestAPICall.API_VERSION+"/proxies/"+actualState.getId())
						.build();
				apiCall = new DELRequest(uri, this);
				context.put("responseMessage", "'Old' FE-API deleted (API-Proxy)");
				apiCall.execute();
				// Additionally we need to delete the BE-API
				uri = new URIBuilder(cmd.getAPIManagerURL())
						.setPath(RestAPICall.API_VERSION+"/apirepo/"+actualState.getApiId())
						.build();
				apiCall = new DELRequest(uri, this, true);
				context.put("responseMessage", "'Old' BE-API deleted.");
				apiCall.execute();
			} else {
				uri = new URIBuilder(cmd.getAPIManagerURL())
					.setPath(RestAPICall.API_VERSION+"/proxies/"+actualState.getId()+"/"+statusEndpoint.get(desiredState.getState()))
					.build();
			
				apiCall = new POSTRequest(null, uri, this, true);
				apiCall.setContentType("application/x-www-form-urlencoded");
				apiCall.execute();
			} 
		} catch (Exception e) {
			throw new AppException("The status change from: '" + actualState.getState() + "' to '" + desiredState.getState() + "' is not possible!", ErrorCode.CANT_UPDATE_API_STATUS, e);
		}
	}
	@Override
	public JsonNode parseResponse(HttpResponse httpResponse) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
		String response = null;
		Transaction context = Transaction.getInstance();
		if(context.get("responseMessage")!=null) {
			LOG.info(""+context.get("responseMessage"));
			return null;
		} else {
			try {
				response = EntityUtils.toString(httpResponse.getEntity());
				JsonNode jsonNode = objectMapper.readTree(response);
				String backendAPIId = jsonNode.findPath("id").asText();
				Transaction.getInstance().put("backendAPIId", backendAPIId);
				// The action was successful, update the status!
				this.actualState.setState(desiredState.getState());
				LOG.info(this.intent + "Actual API state set to: " + this.actualState.getState());
				return null;
			} catch (Exception e1) {
				throw new AppException("Unable to parse response", ErrorCode.CANT_UPDATE_API_PROXY, e1);
			}
		}
	}
	
	
}
