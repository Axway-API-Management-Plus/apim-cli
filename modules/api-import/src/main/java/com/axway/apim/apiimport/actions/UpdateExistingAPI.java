package com.axway.apim.apiimport.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIStatusManager;
import com.axway.apim.api.API;
import com.axway.apim.api.state.APIChangeState;
import com.axway.apim.apiimport.APIImportManager;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.lib.errorHandling.AppException;

/**
 * This class is used by the {@link APIImportManager#applyChanges(APIChangeState)} to update an existing API. 
 * This happens, when all changes can be applied to the existing API which is quite of the case for an "Unpublished" API.
 * 
 * @author cwiechmann@axway.com
 */
public class UpdateExistingAPI {
	
	static Logger LOG = LoggerFactory.getLogger(UpdateExistingAPI.class);

	public void execute(APIChangeState changes) throws AppException {
		
		API actualAPI = changes.getActualAPI();
		
		APIManagerAdapter apiManager = APIManagerAdapter.getInstance();
		
		try {
			LOG.info("Update existing "+actualAPI.getState()+" API: '"+actualAPI.getName()+"' "+actualAPI.getVersion()+" (ID: "+actualAPI.getId()+")" );
			// Copy all desired proxy changes into the actual API
			APIChangeState.copyChangedProps(changes.getDesiredAPI(), changes.getActualAPI(), changes.getAllChanges());
			
			// If a proxy update is required
			if(changes.isProxyUpdateRequired()) {
				// Update the proxy
				apiManager.apiAdapter.updateAPIProxy(changes.getActualAPI());
			}
			
			// If image an include, update it
			if(changes.getDesiredAPI().getImage()!=null) {
				apiManager.apiAdapter.updateAPIImage(changes.getActualAPI(), changes.getDesiredAPI().getImage());
			}
			
			// This is special, as the status is not a property and requires some additional actions!
			APIStatusManager statusUpdate = new APIStatusManager();
			if(changes.getNonBreakingChanges().contains("state")) {
				statusUpdate.update(changes.getActualAPI(), changes.getDesiredAPI().getState());
			}
			if(changes.getNonBreakingChanges().contains("retirementDate")) {
				apiManager.apiAdapter.updateRetirementDate(changes.getActualAPI(), changes.getDesiredAPI().getRetirementDate());
			}
			
			// This is required when an API has been set back to Unpublished
			// In that case, the V-Host is reseted to null - But we still want to use the configured V-Host
			if(statusUpdate.isUpdateVHostRequired()) {
				apiManager.apiAdapter.updateAPIProxy(changes.getActualAPI());
			}
			
			new APIQuotaManager(changes.getDesiredAPI(), changes.getActualAPI()).execute();
			new ManageClientOrgs(changes.getDesiredAPI(), changes.getActualAPI()).execute(false);
			// Handle subscription to applications
			new ManageClientApps(changes.getDesiredAPI(), changes.getActualAPI(), null).execute(false);
			LOG.info("Successfully updated "+actualAPI.getState()+" API: '"+actualAPI.getName()+"' "+actualAPI.getVersion()+" (ID: "+actualAPI.getId()+")" );
		} catch (Exception e) {
			throw e;
		} finally {
			APIPropertiesExport.getInstance().setProperty("feApiId", changes.getActualAPI().getId());
		}
	}

}
