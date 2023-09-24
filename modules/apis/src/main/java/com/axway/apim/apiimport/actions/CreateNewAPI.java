package com.axway.apim.apiimport.actions;

import com.axway.apim.adapter.apis.APIManagerAPIMethodAdapter;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.api.model.ServiceProfile;
import com.axway.apim.apiimport.DesiredAPI;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.EnvironmentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.api.API;
import com.axway.apim.apiimport.APIChangeState;
import com.axway.apim.apiimport.rollback.RollbackAPIProxy;
import com.axway.apim.apiimport.rollback.RollbackBackendAPI;
import com.axway.apim.apiimport.rollback.RollbackHandler;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.error.AppException;

import java.util.List;
import java.util.Map;

/**
 * This class is used by the APIImportManager#applyChanges(APIChangeState, boolean) to create a new API.
 * It's called, when an existing API can't be found.
 *
 * @author cwiechmann@axway.com
 */
public class CreateNewAPI {

    private static final Logger LOG = LoggerFactory.getLogger(CreateNewAPI.class);

    private API createdAPI = null;

    public void execute(APIChangeState changes, boolean reCreation) throws AppException {

        API desiredAPI = changes.getDesiredAPI();
        API actualAPI = changes.getActualAPI();
        APIManagerAdapter apiManagerAdapter = APIManagerAdapter.getInstance();
        APIManagerAPIAdapter apiAdapter = apiManagerAdapter.getApiAdapter();
        APIManagerAPIMethodAdapter methodAdapter = apiManagerAdapter.getMethodAdapter();
        RollbackHandler rollback = RollbackHandler.getInstance();

        API createdBEAPI = apiAdapter.importBackendAPI(desiredAPI);
        rollback.addRollbackAction(new RollbackBackendAPI(createdBEAPI));
        LOG.info("Create {} API: {} {}  based on {} specification.", desiredAPI.getState(), desiredAPI.getName(), desiredAPI.getVersion(), desiredAPI.getApiDefinition().getAPIDefinitionType().getNiceName());
        try {
            desiredAPI.setApiId(createdBEAPI.getApiId());
            createdAPI = apiAdapter.createAPIProxy(desiredAPI);
            List<APIMethod> desiredApiMethods = desiredAPI.getApiMethods();
            List<APIMethod> actualApiMethods = methodAdapter.getAllMethodsForAPI(createdAPI.getId());
            LOG.debug("Number of Methods : {}", actualApiMethods.size());
            ManageApiMethods manageApiMethods = new ManageApiMethods();
            manageApiMethods.updateApiMethods(createdAPI.getId(), actualApiMethods, desiredApiMethods);
            desiredAPI.setApiMethods(null);
        } catch (Exception e) {
            // Try to rollback FE-API (Proxy) bases on the created BE-API
            rollback.addRollbackAction(new RollbackAPIProxy(createdBEAPI));
            throw e;
        }
        rollback.addRollbackAction(new RollbackAPIProxy(createdAPI)); // In any case, register the API just created for a potential rollback

        try {
            // ... here we basically need to add all props to initially bring the API in sync!
            APIChangeState.initCreatedAPI(desiredAPI, createdAPI);
            //handle backend base path update
            String backendBasePath = ((DesiredAPI) desiredAPI).getBackendBasepath();
            LOG.debug("backendBasePath from config : {}", backendBasePath);
            if (backendBasePath != null && !CoreParameters.getInstance().isOverrideSpecBasePath()) {
                Map<String, ServiceProfile> serviceProfiles = createdAPI.getServiceProfiles();
                if (serviceProfiles != null) {
                    ServiceProfile serviceProfile = serviceProfiles.get("_default");
                    LOG.info("Updating API backendBasePath with value : {}", backendBasePath);
                    serviceProfile.setBasePath(backendBasePath);
                }
            }
            // But without updating the Swagger, as we have just imported it!
            createdAPI = apiAdapter.updateAPIProxy(createdAPI);
            // If an image is included, update it
            if (desiredAPI.getImage() != null) {
                apiAdapter.updateAPIImage(createdAPI, desiredAPI.getImage());
            }
            // This is special, as the status is not a normal property and requires some additional actions!
            APIStatusManager statusManager = new APIStatusManager();
            statusManager.update(createdAPI, desiredAPI.getState(), desiredAPI.getVhost());
            apiAdapter.updateRetirementDate(createdAPI, desiredAPI.getRetirementDate());

            if (reCreation && actualAPI.getState().equals(API.STATE_PUBLISHED)) {
                // In case, the existing API is already in use (Published), we have to grant access to our new imported API
                apiAdapter.upgradeAccessToNewerAPI(createdAPI, actualAPI);
            }
            if (EnvironmentProperties.CHECK_CATALOG) {
                apiAdapter.pollCatalogForPublishedState(createdAPI.getId(), createdAPI.getName(), createdAPI.getState());
            }
            // Is a Quota is defined we must manage it
            new APIQuotaManager(desiredAPI, actualAPI).execute(createdAPI);
            // Grant access to the API
            new ManageClientOrgs(desiredAPI, createdAPI).execute(reCreation);
            // Handle subscription to applications
            new ManageClientApps(desiredAPI, createdAPI, actualAPI).execute(reCreation);
            // Provide the ID of the created API to the desired API just for logging purposes
            changes.getDesiredAPI().setId(createdAPI.getId());
            LOG.info("Successfully created {} API: {} {} (ID: {})", createdAPI.getState(), createdAPI.getName(), createdAPI.getVersion(), createdAPI.getId());
            if (!changes.waiting4Approval().isEmpty() && LOG.isInfoEnabled()) {
                LOG.info("{}", changes.waiting4Approval());
            }
        } finally {
            if (createdAPI == null) {
                LOG.warn("Can't create PropertiesExport as createdAPI is null");
            } else {
                APIPropertiesExport.getInstance().setProperty("feApiId", createdAPI.getId());
            }
        }
    }

    public API getCreatedAPI() {
        return createdAPI;
    }
}
