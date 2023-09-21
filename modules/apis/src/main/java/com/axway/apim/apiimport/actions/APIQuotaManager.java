package com.axway.apim.apiimport.actions;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIMethodAdapter;
import com.axway.apim.adapter.apis.APIManagerQuotaAdapter;
import com.axway.apim.adapter.apis.APIManagerQuotaAdapter.Quota;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.QuotaRestrictionType;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class APIQuotaManager {

    private static final Logger LOG = LoggerFactory.getLogger(APIQuotaManager.class);

    private final API desiredState;

    private final API actualState;

    public APIQuotaManager(API desiredState, API actualState) {
        this.desiredState = desiredState;
        this.actualState = actualState;
    }

    public void execute(API createdAPI) throws AppException {
        if (desiredState.getApplicationQuota() == null && desiredState.getSystemQuota() == null
                && (actualState == null || (actualState.getApplicationQuota() == null && actualState.getSystemQuota() == null)))
            return;
        if (CoreParameters.getInstance().isIgnoreQuotas() || CoreParameters.getInstance().getQuotaMode().equals(CoreParameters.Mode.ignore)) {
            LOG.info("Configured quotas will be ignored, as ignoreQuotas is true or QuotaMode has been set to ignore.");
            return;
        }
        boolean sameAPI = this.actualState != null && this.actualState.getId().equals(createdAPI.getId());
        // Handle the system quota
        List<QuotaRestriction> actualRestrictions = actualState != null ? getRestrictions(actualState.getSystemQuota()) : null;
        List<QuotaRestriction> desiredRestrictions = getRestrictions(desiredState.getSystemQuota());
        updateRestrictions(actualRestrictions, desiredRestrictions, createdAPI, Quota.SYSTEM_DEFAULT, sameAPI);
        // Handle the application quota
        actualRestrictions = actualState != null ? getRestrictions(actualState.getApplicationQuota()) : null;
        desiredRestrictions = getRestrictions(desiredState.getApplicationQuota());
        updateRestrictions(actualRestrictions, desiredRestrictions, createdAPI, Quota.APPLICATION_DEFAULT, sameAPI);
    }

    public void updateRestrictions(List<QuotaRestriction> actualRestrictions, List<QuotaRestriction> desiredRestrictions, API createdAPI, Quota type, boolean sameAPI) throws AppException {
        // If restrictions are equal and the API is not re-created, there is nothing to do
        if (actualRestrictions == null && desiredRestrictions == null) return;
        if (desiredRestrictions != null && desiredRestrictions.equals(actualRestrictions) && sameAPI) {
            LOG.info("{} quota for API: {} is UN-CHANGED. Nothing to do.", type.getFriendlyName(), createdAPI.getName());
        } else {
            APIManagerAPIMethodAdapter methodAdapter = APIManagerAdapter.getInstance().getMethodAdapter();
            APIManagerQuotaAdapter quotaManager =  APIManagerAdapter.getInstance().getQuotaAdapter();
            LOG.info("Updating {} quota for API: {}", type.getFriendlyName(), createdAPI.getName());
            LOG.debug("{}-Restrictions: Desired: {}, Actual: {}", type.getFriendlyName(), desiredRestrictions, actualRestrictions);
            // In order to compare/merge the restrictions, we must translate the desired API-Method-Names, if not a "*", into the methodId of the createdAPI
            if (desiredRestrictions != null) {
                for (QuotaRestriction desiredRestriction : desiredRestrictions) {
                    if ("*".equals(desiredRestriction.getMethod())) continue;
                    desiredRestriction.setMethod(methodAdapter.getMethodForName(createdAPI.getId(), desiredRestriction.getMethod()).getId());
                }
            }
            // Load the entire current default quota
            APIQuota currentDefaultQuota = quotaManager.getDefaultQuota(type);
            List<QuotaRestriction> mergedRestrictions = addOrMergeRestriction(actualRestrictions, desiredRestrictions);
            populateMethodId(createdAPI, mergedRestrictions);
            // If there is an actual API, remove the restrictions for the current actual API
            if (actualState != null) {
                currentDefaultQuota.getRestrictions().removeIf(restriction -> restriction.getApiId().equals(actualState.getId()));
            }
            // Add all new desired restrictions to the Default-Quota
            currentDefaultQuota.getRestrictions().addAll(mergedRestrictions);
            quotaManager.saveQuota(currentDefaultQuota, currentDefaultQuota.getId());
        }
    }

    public List<QuotaRestriction> addOrMergeRestriction(List<QuotaRestriction> existingRestrictions, List<QuotaRestriction> desiredRestrictions) {
        List<QuotaRestriction> mergedRestrictions = new ArrayList<>();
        if (existingRestrictions == null) existingRestrictions = new ArrayList<>();
        boolean existingRestrictionFound = false;
        if (CoreParameters.getInstance().getQuotaMode().equals(CoreParameters.Mode.replace)) {
            LOG.info("Removing existing Quotas for API: {} as quotaMode is set to replace.", this.actualState.getName());
        } else {
            // Otherwise initially take over all existing restrictions for that API.
            mergedRestrictions.addAll(existingRestrictions);
        }
        if (desiredRestrictions != null) {
            // Iterate over the given desired restrictions
            for (QuotaRestriction desiredRestriction : desiredRestrictions) {
                desiredRestriction.setApiId(null);
                // And compare each desired restriction, if it is already included in the existing restrictions
                for (QuotaRestriction existingRestriction : mergedRestrictions) {
                    // It's considered as the same restriction when type, method, period & per are equal
                    if (desiredRestriction.isSameRestriction(existingRestriction, true)) {
                        // If it is the same restriction, we need to update the restriction configuration
                        if (existingRestriction.getType() == QuotaRestrictionType.throttle) {
                            existingRestriction.getConfig().put("messages", desiredRestriction.getConfig().get("messages"));
                        } else {
                            existingRestriction.getConfig().put("mb", desiredRestriction.getConfig().get("mb"));
                        }
                        existingRestrictionFound = true;
                        break;
                    }
                }
                // If we haven't found any existing restriction add a new desired restriction
                if (!existingRestrictionFound) mergedRestrictions.add(desiredRestriction);
            }
        }
        return mergedRestrictions;
    }

    public void populateMethodId(API createdAPI, List<QuotaRestriction> mergedRestrictions) throws AppException{
        APIManagerAPIMethodAdapter methodAdapter = APIManagerAdapter.getInstance().getMethodAdapter();
        for (QuotaRestriction restriction : mergedRestrictions) {
            // Update the API-ID for the API-Restrictions as the API might be re-created.
            restriction.setApiId(createdAPI.getId());
            if (restriction.getMethod().equals("*")) continue;
            // Additionally, we have to change the methodId
            // Load the method for actualAPI to get the name of the method to which the existing quota is applied to
            APIMethod actualMethod = methodAdapter.getMethodForId(actualState.getId(), restriction.getMethod());
            // Now load the new method based on the name for the createdAPI
            APIMethod newMethod = methodAdapter.getMethodForName(createdAPI.getId(), actualMethod.getName());
            // Finally modify the restriction
            restriction.setMethod(newMethod.getId());
        }
    }

    private List<QuotaRestriction> getRestrictions(APIQuota quota) {
        if (quota == null) return Collections.emptyList();
        return quota.getRestrictions();
    }
}
