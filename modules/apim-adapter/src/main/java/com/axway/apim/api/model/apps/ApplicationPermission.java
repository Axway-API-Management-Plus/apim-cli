package com.axway.apim.api.model.apps;

import org.apache.commons.lang3.StringUtils;

import com.axway.apim.api.model.User;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonFilter("ApplicationPermissionFilter")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationPermission {
	
	public enum SharePermission {
		view, 
		manage
	}
	
	String id;
	String createdBy;
	String userId;

	@JsonProperty("user")
	String username;
	SharePermission permission;
	
	@JsonIgnore
	User user;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getUserId() {
		if(this.userId!=null) return this.userId;
		if(this.user==null) return null;
		return user.getId();
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUsername() {
		if(username!=null) return username;
		if(this.user==null) return null;
		return user.getLoginName();
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public SharePermission getPermission() {
		return permission;
	}
	public void setPermission(SharePermission permission) {
		this.permission = permission;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof ApplicationPermission) {
			ApplicationPermission otherAppPermission = (ApplicationPermission)other;
			return 
					StringUtils.equals(otherAppPermission.getUsername(), this.getUsername() ) && 
					otherAppPermission.getPermission().equals(this.getPermission())
					;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(username, permission);
	}

	@Override
	public String toString() {
		return "ApplicationPermission [username=" + getUsername() + ", permission=" + getPermission() + "]";
	}
}
