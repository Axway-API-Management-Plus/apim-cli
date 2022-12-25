package com.axway.apim.users.impl;

import java.util.List;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.user.UserFilter;
import com.axway.apim.adapter.user.UserFilter.Builder;
import com.axway.apim.api.model.User;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.ExportResult;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.users.lib.params.UserChangePasswordParams;
import com.axway.apim.users.lib.params.UserExportParams;

public class UserChangePasswordHandler extends UserResultHandler {
	
	String newPassword;

	public UserChangePasswordHandler(UserExportParams params, ExportResult result) {
		super(params, result);
		this.newPassword = ((UserChangePasswordParams)params).getNewPassword();
	}

	@Override
	public void export(List<User> users) throws AppException {
		System.out.println(users.size() + " user(s) selected to change the password.");
		if(CoreParameters.getInstance().isForce()) {
			System.out.println("Force flag given to change the password for: "+users.size()+" User(s)");
		} else {
			if(Utils.askYesNo("Do you wish to proceed? (Y/N)")) {
			} else {
				System.out.println("Canceled.");
				return;
			}
		}
		System.out.println("Okay, going to change the password for: " + users.size() + " Users(s)");
		for(User user : users) {
			try {
				APIManagerAdapter.getInstance().userAdapter.changepassword(newPassword, user);
			} catch(Exception e) {
				LOG.error("Error changing password of user: {}", user.getName());
			}
		}
		System.out.println("Done!");
	}

	@Override
	public UserFilter getFilter() {
		Builder builder = getBaseFilterBuilder();
		return builder.build();
	}
}
