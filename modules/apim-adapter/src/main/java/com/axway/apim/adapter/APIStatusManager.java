package com.axway.apim.adapter;

import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.api.API;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class APIStatusManager {

    private static final Logger LOG = LoggerFactory.getLogger(APIStatusManager.class);

    private boolean updateVHostRequired = false;

    private enum StatusChangeMap {
        unpublished(new String[]{Constants.API_PUBLISHED, Constants.API_DELETED}),
        published(new String[]{Constants.API_UNPUBLISHED, Constants.API_DEPRECATED}),
        deleted(new String[]{}),
        deprecated(new String[]{Constants.API_UNPUBLISHED, Constants.API_UNDEPRECATED}),
        undeprecated(new String[]{Constants.API_PUBLISHED, Constants.API_UNPUBLISHED}),
        pending(new String[]{Constants.API_DELETED});

        private final String[] possibleStates;

        StatusChangeMap(String[] possibleStates) {
            this.possibleStates = possibleStates;
        }
    }

    private enum StatusChangeRequiresEnforce {
        published(new String[]{Constants.API_UNPUBLISHED, Constants.API_DELETED}),
        deprecated(new String[]{Constants.API_UNPUBLISHED, Constants.API_DELETED});

        private final List<String> enforceRequired;

        StatusChangeRequiresEnforce(String[] enforceRequired) {
            this.enforceRequired = Arrays.asList(enforceRequired);
        }

        public static StatusChangeRequiresEnforce getEnum(String value) {
            try {
                return StatusChangeRequiresEnforce.valueOf(value);
            } catch (Exception ignore) {
                return null;
            }
        }
    }

    public void update(API apiToUpdate, String desiredState, boolean enforceBreakingChange) throws AppException {
        update(apiToUpdate, desiredState, null, enforceBreakingChange);
    }

    public void update(API apiToUpdate, String desiredState) throws AppException {
        update(apiToUpdate, desiredState, null);
    }

    public void update(API apiToUpdate, String desiredState, String vhost) throws AppException {
        update(apiToUpdate, desiredState, vhost, CoreParameters.getInstance().isForce());
    }


    public void update(API apiToUpdate, String desiredState, String vhost, boolean enforceBreakingChange) throws AppException {
        if (desiredState.equals(apiToUpdate.getState())) {
            LOG.debug("Desired and actual status equal. No need to update status!");
            return;
        }
        LOG.debug("Updating API-Status from: {} to {}", apiToUpdate.getState(), desiredState);
        if (!enforceBreakingChange && (StatusChangeRequiresEnforce.getEnum(apiToUpdate.getState()) != null &&
                StatusChangeRequiresEnforce.valueOf(apiToUpdate.getState()).enforceRequired.contains(desiredState))) {
                throw new AppException("Status change from actual status: '" + apiToUpdate.getState() + "' to desired status: '" + desiredState + "' "
                    + "is breaking. Enforce change with option: -force", ErrorCode.BREAKING_CHANGE_DETECTED);

        }
        try {
            APIManagerAdapter apimAdapter = APIManagerAdapter.getInstance();
            String[] possibleStatus = StatusChangeMap.valueOf(apiToUpdate.getState()).possibleStates;
            String intermediateState = null;
            boolean statusMovePossible = false;
            APIManagerAPIAdapter apiAdapter = apimAdapter.getApiAdapter();
            for (String status : possibleStatus) {
                if (desiredState.equals(status)) {
                    statusMovePossible = true; // Direct move to new state possible
                    break;
                } else {
                    String[] possibleStatus2 = StatusChangeMap.valueOf(status).possibleStates;
                    if (possibleStatus2 != null) {
                        for (String subStatus : possibleStatus2) {
                            if (desiredState.equals(subStatus)) {
                                intermediateState = status;
                                statusMovePossible = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (statusMovePossible) {
                if (intermediateState != null) {
                    LOG.debug("Required intermediate state: {}", intermediateState);
                    // In case, we can't process directly, we have to perform an intermediate state change
                    new APIStatusManager().update(apiToUpdate, intermediateState, vhost, enforceBreakingChange);
                    if (desiredState.equals(apiToUpdate.getState())) return;
                }
            } else {
                LOG.error("The status change from: {} to: {} is not possible!", apiToUpdate.getState(), desiredState);
                throw new AppException("The status change from: '" + apiToUpdate.getState() + "' to '" + desiredState + "' is not possible!", ErrorCode.CANT_UPDATE_API_STATUS);
            }
            apiToUpdate.setState(desiredState);
            if (desiredState.equals(Constants.API_DELETED)) {
                // If an API in state unpublished or pending, also an orgAdmin can delete it
                if (apiAdapter.isFrontendApiExists(apiToUpdate))
                    apiAdapter.deleteAPIProxy(apiToUpdate);
                // Additionally we need to delete the BE-API
                if (apiAdapter.isBackendApiExists(apiToUpdate))
                    apiAdapter.deleteBackendAPI(apiToUpdate);
            } else {
                apiAdapter.updateAPIStatus(apiToUpdate, desiredState, vhost);
                if (vhost != null && desiredState.equals(Constants.API_UNPUBLISHED)) {
                    this.updateVHostRequired = true; // Flag to control update of the VHost
                }
            }
            // Take over the status, as it has been updated now
            apiToUpdate.setState(desiredState);
            // When deprecation or undeprecation is requested, we have to set the actual API accordingly!
            if (desiredState.equals(Constants.API_UNDEPRECATED)) {
                apiToUpdate.setDeprecated("false");
                apiToUpdate.setState(Constants.API_PUBLISHED);
            } else if (desiredState.equals(Constants.API_DEPRECATED)) {
                apiToUpdate.setState(Constants.API_PUBLISHED);
                apiToUpdate.setDeprecated("true");
            }
        } catch (Exception e) {
            throw new AppException("The status change from: '" + apiToUpdate.getState() + "' to '" + desiredState + "' is not possible!", ErrorCode.CANT_UPDATE_API_STATUS, e);
        }
    }

    /**
     * @return true, if the API has been set to unpublished and the VHost needs to be updated
     */
    public boolean isUpdateVHostRequired() {
        return updateVHostRequired;
    }
}
