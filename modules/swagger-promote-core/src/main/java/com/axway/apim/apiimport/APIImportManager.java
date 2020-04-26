package com.axway.apim.apiimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.CreateNewAPI;
import com.axway.apim.actions.RecreateToUpdateAPI;
import com.axway.apim.actions.UpdateExistingAPI;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.state.APIChangeState;
import com.axway.apim.api.state.IAPI;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;

public class APIImportManager {
	
	private static Logger LOG = LoggerFactory.getLogger(APIImportManager.class);
	
	private ErrorState error = ErrorState.getInstance();
	
	private boolean enforceBreakingChange = CommandParameters.getInstance().isEnforceBreakingChange();
	
	/**
	 * This method is taking in the APIChangeState to decide about the strategy how to 
	 * synchronize the desired API-State into the API-Manager.
	 * @param changeState containing the desired and actual API
	 * @throws AppException is the desired state can't be replicated into the API-Manager.
	 */
	public void applyChanges(APIChangeState changeState) throws AppException {
		CommandParameters commands = CommandParameters.getInstance();
		if(!APIManagerAdapter.hasAdminAccount() && isAdminAccountNeeded(changeState) ) {
			if(commands.allowOrgAdminsToPublish()) {
				LOG.debug("Desired API-State set to published using OrgAdmin account only. Going to create a publish request. "
						+ "Set allowOrgAdminsToPublish to false to prevent orgAdmins from creating a publishing request.");
			} else {
				error.setError("OrgAdmin user only allowed to change/register unpublished APIs. "
						+ "Set allowOrgAdminsToPublish to true (default) to allow orgAdmins to create a publishing request.", ErrorCode.NO_ADMIN_ROLE_USER, false);
				throw new AppException("OrgAdmin user only allowed to change/register unpublished APIs.", ErrorCode.NO_ADMIN_ROLE_USER);
			}
		}
		// No existing API found (means: No match for APIPath), creating a complete new
		if(!changeState.getActualAPI().isValid()) {
			// --> CreateNewAPI
			LOG.info("Strategy: No existing API found, creating new!");
			CreateNewAPI createAPI = new CreateNewAPI();
			createAPI.execute(changeState, false);
		// Otherwise an existing API exists
		} else {
			LOG.info("Strategy: Going to update existing API: " + changeState.getActualAPI().getName() +" (Version: "+ changeState.getActualAPI().getVersion() + ")");
			if(!changeState.hasAnyChanges()) {
				APIPropertiesExport.getInstance().setProperty("feApiId", changeState.getActualAPI().getId());
				LOG.debug("BUT, no changes detected between Import- and API-Manager-API. Exiting now...");
				error.setWarning("No changes detected between Import- and API-Manager-API", ErrorCode.NO_CHANGE, false);
				throw new AppException("No changes detected between Import- and API-Manager-API", ErrorCode.NO_CHANGE);
			}
			LOG.info("Recognized the following changes. Potentially Breaking: " + changeState.getBreakingChanges() + 
					" plus Non-Breaking: " + changeState.getNonBreakingChanges());
			if (changeState.isBreaking()) { // Make sure, breaking changes aren't applied without enforcing it.
				if(!enforceBreakingChange) {
					error.setError("A potentially breaking change can't be applied without enforcing it! Try option: -f true", ErrorCode.BREAKING_CHANGE_DETECTED, false);
					throw new AppException("A potentially breaking change can't be applied without enforcing it! Try option: -f true", ErrorCode.BREAKING_CHANGE_DETECTED);
				}
			}
			
			if(changeState.isUpdateExistingAPI()) { // All changes can be applied to the existing API in current state
				LOG.info("Strategy: Update existing API, as all changes can be applied in current state.");
				UpdateExistingAPI updateAPI = new UpdateExistingAPI();
				updateAPI.execute(changeState);
				return;
			} else { // We have changes, that require a re-creation of the API
				LOG.info("Strategy: Apply breaking changes: "+changeState.getBreakingChanges()+" & and "
						+ "Non-Breaking: "+changeState.getNonBreakingChanges()+", for "+changeState.getActualAPI().getState().toUpperCase()+" API by recreating it!");
				RecreateToUpdateAPI recreate = new RecreateToUpdateAPI();
				recreate.execute(changeState);
			}
		}
		if(!APIManagerAdapter.hasAdminAccount() && isAdminAccountNeeded(changeState) && commands.allowOrgAdminsToPublish() ) {
			LOG.info("Actual API has been created and is waiting for an approval by an administrator. "
					+ "You may update the pending API as often as you want before it is finally published.");
		}
	}
	
	private boolean isAdminAccountNeeded(APIChangeState changeState) throws AppException {
		if(changeState.getDesiredAPI().getState().equals(IAPI.STATE_UNPUBLISHED) && 
				(!changeState.getActualAPI().isValid() || changeState.getActualAPI().getState().equals(IAPI.STATE_UNPUBLISHED))) {
			return false;
		} else {
			return true;
		}		
	}

}
