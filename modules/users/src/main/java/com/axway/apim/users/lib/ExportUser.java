package com.axway.apim.users.lib;

import java.util.Map;

import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.User;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "name", "loginName", "email", "role", "organization", "type", "state", "phone", "mobile", "createdOn", "enabled", "image"})
public class ExportUser {
	
	User user;

	public ExportUser(User user) {
		this.user = user;
	}
	
	public String getName() {
		return this.user.getName();
	}
	
	public String getState() {
		return this.user.getState();
	}

	public String getRole() {
		return this.user.getRole();
	}
	
	public String getType() {
		return this.user.getType();
	}
	
	public String getMobile() {
		return this.user.getMobile();
	}
	
	public String getPhone() {
		return this.user.getPhone();
	}

	public String getLoginName() {
		return this.user.getLoginName();
	}

	public String getDescription() {
		return this.user.getDescription();
	}
	
	public boolean isEnabled() {
		return this.user.isEnabled();
	}
	
	public Long getCreatedOn() {
		return this.user.getCreatedOn();
	}	
	
	public String getEmail() {
		return this.user.getEmail();
	}
	
	public Image getImage() {
		return this.user.getImage();
	}

	public String getOrganization() {
		return this.user.getOrganization().getName();
	}
	
	public Long lastSeen() {
		return this.user.getAuthNUserAttributes().getLastSeen();
	}
	
	public Map<String, String> getCustomProperties() {
		return this.user.getCustomProperties();
	}
}
