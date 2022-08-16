package com.axway.apim.apiimport.actions;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.apiimport.APIChangeState;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.errorHandling.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class is used by the APIImportManager#applyChanges(APIChangeState, boolean) to update an existing API.
 * This happens, when all changes can be applied to the existing API which is quite of the case for an "Unpublished" API.
 *
 * @author cwiechmann@axway.com
 */
public class UpdateExistingAPI {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateExistingAPI.class);

    public void execute(APIChangeState changes) throws AppException {

        API actualAPI = changes.getActualAPI();
        List<APIMethod> actualAPIMethods = changes.getActualAPI().getApiMethods();
        APIManagerAdapter apiManager = APIManagerAdapter.getInstance();
        try {

            LOG.info("Update existing " + actualAPI.getState() + " API: '" + actualAPI.getName() + "' " + actualAPI.getVersion() + " (ID: " + actualAPI.getId() + ")");
            // Copy all desired proxy changes into the actual API
            APIChangeState.copyChangedProps(changes.getDesiredAPI(), changes.getActualAPI(), changes.getAllChanges());
            changes.getActualAPI().setApiMethods(null);
            List<APIMethod> desiredAPIMethods = changes.getDesiredAPI().getApiMethods();
            ManageApiMethods manageApiMethods = new ManageApiMethods();
            manageApiMethods.isMethodMismatch(actualAPIMethods, desiredAPIMethods);
            // If a proxy update is required
            if (changes.isProxyUpdateRequired()) {
                // Update the proxy
                apiManager.apiAdapter.updateAPIProxy(changes.getActualAPI());
            }
            manageApiMethods.updateApiMethods(changes.getActualAPI().getId(),actualAPIMethods, desiredAPIMethods );
            // If image an include, update it
            if (changes.getAllChanges().contains("image")) {
                apiManager.apiAdapter.updateAPIImage(changes.getActualAPI(), changes.getDesiredAPI().getImage());
            }
            // This is special, as the status is not a property and requires some additional actions!
            APIStatusManager statusUpdate = new APIStatusManager();
            if (changes.getNonBreakingChanges().contains("state")) {
                statusUpdate.update(changes.getActualAPI(), changes.getDesiredAPI().getState(), changes.getDesiredAPI().getVhost());
            }
            if (changes.getNonBreakingChanges().contains("retirementDate")) {
                apiManager.apiAdapter.updateRetirementDate(changes.getActualAPI(), changes.getDesiredAPI().getRetirementDate());
            }

            // This is required when an API has been set back to Unpublished
            // In that case, the V-Host is reset to null - But we still want to use the configured V-Host
            if (statusUpdate.isUpdateVHostRequired()) {
                apiManager.apiAdapter.updateAPIProxy(changes.getActualAPI());
            }
            new APIQuotaManager(changes.getDesiredAPI(), changes.getActualAPI()).execute(changes.getActualAPI());
            new ManageClientOrgs(changes.getDesiredAPI(), changes.getActualAPI()).execute(false);
            // Handle subscription to applications
            new ManageClientApps(changes.getDesiredAPI(), changes.getActualAPI(), null).execute(false);
            if (actualAPI.getState().equals(API.STATE_DELETED)) {
                LOG.info(changes.waiting4Approval() + "Successfully deleted API: '" + actualAPI.getName() + "' " + actualAPI.getVersion() + " (ID: " + actualAPI.getId() + ")");
            } else {
                LOG.info(changes.waiting4Approval() + "Successfully updated " + actualAPI.getState() + " API: '" + actualAPI.getName() + "' " + actualAPI.getVersion() + " (ID: " + actualAPI.getId() + ")");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            APIPropertiesExport.getInstance().setProperty("feApiId", changes.getActualAPI().getId());
        }
    }

}
