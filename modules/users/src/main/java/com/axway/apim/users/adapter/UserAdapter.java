package com.axway.apim.users.adapter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.model.User;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.users.lib.UserImportParams;

public abstract class UserAdapter {
	
	protected static Logger LOG = LoggerFactory.getLogger(UserAdapter.class);
	
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
