package com.axway.apim.users.lib.params;

public interface UserFilterParams {
	
	void setName(String name);
	void setLoginName(String loginName);
	void setEmail(String email);
	void setType(String type);
	void setOrg(String org);
	void setRole(String role);
	void setState(String state);
	void setEnabled(Boolean enabled);
	void setId(String id);
}
