package com.axway.apim.apiimport.actions;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.api.API;
import com.axway.apim.apiimport.APIChangeState;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.CoreParameters.Mode;
import com.axway.apim.lib.error.AppException;

public class RepublishToUpdateAPI {

    private static final Logger LOG = LoggerFactory.getLogger(RepublishToUpdateAPI.class);

    public void execute(APIChangeState changes) throws AppException {

        API actualAPI = changes.getActualAPI();
        Mode clientAppsMode = CoreParameters.getInstance().getClientAppsMode();
        Mode clientOrgsMode = CoreParameters.getInstance().getClientOrgsMode();
        // Get existing Orgs and Apps, as they will be lost when the API gets unpublished
        if (clientAppsMode == Mode.add && actualAPI.getApplications() != null) {
            if (changes.getDesiredAPI().getApplications() == null)
                changes.getDesiredAPI().setApplications(new ArrayList<>());
            mergeIntoList(changes.getDesiredAPI().getApplications(), actualAPI.getApplications());
            // Reset the applications to have them re-created based the desired Apps
            actualAPI.setApplications(new ArrayList<>());
        }
        if (clientOrgsMode == Mode.add && actualAPI.getClientOrganizations() != null) {
            if (changes.getDesiredAPI().getClientOrganizations() == null)
                changes.getDesiredAPI().setClientOrganizations(new ArrayList<>());
            // Take over existing organizations
            mergeIntoList(changes.getDesiredAPI().getClientOrganizations(), actualAPI.getClientOrganizations());
            // Delete them, so that they are re-created based on the desired orgs
            actualAPI.setClientOrganizations(new ArrayList<>());
        }
        // 1. Create BE- and FE-API (API-Proxy) / Including updating all belonging props!
        // This also includes all CONFIGURED application subscriptions and client-orgs
        // But not potentially existing Subscriptions or manually created Client-Orgs
        LOG.info("Unpublish existing API for update: {} (ID: {})", actualAPI.getName(), actualAPI.getId());
        String actualState = changes.getActualAPI().getState();
        // Make sure the API state is restored during updateExisting
        if (!changes.getNonBreakingChanges().contains("state")) {
            changes.getNonBreakingChanges().add("state");
            changes.getDesiredAPI().setState(actualState);
        }
        APIStatusManager statusManager = new APIStatusManager();
        statusManager.update(actualAPI, API.STATE_UNPUBLISHED, true);
        UpdateExistingAPI updateExistingAPI = new UpdateExistingAPI();
        updateExistingAPI.execute(changes);
        LOG.debug("Existing API successfully updated: {} (ID: {})", actualAPI.getName(), actualAPI.getId());
    }

    private <T> void mergeIntoList(List<T> targetList, List<T> source) {
        for (T element : source) {
            if (!targetList.contains(element)) {
                targetList.add(element);
            }
        }
    }
}
