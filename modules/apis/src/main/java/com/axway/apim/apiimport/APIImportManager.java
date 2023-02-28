package com.axway.apim.apiimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.apiimport.actions.CreateNewAPI;
import com.axway.apim.apiimport.actions.RecreateToUpdateAPI;
import com.axway.apim.apiimport.actions.RepublishToUpdateAPI;
import com.axway.apim.apiimport.actions.UpdateExistingAPI;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;

public class APIImportManager {

    private static final Logger LOG = LoggerFactory.getLogger(APIImportManager.class);

    private final boolean enforceBreakingChange = CoreParameters.getInstance().isForce();

    /**
     * This method is taking in the APIChangeState to decide about the strategy how to
     * synchronize the desired API-State into the API-Manager.
     *
     * @param changeState containing the desired and actual API
     * @param forceUpdate controls if the API should be re-created or not no matter if required
     * @param updateOnly  if true, no new API is created
     * @throws AppException is the desired state can't be replicated into the API-Manager.
     */
    public void applyChanges(APIChangeState changeState, boolean forceUpdate, boolean updateOnly) throws AppException {
        boolean orgAdminSelfService = APIManagerAdapter.getInstance().configAdapter.getConfig(APIManagerAdapter.hasAdminAccount()).getOadminSelfServiceEnabled();
        if (!APIManagerAdapter.hasAdminAccount() && changeState.isAdminAccountNeeded()) {
            if (orgAdminSelfService) {
                LOG.info("Desired API-State set to published using OrgAdmin account only. Going to create a publish request.");
            } else {
                LOG.info("OrgAdmin user only allowed to change/register unpublished APIs.");
            }
        }
        // No existing API found (means: No match for APIPath/V-Host & Query-Version), creating a complete new
        if (changeState.getActualAPI() == null) {
            // --> CreateNewAPI
            if (updateOnly) {
                throw new AppException("No existing API but, cannot create new API as flag updateOnly is set.", ErrorCode.UPDATE_ONLY_IS_SET);
            }
            LOG.info("No existing API found, creating new!");
            CreateNewAPI createAPI = new CreateNewAPI();
            createAPI.execute(changeState, false);
            // Otherwise an existing API exists
        } else if (forceUpdate) {
            LOG.info("Re-Creating API as the ForceUpdate flag is set");
            RecreateToUpdateAPI recreate = new RecreateToUpdateAPI();
            recreate.execute(changeState);
        } else {
            if (!changeState.hasAnyChanges()) {
                APIPropertiesExport.getInstance().setProperty("feApiId", changeState.getActualAPI().getId());
                LOG.debug("BUT, no changes detected between Import- and API-Manager-API. Exiting now...");
                throw new AppException("No changes detected between Import- and API-Manager-API: '" + changeState.getActualAPI().getName() + "' (" + changeState.getActualAPI().getId() + ")", ErrorCode.NO_CHANGE);
            }
            LOG.info("Recognized the following changes. Potentially Breaking: {} plus Non-Breaking: {}", changeState.getBreakingChanges(), changeState.getNonBreakingChanges());
            LOG.info("Is Breaking changes : {} Enforce Breaking changes : {}", changeState.isBreaking(), enforceBreakingChange);
            if (changeState.isBreaking()) { // Make sure, breaking changes aren't applied without enforcing it.
                if (!enforceBreakingChange) {
                    throw new AppException("A potentially breaking change can't be applied without enforcing it! Try option: -force", ErrorCode.BREAKING_CHANGE_DETECTED);
                }
            }
            if (changeState.isUpdateExistingAPI()) { // All changes can be applied to the existing API in current state
                LOG.info("Update API Strategy: All changes can be applied in current state.");
                LOG.debug("Apply breaking changes: {} & and Non-Breaking: {}, for {}", changeState.getBreakingChanges(), changeState.getNonBreakingChanges(), changeState.getActualAPI().getState().toUpperCase());
                UpdateExistingAPI updateAPI = new UpdateExistingAPI();
                updateAPI.execute(changeState);
            } else if (changeState.isRecreateAPI() || CoreParameters.getInstance().isZeroDowntimeUpdate()) {
                if (changeState.isRecreateAPI()) {
                    LOG.info("Update API Strategy: Re-Create API as changes can't be applied to existing API.");
                } else {
                    LOG.info("Update API Strategy: Re-Create API for a Zero-Downtime update.");
                }
                LOG.debug("Apply breaking changes: {} & and Non-Breaking: {}, for {}",changeState.getBreakingChanges(), changeState.getNonBreakingChanges(), changeState.getActualAPI().getState().toUpperCase());
                RecreateToUpdateAPI recreate = new RecreateToUpdateAPI();
                recreate.execute(changeState);
            } else { // We have changes, that require a re-creation of the API
                LOG.info("Update API Strategy: Re-Publish API to apply changes");
                LOG.debug("Apply breaking changes: {} & and Non-Breaking:  {}, for {}" ,changeState.getBreakingChanges(), changeState.getNonBreakingChanges(), changeState.getActualAPI().getState().toUpperCase());
                RepublishToUpdateAPI republish = new RepublishToUpdateAPI();
                republish.execute(changeState);
            }
        }
        if (!APIManagerAdapter.hasAdminAccount() && changeState.isAdminAccountNeeded() ) {
            LOG.info("Actual API has been created and is waiting for an approval by an administrator. "
                    + "You may update the pending API as often as you want before it is finally published.");
        }
    }
}
