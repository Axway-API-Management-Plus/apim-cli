package com.axway.apim.apiimport.actions;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.apiimport.APIImportManager;
import com.axway.apim.apiimport.actions.tasks.ManageClientApps;
import com.axway.apim.apiimport.actions.tasks.ManageClientOrgs;
import com.axway.apim.apiimport.actions.tasks.UpdateAPIImage;
import com.axway.apim.apiimport.actions.tasks.UpdateAPIProxy;
import com.axway.apim.apiimport.actions.tasks.UpdateAPIStatus;
import com.axway.apim.apiimport.actions.tasks.UpdateQuotaConfiguration;
import com.axway.apim.apiimport.actions.tasks.props.VhostPropertyHandler;
import com.axway.apim.apiimport.state.APIChangeState;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.APIPropertiesExport;

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
		
		try {
		
			VhostPropertyHandler vHostHandler = new VhostPropertyHandler(allChanges);
	
			new UpdateAPIProxy(changes.getDesiredAPI(), changes.getActualAPI()).execute(allChanges);
			
			// If image is include, update it
			if(changes.getNonBreakingChanges().contains("image")) {
				new UpdateAPIImage(changes.getDesiredAPI(), changes.getActualAPI()).execute();
			}
			
			// This is special, as the status is not a property and requires some additional actions!
			UpdateAPIStatus statusUpdate = new UpdateAPIStatus(changes.getDesiredAPI(), changes.getActualAPI());
			if(changes.getNonBreakingChanges().contains("state")) {
				statusUpdate.execute();
			}
			if(changes.getNonBreakingChanges().contains("retirementDate")) {
				statusUpdate.updateRetirementDate(changes);
			}
			
			vHostHandler.handleVHost(changes.getDesiredAPI(), changes.getActualAPI(), statusUpdate.isUpdateVHostRequired());
			
			new UpdateQuotaConfiguration(changes.getDesiredAPI(), changes.getActualAPI()).execute();
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
