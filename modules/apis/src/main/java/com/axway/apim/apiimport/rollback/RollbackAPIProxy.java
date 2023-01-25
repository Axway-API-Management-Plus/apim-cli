package com.axway.apim.apiimport.rollback;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.api.API;
import com.axway.apim.lib.errorHandling.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RollbackAPIProxy extends AbstractRollbackAction implements RollbackAction {

    private static final Logger LOG = LoggerFactory.getLogger(RollbackAPIProxy.class);


    /**
     * This is the API to be deleted
     */
    API rollbackAPI;

    public RollbackAPIProxy(API rollbackAPI) {
        super();
        this.rollbackAPI = rollbackAPI;
        executeOrder = 10;
        this.name = "Frontend-API";
    }

    @Override
    public void rollback() throws AppException {
        try {
            if (rollbackAPI.getId() != null) { // We already have an ID to the FE-API can delete it directly
                LOG.info("Rollback FE-API: {} (ID: {} / State: {})", this.rollbackAPI.getName(), this.rollbackAPI.getId(), this.rollbackAPI.getState());
                if (rollbackAPI.getId() != null) {
                    this.rollbackAPI = APIManagerAdapter.getInstance().apiAdapter.getAPIWithId(this.rollbackAPI.getId());
                    new APIStatusManager().update(rollbackAPI, API.STATE_UNPUBLISHED, true);
                }
                APIManagerAdapter.getInstance().apiAdapter.deleteAPIProxy(this.rollbackAPI);
            } else {
                // As we don't have the FE-API ID, try to find the FE-API, based on the BE-API-ID
                APIFilter filter = new APIFilter.Builder().hasApiId(rollbackAPI.getApiId()).build();
                API existingAPI = APIManagerAdapter.getInstance().apiAdapter.getAPI(filter, false);// The path is not set at this point, hence we provide null
                if (existingAPI != null) {
                    LOG.info("Rollback FE-API: {} (ID: {} / State: {})", existingAPI.getName(), existingAPI.getId(), existingAPI.getState());
                    if (existingAPI.getState() != null && existingAPI.getState().equals(API.STATE_PUBLISHED)) {
                        new APIStatusManager().update(existingAPI, API.STATE_UNPUBLISHED, true);
                    }
                    APIManagerAdapter.getInstance().apiAdapter.deleteAPIProxy(existingAPI);
                } else {
                    LOG.info("No FE-API found to rollback.");
                }
            }
        } catch (Exception e) {
            LOG.error("Error while deleting FE-API to roll it back", e);
            throw e;
        }
    }
}
