package com.axway.apim.apiimport.actions;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.APIStatusManager;
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
		
		List<String> allChanges = new Vector<String>();
		allChanges.addAll(changes.getBreakingChanges());
		allChanges.addAll(changes.getNonBreakingChanges());
		
		APIManagerAdapter apiManager = APIManagerAdapter.getInstance();
		
		try {
			apiManager.apiAdapter.updateAPIProxy(changes.getActualAPI());
			
			// If image is include, update it
			if(changes.getNonBreakingChanges().contains("image")) {
				apiManager.apiAdapter.updateAPIImage(changes.getActualAPI());
			}
			
			// This is special, as the status is not a property and requires some additional actions!
			APIStatusManager statusUpdate = new APIStatusManager();
			if(changes.getNonBreakingChanges().contains("state")) {
				statusUpdate.update(changes.getDesiredAPI(), changes.getActualAPI());
			}
			if(changes.getNonBreakingChanges().contains("retirementDate")) {
				apiManager.apiAdapter.updateRetirementDate(changes.getDesiredAPI());
			}
			
			new APIQuotaManager(changes.getDesiredAPI(), changes.getActualAPI()).execute();
			new ManageClientOrgs(changes.getDesiredAPI(), changes.getActualAPI()).execute(false);
			// Handle subscription to applications
			new ManageClientApps(changes.getDesiredAPI(), changes.getActualAPI(), null).execute(false);
		} catch (Exception e) {
			throw e;
		} finally {
			APIPropertiesExport.getInstance().setProperty("feApiId", changes.getActualAPI().getId());
		}
	}

}
