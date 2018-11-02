package com.axway.apim.actions;

import java.util.List;
import java.util.Vector;

import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.actions.tasks.UpdateAPIProxy;
import com.axway.apim.actions.tasks.UpdateAPIStatus;
import com.axway.apim.swagger.APIChangeState;

public class UpdateExistingAPI extends AbstractCommandHandler {

	@Override
	public void execute(APIChangeState changes) {

		Transaction.getInstance().beginTransaction();
		Transaction.getInstance().put("orgId", "0926142d-1049-4847-a1ea-9063d9e1c135");
		
		List<String> allChanges = new Vector<String>();
		allChanges.addAll(changes.getBreakingChanges());
		allChanges.addAll(changes.getNonBreakingChanges());
		
		RestAPICall call = UpdateAPIProxy.execute(changes.getDesiredAPI(), changes.getActualAPI(), allChanges);
		super.executeAPICall(call);
		// This is special, as the status is not a property and requires some additional actions!
		if(changes.getBreakingChanges().contains("status")) {
			call = UpdateAPIStatus.execute(changes.getDesiredAPI(), changes.getActualAPI());
			super.executeAPICall(call);
		}
	}

}
