package com.axway.apim.apiimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.apiimport.actions.CreateNewAPI;
import com.axway.apim.apiimport.actions.RecreateToUpdateAPI;
import com.axway.apim.apiimport.actions.RepublishToUpdateAPI;
import com.axway.apim.apiimport.actions.UpdateExistingAPI;
import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;

public class APIImportManager {
	
	private static Logger LOG = LoggerFactory.getLogger(APIImportManager.class);
	
	private ErrorState error = ErrorState.getInstance();
	
	private boolean enforceBreakingChange = CoreParameters.getInstance().isForce();
	
	/**
	 * This method is taking in the APIChangeState to decide about the strategy how to 
	 * synchronize the desired API-State into the API-Manager.
	 * @param changeState containing the desired and actual API
	 * @param forceUpdate controls if the API should be re-created or not no matter if required
	 * @throws AppException is the desired state can't be replicated into the API-Manager.
	 */
	public void applyChanges(APIChangeState changeState, boolean forceUpdate) throws AppException {
		CoreParameters commands = CoreParameters.getInstance();
		if(!APIManagerAdapter.hasAdminAccount() && changeState.isAdminAccountNeeded() ) {
			if(commands.isAllowOrgAdminsToPublish()) {
				LOG.debug("Desired API-State set to published using OrgAdmin account only. Going to create a publish request. "
						+ "Set allowOrgAdminsToPublish to false to prevent orgAdmins from creating a publishing request.");
			} else {
				error.setError("OrgAdmin user only allowed to change/register unpublished APIs. "
						+ "Set allowOrgAdminsToPublish to true (default) to allow orgAdmins to create a publishing request.", ErrorCode.NO_ADMIN_ROLE_USER, false);
				throw new AppException("OrgAdmin user only allowed to change/register unpublished APIs.", ErrorCode.NO_ADMIN_ROLE_USER);
			}
		}
		// No existing API found (means: No match for APIPath/V-Host & Query-Version), creating a complete new
		if(changeState.getActualAPI()==null) {
			// --> CreateNewAPI
			LOG.info("No existing API found, creating new!");
			CreateNewAPI createAPI = new CreateNewAPI();
			createAPI.execute(changeState, false);
		// Otherwise an existing API exists
		} else if(forceUpdate) {
			LOG.info("Re-Creating API as the ForceUpdate flag is set");
			RecreateToUpdateAPI recreate = new RecreateToUpdateAPI();
			recreate.execute(changeState);
		} else {
			if(!changeState.hasAnyChanges()) {
				APIPropertiesExport.getInstance().setProperty("feApiId", changeState.getActualAPI().getId());
				LOG.debug("BUT, no changes detected between Import- and API-Manager-API. Exiting now...");
				error.setWarning("No changes detected between Import- and API-Manager-API: '" + changeState.getActualAPI().getName() + "' ("+changeState.getActualAPI().getId()+")", ErrorCode.NO_CHANGE, false);
				throw new AppException("No changes detected between Import- and API-Manager-API", ErrorCode.NO_CHANGE);
			}
			LOG.info("Recognized the following changes. Potentially Breaking: " + changeState.getBreakingChanges() + 
					" plus Non-Breaking: " + changeState.getNonBreakingChanges());
			if (changeState.isBreaking()) { // Make sure, breaking changes aren't applied without enforcing it.
				if(!enforceBreakingChange) {
					error.setError("A potentially breaking change can't be applied without enforcing it! Try option: -force", ErrorCode.BREAKING_CHANGE_DETECTED, false);
					throw new AppException("A potentially breaking change can't be applied without enforcing it! Try option: -force", ErrorCode.BREAKING_CHANGE_DETECTED);
				}
			}
			if(changeState.isUpdateExistingAPI()) { // All changes can be applied to the existing API in current state
				LOG.info("Update API Strategy: All changes can be applied in current state.");
				LOG.debug("Apply breaking changes: "+changeState.getBreakingChanges()+" & and "
						+ "Non-Breaking: "+changeState.getNonBreakingChanges()+", for "+changeState.getActualAPI().getState().toUpperCase());
				UpdateExistingAPI updateAPI = new UpdateExistingAPI();
				updateAPI.execute(changeState);
			} else if(changeState.isRecreateAPI() || APIImportParams.getInstance().isZeroDowntimeUpdate()) {
				if(changeState.isRecreateAPI()) {
					LOG.info("Update API Strategy: Re-Create API as changes can't be applied to existing API.");
				} else {
					LOG.info("Update API Strategy: Re-Create API for a Zero-Downtime update.");
				}
				LOG.debug("Apply breaking changes: "+changeState.getBreakingChanges()+" & and "
						+ "Non-Breaking: "+changeState.getNonBreakingChanges()+", for "+changeState.getActualAPI().getState().toUpperCase());				
				RecreateToUpdateAPI recreate = new RecreateToUpdateAPI();
				recreate.execute(changeState);
			} else { // We have changes, that require a re-creation of the API
				LOG.info("Update API Strategy: Re-Publish API to apply changes");
				LOG.debug("Apply breaking changes: "+changeState.getBreakingChanges()+" & and "
						+ "Non-Breaking: "+changeState.getNonBreakingChanges()+", for "+changeState.getActualAPI().getState().toUpperCase());
				RepublishToUpdateAPI republish = new RepublishToUpdateAPI();
				republish.execute(changeState);
			}
		}
		if(!APIManagerAdapter.hasAdminAccount() && changeState.isAdminAccountNeeded() && commands.isAllowOrgAdminsToPublish() ) {
			LOG.info("Actual API has been created and is waiting for an approval by an administrator. "
					+ "You may update the pending API as often as you want before it is finally published.");
		}
	}
}
