package com.axway.apim.apiimport.actions;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIAccessAdapter.Type;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ManageClientApps {

    private static final Logger LOG = LoggerFactory.getLogger(ManageClientApps.class);

    private final API desiredState;
    private final API actualState;
    private final API oldAPI;

    APIManagerAPIAccessAdapter accessAdapter = APIManagerAdapter.getInstance().getAccessAdapter();

    /**
     * In case, the API has been re-created, this is object contains the API how it was before
     *
     * @param desiredState the desired state of the API
     * @param actualState  the actual state of the API incl. intermediate changes already performed so far
     * @param oldAPI       the actual state of the API how it was loaded initially
     * @throws AppException when an error occurs
     */
    public ManageClientApps(API desiredState, API actualState, API oldAPI) throws AppException {
        this.desiredState = desiredState;
        this.actualState = actualState;
        this.oldAPI = oldAPI;
    }

    public void execute(boolean reCreation) throws AppException {
        if (desiredState.getApplications() == null && !reCreation) return;
        if (CoreParameters.getInstance().isIgnoreClientApps()) {
            LOG.info("Configured client applications are ignored, as flag ignoreClientApps has been set.");
            return;
        }
        if (desiredState.getApplications() != null) { // Happens, when config-file doesn't contain client apps
            // Remove configured apps, for Non-Granted-Orgs!
            removeNonGrantedClientApps(desiredState.getApplications());
        }
        // If an UNPUBLISHED API has been re-created, we have to create App-Subscriptions manually, as API-Manager Upgrade only works on PUBLISHED APIs
        // But we only need to do this, if existing App-Subscriptions should be preserved (MODE_ADD).
        if (reCreation && actualState.getState().equals(API.STATE_UNPUBLISHED) && CoreParameters.getInstance().getClientAppsMode().equals(CoreParameters.Mode.add)) {
            LOG.debug("Copying applications from old api id : {} to new api id: {}", oldAPI.getId(), actualState.getId());
            // Create previously existing App-Subscriptions
            createAppSubscription(oldAPI.getApplications(), actualState.getId());
            // Update the In-Memory actual state for further processing
            actualState.setApplications(oldAPI.getApplications());
        }
        LOG.debug("Desired applications: {}", desiredState.getApplications());
        LOG.debug("Actual applications: {}", actualState.getApplications());
        List<ClientApplication> missingDesiredApps = getMissingApps(desiredState.getApplications(), actualState.getApplications());
        List<ClientApplication> removingActualApps = getMissingApps(actualState.getApplications(), desiredState.getApplications());
        LOG.debug("Missing Apps to be provisioned: {}", missingDesiredApps);
        LOG.debug("Removing Apps from API access: {}", removingActualApps);
        if (missingDesiredApps.isEmpty() && desiredState.getApplications() != null) {
            LOG.info("All desired applications: {} have already a subscription. Nothing to do.", desiredState.getApplications());
        } else {
            createAppSubscription(missingDesiredApps, actualState.getId());
        }
        if (!removingActualApps.isEmpty()) {
            if (CoreParameters.getInstance().getClientAppsMode().equals(CoreParameters.Mode.replace)) {
                LOG.info("Removing access for applications: {} from API: {} ", removingActualApps, actualState.getName());
                removeAppSubscription(removingActualApps, actualState);
            } else {
                LOG.info("NOT removing access for applications: {} from API: {} as clientAppsMode NOT set to replace.", removingActualApps, actualState.getName());
            }
        }

    }


    public void removeNonGrantedClientApps(List<ClientApplication> apps) {
        if (apps == null) return;
        ListIterator<ClientApplication> it = apps.listIterator();
        ClientApplication app;
        while (it.hasNext()) {
            app = it.next();
            if (hasClientAppPermission(app)) {
                LOG.error("Organization of configured application: {} has NO permission to this API. Ignoring this application.", app.getName());
                it.remove();
            }
        }
    }

    public boolean hasClientAppPermission(ClientApplication app) {
        LOG.info("Checking Application name : {} belong to granted organization : {}", app.getName(), app.getOrganization().getName());
        if (actualState.getClientOrganizations() == null) {
            LOG.debug("No Client-Orgs configured for this API, therefore other app has NO permission.");
            return true;
        }
        return !actualState.getClientOrganizations().contains(app.getOrganization());
    }

    private void createAppSubscription(List<ClientApplication> missingDesiredApps, String apiId) throws AppException {
        if (missingDesiredApps.isEmpty()) return;
        LOG.info("Creating API-Access for the following apps: {}", missingDesiredApps);
        for (ClientApplication app : missingDesiredApps) {
            LOG.info("Creating API-Access for application {}", app.getName());
            APIAccess apiAccess = new APIAccess();
            apiAccess.setApiId(apiId);
            APIAccess response = accessAdapter.createAPIAccessForApplication(apiAccess, app.getId());
            LOG.info("Created API ({}) Access for application {} ", response.getApiId(), app.getName());
        }
    }

    private void removeAppSubscription(List<ClientApplication> removingActualApps, API actualApi) throws AppException {
        for (ClientApplication app : removingActualApps) {
            // A Client-App that doesn't belong to a granted organization, can't have a subscription.
            if (hasClientAppPermission(app)) continue;
            LOG.debug("Removing API-Access for application {}", app.getName());
            List<APIAccess> apiAccesses = APIManagerAdapter.getInstance().getAccessAdapter().getAPIAccess(app, APIManagerAPIAccessAdapter.Type.applications);
            LOG.debug("App API Access : {}", app.getApiAccess());
            try {
                for (APIAccess apiAccess : apiAccesses) {
                    if (apiAccess.getApiId().equals(actualApi.getId())) {
                        accessAdapter.deleteAPIAccess(apiAccess, app, Type.applications);
                    }
                }
            } catch (Exception e) {
                LOG.error("Can't delete API access requests for application.");
                throw new AppException("Can't delete API access requests for application.", ErrorCode.API_MANAGER_COMMUNICATION, e);
            }
        }
    }

    public List<ClientApplication> getMissingApps(List<ClientApplication> apps, List<ClientApplication> otherApps) {
        List<ClientApplication> missingApps = new ArrayList<>();
        if (otherApps == null) otherApps = new ArrayList<>();
        if (apps == null) apps = new ArrayList<>();
        for (ClientApplication app : apps) {
            if (containsAppName(otherApps, app)) {
                continue;
            }
            missingApps.add(app);
        }
        return missingApps;
    }


    public boolean containsAppName(List<ClientApplication> source, ClientApplication clientApplication) {
        for (ClientApplication app : source) {
            if (app.getName().equals(clientApplication.getName())) {
                if (app.getOrganization() != null && clientApplication.getOrganization() != null) {
                    return app.getOrganization().getName().equals(clientApplication.getOrganization().getName());
                }
                return true;
            }
        }
        return false;
    }
}
