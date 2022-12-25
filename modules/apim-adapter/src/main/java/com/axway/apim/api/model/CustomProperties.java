package com.axway.apim.api.model;

import java.util.Map;

public class CustomProperties {
	private Map<String, CustomProperty> user;
	
	private Map<String, CustomProperty> organization;
	
	private Map<String, CustomProperty> application;
	
	private Map<String, CustomProperty> api; 

	public Map<String, CustomProperty> getUser() {
		return user;
	}

	public void setUser(Map<String, CustomProperty> user) {
		this.user = user;
	}

	public Map<String, CustomProperty> getOrganization() {
		return organization;
	}

	public void setOrganization(Map<String, CustomProperty> organization) {
		this.organization = organization;
	}

	public Map<String, CustomProperty> getApplication() {
		return application;
	}

	public void setApplication(Map<String, CustomProperty> application) {
		this.application = application;
	}

	public Map<String, CustomProperty> getApi() {
		return api;
	}

	public void setApi(Map<String, CustomProperty> api) {
		this.api = api;
	}
	
	public enum Type {
		api("API"), 
		user("User"), 
		organization("Organization"), 
		application("Application");
		
		public String niceName;

		Type(String niceName) {
			this.niceName = niceName;
		}
	}
}
