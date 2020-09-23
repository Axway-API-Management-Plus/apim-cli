package com.axway.apim.apiimport.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerQuotaAdapter.Quota;
import com.axway.apim.api.API;
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

	public void execute() throws AppException {
		if(desiredState.getApplicationQuota()==null && desiredState.getSystemQuota()==null) return;
		if(CoreParameters.getInstance().isIgnoreQuotas() || CoreParameters.getInstance().getQuotaMode().equals(CoreParameters.Mode.ignore)) {
			LOG.info("Configured quotas will be ignored, as ignoreQuotas is true or QuotaMode has been set to ignore.");
			return;
		}
		
		if(desiredState.getSystemQuota()!=null) {
			if(desiredState.getSystemQuota().equals(actualState.getSystemQuota())) {
				LOG.info("Default-System-Quota for API: '"+desiredState.getName()+"' is UN-CHANGED. Nothing to do.");
			} else {
				LOG.info("Updating System-Default-Quota for API: " + desiredState.getName());
				LOG.debug("System-Quota-Config: '" + desiredState.getSystemQuota()+"'");
				APIQuota systemQuota = APIManagerAdapter.getInstance().quotaAdapter.getDefaultQuota(Quota.SYSTEM_DEFAULT);
				for(QuotaRestriction restriction : desiredState.getSystemQuota().getRestrictions()) {
					restriction.setApi(actualState.getId());
				}
				addOrMergeRestriction(systemQuota.getRestrictions(), desiredState.getSystemQuota().getRestrictions());
				APIManagerAdapter.getInstance().quotaAdapter.saveQuota(systemQuota, systemQuota.getId());
			}
		}
		if(desiredState.getApplicationQuota()!=null) {
			if(desiredState.getApplicationQuota().equals(actualState.getApplicationQuota())) {
				LOG.info("Default-Application-Quota for API: '"+desiredState.getName()+"' is UN-CHANGED. Nothing to do.");
			} else {
				LOG.info("Updating Application-Default-Quota for API: " + desiredState.getName());
				LOG.debug("Application-Quota-Config: '" + desiredState.getApplicationQuota()+"'");
				APIQuota applicationQuota = APIManagerAdapter.getInstance().quotaAdapter.getDefaultQuota(Quota.APPLICATION_DEFAULT);
				for(QuotaRestriction restriction : desiredState.getApplicationQuota().getRestrictions()) {
					restriction.setApi(actualState.getId());
				}
				addOrMergeRestriction(applicationQuota.getRestrictions(), desiredState.getApplicationQuota().getRestrictions());
				APIManagerAdapter.getInstance().quotaAdapter.saveQuota(applicationQuota, applicationQuota.getId());
			}
		}
	}
	
	private void addOrMergeRestriction(List<QuotaRestriction> existingRestrictions, List<QuotaRestriction> desiredRestrictions) throws AppException {
		List<QuotaRestriction> newDesiredRestrictions = new ArrayList<QuotaRestriction>();
		boolean existingRestrictionFound = false;
		Iterator<QuotaRestriction> it;
		if(CoreParameters.getInstance().getQuotaMode().equals(CoreParameters.Mode.replace)) {
			LOG.info("Removing existing Quotas for API: '"+this.actualState.getName()+"' as quotaMode is set to replace.");
			it = existingRestrictions.iterator();
			// Remove actual existing restrictions for that API
			while(it.hasNext()) {
				QuotaRestriction existingRestriction = it.next();
				if(existingRestriction.getApi().equals(this.actualState.getId())) {
					it.remove();
				}
			}
			
		}
		it = desiredRestrictions.iterator();
		while(it.hasNext()) {
			QuotaRestriction desiredRestriction = it.next();
			for(QuotaRestriction existingRestriction : existingRestrictions) {
				// Don't care about restrictions for another APIs
				if(!existingRestriction.getApi().equals(this.actualState.getId())) {
					continue;
				}
				if(desiredRestriction.isSameRestriction(existingRestriction)) {
					// If it the same restriction, we need to update this one!
					if(existingRestriction.getType()==QuotaRestrictiontype.throttle) {
						existingRestriction.getConfig().put("messages", desiredRestriction.getConfig().get("messages"));
					} else {
						existingRestriction.getConfig().put("mb", desiredRestriction.getConfig().get("mb"));
					}
					existingRestrictionFound = true;
					break;
				}
			}
			// If we haven't found an existing restriction add a new one!
			if(!existingRestrictionFound) newDesiredRestrictions.add(desiredRestriction);
		}
		// And all new desired restrictions
		existingRestrictions.addAll(newDesiredRestrictions);
	}
}
