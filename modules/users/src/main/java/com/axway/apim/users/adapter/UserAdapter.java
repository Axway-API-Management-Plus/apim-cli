package com.axway.apim.users.adapter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.model.User;
import com.axway.apim.lib.errorHandling.AppException;

public abstract class UserAdapter {
	
	protected static Logger LOG = LoggerFactory.getLogger(UserAdapter.class);
	
	List<User> users;

	public UserAdapter() {
		// TODO Auto-generated constructor stub
	}
	
	public abstract boolean readConfig(Object config) throws AppException;
	
	public List<User> getUsers() throws AppException {
		return this.users;
	}
}
