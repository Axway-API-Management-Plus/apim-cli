package com.axway.apim.actions;

import java.util.List;
import java.util.Vector;

import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.actions.tasks.UpdateAPIImage;
import com.axway.apim.actions.tasks.UpdateAPIProxy;
import com.axway.apim.actions.tasks.UpdateAPIStatus;
import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.APIChangeState;

public class UpdateExistingAPI {

	public void execute(APIChangeState changes) throws AppException {

		Transaction.getInstance().beginTransaction();
		Transaction.getInstance().put("orgId", "0926142d-1049-4847-a1ea-9063d9e1c135");
		
		List<String> allChanges = new Vector<String>();
		allChanges.addAll(changes.getBreakingChanges());
		allChanges.addAll(changes.getNonBreakingChanges());
		
		new UpdateAPIProxy(changes.getDesiredAPI(), changes.getActualAPI()).execute(allChanges);
		
		// If image is include, update it
		if(changes.getNonBreakingChanges().contains("apiImage")) {
			new UpdateAPIImage(changes.getDesiredAPI(), changes.getActualAPI()).execute();
		}
		
		// This is special, as the status is not a property and requires some additional actions!
		if(changes.getNonBreakingChanges().contains("status")) {
			new UpdateAPIStatus(changes.getDesiredAPI(), changes.getActualAPI()).execute();
		}
	}

}
