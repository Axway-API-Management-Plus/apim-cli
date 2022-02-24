package com.axway.apim.apiimport.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerQuotaAdapter.Quota;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.QuotaRestriction;
import com.axway.apim.api.model.QuotaRestrictiontype;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;

public class APIQuotaManager {
	
	static Logger LOG = LoggerFactory.getLogger(APIQuotaManager.class);
	
	private API desiredState;
	
	private API actualState;

	public APIQuotaManager(API desiredState, API actualState) {
		this.desiredState = desiredState;
		this.actualState = actualState;
	}

	public void execute(API createdAPI) throws AppException {
		if(desiredState.getApplicationQuota()==null && desiredState.getSystemQuota()==null 
				&& (actualState==null || (actualState.getApplicationQuota()==null && actualState.getSystemQuota()==null))) return;
		if(CoreParameters.getInstance().isIgnoreQuotas() || CoreParameters.getInstance().getQuotaMode().equals(CoreParameters.Mode.ignore)) {
			LOG.info("Configured quotas will be ignored, as ignoreQuotas is true or QuotaMode has been set to ignore.");
			return;
		}
		boolean sameAPI = false;
		if(this.actualState!=null && this.actualState.getId().equals(createdAPI.getId())) {
			sameAPI = true;
		}
		// Handle the system quota
		List<QuotaRestriction> actualRestrictions  = actualState!=null ? getRestrictions(actualState.getSystemQuota()) : null;
		List<QuotaRestriction> desiredRestrictions = desiredState!=null ? getRestrictions(desiredState.getSystemQuota()) : null;
		updateRestrictions(actualRestrictions, desiredRestrictions, createdAPI, Quota.SYSTEM_DEFAULT, sameAPI);
		
		// Handle the application quota
		actualRestrictions  = actualState!=null ? getRestrictions(actualState.getApplicationQuota()) : null;
		desiredRestrictions = desiredState!=null ? getRestrictions(desiredState.getApplicationQuota()) : null;
		updateRestrictions(actualRestrictions, desiredRestrictions, createdAPI, Quota.APPLICATION_DEFAULT, sameAPI);
	}
	
	private void updateRestrictions(List<QuotaRestriction> actualRestrictions, List<QuotaRestriction> desiredRestrictions, API createdAPI, Quota type, boolean sameAPI) throws AppException {
		// If restrictions are equal and the API is not re-created, there is nothing to do
		if(actualRestrictions==null && desiredRestrictions==null) return;
		if(desiredRestrictions!=null && desiredRestrictions.equals(actualRestrictions) && sameAPI) {
			LOG.info(type.getFiendlyName() + " quota for API: '"+createdAPI.getName()+"' is UN-CHANGED. Nothing to do.");
		} else {
			LOG.info("Updating "+type.getFiendlyName()+" quota for API: " + createdAPI.getName());
			LOG.debug(type.getFiendlyName()+"-Restrictions: Desired: '" + desiredRestrictions+"', Actual: '" + actualRestrictions+"'");
			// In order to compare/merge the restrictions, we must translate the desired API-Method-Names, if not a "*", into the methodId of the createdAPI
			for(QuotaRestriction desiredRestriction : desiredRestrictions) {
				if("*".equals(desiredRestriction.getMethod())) continue;
				desiredRestriction.setMethod(APIManagerAdapter.getInstance().methodAdapter.getMethodForName(createdAPI.getId(), desiredRestriction.getMethod()).getId());
			}
			// Load the entire current default quota
			APIQuota currentDefaultQuota = APIManagerAdapter.getInstance().quotaAdapter.getDefaultQuota(type);
			List<QuotaRestriction> mergedRestrictions = addOrMergeRestriction(actualRestrictions, desiredRestrictions);
			for(QuotaRestriction restriction : mergedRestrictions) {
				// Update the API-ID for the API-Restrictions as the API might be re-created.
				restriction.setApiId(createdAPI.getId());
				if(restriction.getMethod().equals("*")) continue;
				// Additionally, we have to change the methodId
				// Load the method for actualAPI to get the name of the method to which the existing quota is applied to
				APIMethod actualMethod = APIManagerAdapter.getInstance().methodAdapter.getMethodForId(actualState.getId(), restriction.getMethod()); 
				// Now load the new method based on the name for the createdAPI
				APIMethod newMethod = APIManagerAdapter.getInstance().methodAdapter.getMethodForName(createdAPI.getId(), actualMethod.getName());
				// Finally modify the restriction
				restriction.setMethod(newMethod.getId());
			}
			// If there is an actual API, remove the restrictions for the current actual API
			if(actualState!=null) {
				Iterator<QuotaRestriction> it = currentDefaultQuota.getRestrictions().iterator();
				while(it.hasNext()) {
					QuotaRestriction restriction = it.next();
					if(restriction.getApiId().equals(actualState.getId())) {
						it.remove();
					}
				}
			}
			// Add all new desired restrictions to the Default-Quota
			currentDefaultQuota.getRestrictions().addAll(mergedRestrictions);
			APIManagerAdapter.getInstance().quotaAdapter.saveQuota(currentDefaultQuota, currentDefaultQuota.getId());
		}
	}
	
	private List<QuotaRestriction> addOrMergeRestriction(List<QuotaRestriction> existingRestrictions, List<QuotaRestriction> desiredRestrictions) throws AppException {
		List<QuotaRestriction> mergedRestrictions = new ArrayList<QuotaRestriction>();
		if(existingRestrictions==null) existingRestrictions = new ArrayList<QuotaRestriction>();
		boolean existingRestrictionFound = false;
		if(CoreParameters.getInstance().getQuotaMode().equals(CoreParameters.Mode.replace)) {
			LOG.info("Removing existing Quotas for API: '"+this.actualState.getName()+"' as quotaMode is set to replace.");
		} else {
			// Otherwise initially take over all existing restrictions for that API.
			mergedRestrictions.addAll(existingRestrictions);
		}
		if(desiredRestrictions!=null) {
			// Iterate over the given desired restrictions
			Iterator<QuotaRestriction> it = desiredRestrictions.iterator();
			while(it.hasNext()) {
				QuotaRestriction desiredRestriction = it.next();
				desiredRestriction.setApiId(null);
				// And compare each desired restriction, if it is already included in the existing restrictions
				for(QuotaRestriction existingRestriction : mergedRestrictions) {
					// It's considered as the same restriction when type, method, period & per are equal
					if(desiredRestriction.isSameRestriction(existingRestriction, true)) {
						// If it is the same restriction, we need to update the restriction configuration
						if(existingRestriction.getType()==QuotaRestrictiontype.throttle) {
							existingRestriction.getConfig().put("messages", desiredRestriction.getConfig().get("messages"));
						} else {
							existingRestriction.getConfig().put("mb", desiredRestriction.getConfig().get("mb"));
						}
						existingRestrictionFound = true;
						break;
					}
				}
				// If we haven't found any existing restriction add a new desired restriction
				if(!existingRestrictionFound) mergedRestrictions.add(desiredRestriction);
			}
		}
		return mergedRestrictions;
	}
	
	private List<QuotaRestriction> getRestrictions(APIQuota quota) {
		if(quota==null) return null;
		return quota.getRestrictions();
	}
}
