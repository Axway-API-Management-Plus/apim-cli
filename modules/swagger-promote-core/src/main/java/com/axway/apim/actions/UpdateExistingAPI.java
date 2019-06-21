package com.axway.apim.actions;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.tasks.ManageClientApps;
import com.axway.apim.actions.tasks.ManageClientOrgs;
import com.axway.apim.actions.tasks.UpdateAPIImage;
import com.axway.apim.actions.tasks.UpdateAPIProxy;
import com.axway.apim.actions.tasks.UpdateAPIStatus;
import com.axway.apim.actions.tasks.UpdateQuotaConfiguration;
import com.axway.apim.actions.tasks.props.VhostPropertyHandler;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.APIPropertiesExport;
import com.axway.apim.swagger.APIChangeState;
import com.axway.apim.swagger.APIManagerAdapter;

/**
 * This class is used by the {@link APIManagerAdapter#applyChanges(APIChangeState)} to update an existing API. 
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
		
		VhostPropertyHandler vHostHandler = new VhostPropertyHandler(allChanges);

		new UpdateAPIProxy(changes.getDesiredAPI(), changes.getActualAPI()).execute(allChanges);
		
		// If image is include, update it
		if(changes.getNonBreakingChanges().contains("image")) {
			new UpdateAPIImage(changes.getDesiredAPI(), changes.getActualAPI()).execute();
		}
		
		// This is special, as the status is not a property and requires some additional actions!
		if(changes.getNonBreakingChanges().contains("state")) {
			new UpdateAPIStatus(changes.getDesiredAPI(), changes.getActualAPI()).execute();
		}
		
		vHostHandler.handleVHost(changes.getDesiredAPI(), changes.getActualAPI());
		
		new UpdateQuotaConfiguration(changes.getDesiredAPI(), changes.getActualAPI()).execute();
		new ManageClientOrgs(changes.getDesiredAPI(), changes.getActualAPI()).execute(false);
		// Handle subscription to applications
		new ManageClientApps(changes.getDesiredAPI(), changes.getActualAPI(), null).execute(false);
		
		APIPropertiesExport.getInstance().setProperty("feApiId", changes.getActualAPI().getApiId());
	}

}
