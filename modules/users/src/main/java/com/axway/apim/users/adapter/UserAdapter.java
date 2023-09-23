package com.axway.apim.users.adapter;

import com.axway.apim.api.model.User;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.users.lib.UserImportParams;

import java.util.List;

public abstract class UserAdapter {


	List<User> users;

	UserImportParams importParams;

	protected UserAdapter(UserImportParams importParams) {
		this.importParams = importParams;
	}

	abstract void readConfig() throws AppException;

	public List<User> getUsers() throws AppException {
		if(this.users == null) readConfig();
		return this.users;
	}
}
