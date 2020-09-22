package com.axway.apim.users.lib;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.StandardExportParams;

public class UserExportParams extends StandardExportParams {
	
	private String loginName;
	private String name;
	private String email;
	private String id;
	private String org;
	private String type;
	private String role;
	private String state;
	private Boolean enabled;
	
	public static synchronized UserExportParams getInstance() {
		return (UserExportParams)CoreParameters.getInstance();
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		if(enabled==null) return; // We stay with the default
		this.enabled = enabled;
	}
}
