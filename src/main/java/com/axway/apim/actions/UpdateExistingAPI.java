package com.axway.apim.actions;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.tasks.UpdateAPIImage;
import com.axway.apim.actions.tasks.UpdateAPIProxy;
import com.axway.apim.actions.tasks.UpdateAPIStatus;
import com.axway.apim.actions.tasks.props.VhostPropertyHandler;
import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.APIChangeState;

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
	}

}
