package com.axway.apim.apiimport.rollback;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter;
import com.axway.apim.adapter.apis.APIFilter.Builder.APIType;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.api.API;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RollbackBackendAPI extends AbstractRollbackAction implements RollbackAction {

    private static final Logger LOG = LoggerFactory.getLogger(RollbackBackendAPI.class);

    /**
     * This is the API to be deleted
     */
    API rollbackAPI;

    public RollbackBackendAPI(API rollbackAPI) {
        super();
        this.rollbackAPI = rollbackAPI;
        this.executeOrder = 20;
        this.name = "Backend-API";
    }

    @Override
    public void rollback() throws AppException {
        try {
            APIManagerAPIAdapter apiAdapter = APIManagerAdapter.getInstance().getApiAdapter();
            apiAdapter.deleteBackendAPI(rollbackAPI);
            /*
             * API-Manager 7.7 creates unfortunately two APIs at the same time, when importing a backend-API
             * having both schemas: https & http.
             * On top to that problem, only ONE backend-API-ID is returned when creating the BE-API-ID. The following
             * code tries to find the other Backend-API, which has been created almost at the same time.
             */
            if (APIManagerAdapter.hasAPIManagerVersion("7.7")) {
                rolledBack = true;
                Long beAPICreatedOn = rollbackAPI.getCreatedOn();
                // The createdOn of the API we are looking for, should be almost created at the same time, as the code runs internally in API-Manager.
                beAPICreatedOn = beAPICreatedOn - 1000;
                APIFilter filter = new APIFilter.Builder(APIType.CUSTOM, true)
                        .hasName(rollbackAPI.getName().replace(" HTTPS", " HTTP"))
                        .isCreatedOnAfter((beAPICreatedOn).toString())
                        .build();
                API existingBEAPI = apiAdapter.getAPI(filter, false);
                if (existingBEAPI != null && existingBEAPI.getId() != null) {
                    existingBEAPI.setApiId(existingBEAPI.getId());
                    apiAdapter.deleteBackendAPI(existingBEAPI);
                }
            }
        } catch (Exception e) {
            LOG.error("Error while deleting BE-API with ID: {} to roll it back", rollbackAPI.getApiId(), e);
            throw new AppException("Rollback as Error while deleting BE-API", ErrorCode.UNXPECTED_ERROR);
        }

    }
}
