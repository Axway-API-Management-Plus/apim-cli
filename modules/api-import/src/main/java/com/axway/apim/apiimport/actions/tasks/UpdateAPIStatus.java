package com.axway.apim.apiimport.actions.tasks;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.APIBaseDefinition;
import com.axway.apim.api.IAPI;
import com.axway.apim.apiimport.state.APIChangeState;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.IResponseParser;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.DELRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.axway.apim.lib.utils.rest.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateAPIStatus extends AbstractAPIMTask implements IResponseParser {
	
	private String intent = "";
	
	private boolean updateVHostRequired = false;
	
	public static HashMap<String, String[]> statusChangeMap = new HashMap<String, String[]>() {{
		put("unpublished", 	new String[] {"published", "deleted"});
		put("published", 	new String[] {"unpublished", "deprecated"});
		put("deleted", 		new String[] {});
		put("deprecated", 	new String[] {"unpublished", "undeprecated"});
		put("pending", 		new String[] {"deleted"});
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
	
	

	public UpdateAPIStatus(API desiredState, API actualState, String intent) {
		super(desiredState, actualState);
		this.intent = intent;
	}
	
	public UpdateAPIStatus(API desiredState, API actualState) {
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
		LOG.debug(this.intent + "Updating API-Status from: '" + this.actualState.getState() + "' to '" + this.desiredState.getState() + "'");
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
					API desiredIntermediate = new APIBaseDefinition();
					desiredIntermediate.setState(intermediateState);
					UpdateAPIStatus intermediateStatusUpdate = new UpdateAPIStatus(desiredIntermediate, actualState, " ### ");
					intermediateStatusUpdate.execute(enforceBreakingChange);
				}
			} else {
				LOG.error(this.intent + "The status change from: " + actualState.getState() + " to " + desiredState.getState() + " is not possible!");
				throw new AppException("The status change from: '" + actualState.getState() + "' to '" + desiredState.getState() + "' is not possible!", ErrorCode.CANT_UPDATE_API_STATUS);
			}
			if(desiredState.getState().equals(IAPI.STATE_DELETED)) {
				// If an API in state unpublished or pending, also an orgAdmin can delete it
				boolean useAdmin = (actualState.getState().equals(IAPI.STATE_UNPUBLISHED) || actualState.getState().equals(IAPI.STATE_PENDING)) ? false : true; 
				uri = new URIBuilder(cmd.getAPIManagerURL())
						.setPath(RestAPICall.API_VERSION+"/proxies/"+actualState.getId())
						.build();
				apiCall = new DELRequest(uri, this, useAdmin);
				context.put("responseMessage", "'Old' FE-API deleted (API-Proxy)");
				apiCall.execute();
				// Additionally we need to delete the BE-API
				uri = new URIBuilder(cmd.getAPIManagerURL())
						.setPath(RestAPICall.API_VERSION+"/apirepo/"+actualState.getApiId())
						.build();
				apiCall = new DELRequest(uri, this, useAdmin);
				context.put("responseMessage", "'Old' BE-API deleted.");
				apiCall.execute();
			} else {
				uri = new URIBuilder(cmd.getAPIManagerURL())
					.setPath(RestAPICall.API_VERSION+"/proxies/"+actualState.getId()+"/"+statusEndpoint.get(desiredState.getState()))
					.build();
				if(desiredState.getVhost()!=null && desiredState.getState().equals(IAPI.STATE_PUBLISHED)) { // During publish, it might be required to also set the VHost (See issue: #98)
					HttpEntity entity = new StringEntity("vhost="+desiredState.getVhost());
					apiCall = new POSTRequest(entity, uri, this, useAdminAccountForPublish());
				} else {
					apiCall = new POSTRequest(null, uri, this, useAdminAccountForPublish());
				}
				apiCall.setContentType("application/x-www-form-urlencoded");
				apiCall.execute();
				if (desiredState.getVhost()!=null && desiredState.getState().equals(IAPI.STATE_UNPUBLISHED)) { 
					this.updateVHostRequired = true; // Flag to control update of the VHost
				}
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
		try {
			if(context.get("responseMessage")!=null) {
				LOG.debug(""+context.get("responseMessage"));
				return null;
			} else {
				try {
					response = EntityUtils.toString(httpResponse.getEntity());
					JsonNode jsonNode = objectMapper.readTree(response);
					String backendAPIId = jsonNode.findPath("id").asText();
					Transaction.getInstance().put("backendAPIId", backendAPIId);
					// The action was successful, update the status!
					this.actualState.setState(desiredState.getState());
					LOG.debug(this.intent + "Actual API state set to: " + this.actualState.getState());
					return null;
				} catch (Exception e1) {
					throw new AppException("Unable to parse response", ErrorCode.CANT_UPDATE_API_PROXY, e1);
				}
			}
		} finally {
			try {
				((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) { }
		}
	}

	public boolean isUpdateVHostRequired() {
		return updateVHostRequired;
	}
	
	private boolean useAdminAccountForPublish() throws AppException {
		if(APIManagerAdapter.hasAdminAccount()) return true;
		// This flag can be set to false to stop OrgAdmin from a Publishing request (means Pending approval)
		if(CommandParameters.getInstance().allowOrgAdminsToPublish()) return false;
		// In all other cases, we use the Admin-Account
		return true;
	}
	
	private String formatRetirementDate(Long retirementDate) {
		Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Z")));
		cal.setTimeInMillis(retirementDate);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Z")));
		return format.format(cal.getTime());
	}
	
	public void updateRetirementDate(APIChangeState changeState) throws AppException {
		if(changeState!=null && changeState.getNonBreakingChanges().contains("retirementDate")) {
			// Ignore the retirementDate if desiredState is not deprecated as it's used nowhere
			if(!desiredState.getState().equals(IAPI.STATE_DEPRECATED)) {
				LOG.info("Ignoring given retirementDate as API-Status is not set to deprecated");
				return;
			}
			try {
				URI uri = new URIBuilder(cmd.getAPIManagerURL())
						.setPath(RestAPICall.API_VERSION+"/proxies/"+actualState.getId()+"/deprecate").build();
				RestAPICall apiCall = new POSTRequest(new StringEntity("retirementDate="+formatRetirementDate(desiredState.getRetirementDate())), uri, this, true);
				apiCall.setContentType("application/x-www-form-urlencoded");
				apiCall.execute();
			} catch (Exception e) {
				ErrorState.getInstance().setError("Error while updating the retirementDate.", ErrorCode.CANT_UPDATE_API_PROXY);
				throw new AppException("Error while updating the retirementDate", ErrorCode.CANT_UPDATE_API_PROXY);
			}
		} 
		return;
	}
}
