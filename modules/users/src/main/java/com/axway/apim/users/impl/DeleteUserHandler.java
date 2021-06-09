package com.axway.apim.users.impl;

import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.user.UserFilter;
import com.axway.apim.adapter.user.UserFilter.Builder;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.ActionResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.users.lib.params.UserExportParams;

public class DeleteUserHandler extends UserResultHandler {

	public DeleteUserHandler(UserExportParams params, ExportResult result) {
		super(params, result);
	}

	@Override
	public ActionResult export(List<User> users) throws AppException {
		ActionResult result = new ActionResult();
		System.out.println(users.size() + " selected for deletion.");
		if(CoreParameters.getInstance().isForce()) {
			System.out.println("Force flag given to delete: "+users.size()+" User(s)");
		} else {
			if(Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
			} else {
				System.out.println("Canceled.");
				return result;
			}
		}
		System.out.println("Okay, going to delete: " + users.size() + " Users(s)");
		for(User user : users) {
			try {
				APIManagerAdapter.getInstance().userAdapter.deleteUser(user);
			} catch(Exception e) {
				LOG.error("Error deleting user: " + user.getName());
			}
		}
		System.out.println("Done!");
		return result;
	}

	@Override
	public UserFilter getFilter() {
		Builder builder = getBaseFilterBuilder();
		return builder.build();
	}
}
