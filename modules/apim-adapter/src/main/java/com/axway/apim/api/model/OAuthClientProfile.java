package com.axway.apim.api.model;

import org.apache.commons.lang3.StringUtils;

public class OAuthClientProfile {
	
	private String id;
	
	private String name;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof OAuthClientProfile) {
			return StringUtils.equals(((OAuthClientProfile)other).getName(), this.getName());
		}
		return false;
	}
}
