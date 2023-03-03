package com.axway.apim.api.export.impl;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder.APIType;
import com.axway.apim.api.API;
import com.axway.apim.api.export.lib.params.APIChangeParams;
import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.api.model.ServiceProfile;
import com.axway.apim.apiimport.APIChangeState;
import com.axway.apim.apiimport.APIImportManager;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.rest.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class APIChangeHandler extends APIResultHandler {
    private static final Logger LOG = LoggerFactory.getLogger(APIChangeHandler.class);
    APIChangeParams changeParams;

    public APIChangeHandler(APIExportParams params) {
        super(params);
        this.changeParams = (APIChangeParams) params;
    }

    @Override
    public void execute(List<API> apis) throws AppException {
        APIManagerAdapter adapter = APIManagerAdapter.getInstance();
        List<APIChangeState> apisToChange = new ArrayList<>();
        LOG.info("{} selected to change.", apis.size());
        for (API api : apis) {
            try {
                if (changeParams.getNewBackend() != null) {
                    api = changeBackendBasePath(api, changeParams.getNewBackend(), changeParams.getOldBackend());
                }
                // Reload the actual API again, to get a clone
                API actualAPI = adapter.apiAdapter.getAPI(new APIFilter.Builder(APIType.ACTUAL_API).hasId(api.getId()).build(), false);
                APIChangeState changeState = new APIChangeState(actualAPI, api);
                if (!changeState.hasAnyChanges()) {
                    LOG.warn("No changes for API: {}", api.getName());
                    continue;
                }
                if (changeState.isBreaking() && !params.isForce()) {
                    result.setError(ErrorCode.BREAKING_CHANGE_DETECTED);
                    LOG.error("Changing API: {} is a potentially breaking change which can't be applied without enforcing it! Try option: -force", api.getName());
                    continue;
                }
                LOG.info("Planned changes for API: {}: {}", api.getName(), changeState.getAllChanges());
                apisToChange.add(changeState);
            } catch (Exception e) {
                LOG.error("Error preparing required changes for API: " + api.getName(), e);
            }
        }
        if (apisToChange.isEmpty()) {
            Console.println("No changes required for the selected APIs.");
            return;
        }
        if (CoreParameters.getInstance().isForce()) {
            Console.println("Force flag given to change: " + apis.size() + " API(s)");
        } else {
            Console.println("Okay, going to change: " + apisToChange.size() + " API(s)");
            if (Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
            } else {
                Console.println("Canceled.");
                return;
            }
        }
        APIImportManager importManager = new APIImportManager();
        for (APIChangeState changeState : apisToChange) {
            LOG.info("Apply changes for API: {}", changeState.getDesiredAPI().getName());
            try {
                importManager.applyChanges(changeState, false, false);
            } catch (Exception e) {
                LOG.error("Error applying changes for API: " + changeState.getDesiredAPI().getName(), e);
            }
        }
        Console.println("Done!");
    }

    @Override
    public APIFilter getFilter() {
        // We need to load the complete API, as this becomes the desired API
        // hence, all App, Orgs, Quotas, etc. must be taken over the new API
        return getBaseAPIFilterBuilder()
                .includeCustomProperties(getAPICustomProperties())
                .includeOriginalAPIDefinition(true)
                .includeClientApplications(true)
                .includeClientAppQuota(true)
                .includeClientOrganizations(true)
                .includeQuotas(true)
                .build();
    }

    private API changeBackendBasePath(API api, String newBackendBasepath, String oldBackendBasepath) throws AppException {
        if (oldBackendBasepath != null) {
            String actualBackend = getActualBackendBasePath(api);
            if (!oldBackendBasepath.equals(actualBackend)) {
                LOG.warn("Backend of API: {} wont be changed as it has a different backend configured. Current: {} New: {}", api.getName(), actualBackend, oldBackendBasepath);
                return api;
            }
        }
        for (ServiceProfile profile : api.getServiceProfiles().values()) {
            profile.setBasePath(newBackendBasepath);
        }
        return api;
    }

    private String getActualBackendBasePath(API api) throws AppException {
        String lastBasepath = null;
        for(ServiceProfile profile: api.getServiceProfiles().values()){
            if (lastBasepath != null && !lastBasepath.equals(profile.getBasePath())) {
                throw new AppException("API has multiple backends configured. Please export - change - import the API to change it.", ErrorCode.UNSUPPORTED_FEATURE);
            }
            lastBasepath = profile.getBasePath();
        }
        return lastBasepath;
    }
}
