package com.axway.apim.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.APIBaseDefinition;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;

public class APIStatusManager {
	
	static Logger LOG = LoggerFactory.getLogger(APIStatusManager.class);
	
	private APIManagerAdapter apimAdapter;

	private static enum StatusChangeMap {
		unpublished(new String[] { "published", "deleted" }), 
		published(new String[] { "unpublished", "deprecated" }),
		deleted(new String[] {}), 
		deprecated(new String[] { "unpublished", "undeprecated" }),
		undeprecated(new String[] { "published", "unpublished" }),
		pending(new String[] { "deleted" });

		private String[] possibleStates;

		StatusChangeMap(String[] possibleStates) {
			this.possibleStates = possibleStates;
		}
	}
	
	private static enum StatusChangeRequiresEnforce { 
		published(new String[] { "unpublished", "deleted" }),
		deprecated(new String[] { "unpublished", "deleted" });

		private List<String> enforceRequired = new ArrayList<String>();

		StatusChangeRequiresEnforce(String[] enforceRequired) {
			this.enforceRequired = Arrays.asList(enforceRequired);
		}
		
		public static StatusChangeRequiresEnforce getEnum(String value) {
			try {
				return StatusChangeRequiresEnforce.valueOf(value);
			} catch (Exception ignore) {
				return null;
			}
		}
	}

	public APIStatusManager() throws AppException {
		this.apimAdapter = APIManagerAdapter.getInstance();
	}
	
	public void update(API api, String desiredState, boolean enforceBreakingChange) throws AppException {
		if(api.getState().equals(desiredState)) {
			LOG.debug("Desired and actual status equal. No need to update status!");
			return;
		}
		api.setState(desiredState);
		API actualApi = apimAdapter.apiAdapter.getAPI(new APIFilter.Builder().hasId(api.getId()).build(), true);
		update(api, actualApi, enforceBreakingChange);
	}
	
	public void update(API desiredState, API actualState) throws AppException {
		if(CommandParameters.getInstance().isEnforceBreakingChange()) {
			update(desiredState, actualState, true);
		} else {
			update(desiredState, actualState, false);
		}
	}
	
	
	public void update(API desiredState, API actualState, boolean enforceBreakingChange) throws AppException {
		if(desiredState.getState().equals(actualState.getState())) {
			LOG.debug("Desired and actual status equal. No need to update status!");
			return;
		}
		LOG.debug("Updating API-Status from: '" + actualState.getState() + "' to '" + desiredState.getState() + "'");
		if(!enforceBreakingChange) { 
			if(StatusChangeRequiresEnforce.getEnum(actualState.getState())!=null && 
					StatusChangeRequiresEnforce.valueOf(actualState.getState()).enforceRequired.contains(desiredState.getState())) {
				ErrorState.getInstance().setError("Status change from actual status: '"+actualState.getState()+"' to desired status: '"+desiredState.getState()+"' "
						+ "is breaking. Enforce change with option: -f true", ErrorCode.BREAKING_CHANGE_DETECTED, false);
				throw new AppException("Status change from actual status: '"+actualState.getState()+"' to desired status: '"+desiredState.getState()+"' "
						+ "is breaking. Enforce change with option: -f true", ErrorCode.BREAKING_CHANGE_DETECTED);
			}
		}
		
		try {
			String[] possibleStatus = StatusChangeMap.valueOf(actualState.getState()).possibleStates;
			String intermediateState = null;
			boolean statusMovePossible = false;
			for(String status : possibleStatus) {
				if(desiredState.getState().equals(status)) {
					statusMovePossible = true; // Direct move to new state possible
					break;
				} else {
					String[] possibleStatus2 = StatusChangeMap.valueOf(status).possibleStates;
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
					LOG.debug("Required intermediate state: "+intermediateState);
					// In case, we can't process directly, we have to perform an intermediate state change
					API desiredIntermediate = new APIBaseDefinition();
					desiredIntermediate.setState(intermediateState);
					desiredIntermediate.setId(actualState.getId());
					new APIStatusManager().update(desiredIntermediate, actualState, enforceBreakingChange);
					if(desiredState.getState().equals(actualState.getState())) return;
				}
			} else {
				LOG.error("The status change from: " + actualState.getState() + " to " + desiredState.getState() + " is not possible!");
				throw new AppException("The status change from: '" + actualState.getState() + "' to '" + desiredState.getState() + "' is not possible!", ErrorCode.CANT_UPDATE_API_STATUS);
			}
			if(desiredState.getState().equals(API.STATE_DELETED)) {
				// If an API in state unpublished or pending, also an orgAdmin can delete it
				//boolean useAdmin = (actualState.getState().equals(API.STATE_UNPUBLISHED) || actualState.getState().equals(API.STATE_PENDING)) ? false : true; 
				apimAdapter.apiAdapter.deleteAPIProxy(desiredState);
				// Additionally we need to delete the BE-API
				apimAdapter.apiAdapter.deleteBackendAPI(desiredState);
			} else {
				apimAdapter.apiAdapter.updateAPIStatus(desiredState);
				
			} 
			// Take over the status, as it has been updated now
			actualState.setState(desiredState.getState());
			// When deprecation or undeprecation is requested, we have to set the actual API accordingly!
			if(desiredState.getState().equals("undeprecated")) {
				actualState.setDeprecated("false");
				actualState.setState(API.STATE_PUBLISHED);
			} else if (desiredState.getState().equals("deprecated")) {
				actualState.setState(API.STATE_PUBLISHED);
				actualState.setDeprecated("true");
			}
		} catch (Exception e) {
			throw new AppException("The status change from: '" + actualState.getState() + "' to '" + desiredState.getState() + "' is not possible!", ErrorCode.CANT_UPDATE_API_STATUS, e);
		}
	}
/*
	public boolean isUpdateVHostRequired() {
		return updateVHostRequired;
	}*/
}
