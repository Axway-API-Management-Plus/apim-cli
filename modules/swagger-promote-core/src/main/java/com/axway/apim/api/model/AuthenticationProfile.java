package com.axway.apim.api.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.axway.apim.adapter.APIManagerAdapter;

public class AuthenticationProfile {

	private String name;

	private boolean isDefault;

	private Map<String, Object> parameters;
	
	private AuthType type;

	public AuthenticationProfile() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
	
	public Map<String, Object> getParameters() {
		if(parameters==null) return new HashMap<String, Object>();
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public AuthType getType() {
		return type;
	}

	public void setType(AuthType type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other instanceof AuthenticationProfile) {
			AuthenticationProfile authenticationProfile = (AuthenticationProfile) other;
			Map<String, Object> otherParameters = authenticationProfile.getParameters();
			Map<String, Object> thisParameters = this.getParameters();
			otherParameters.remove("_id_");
			thisParameters.remove("_id_");
			if(APIManagerAdapter.hasAPIManagerVersion("7.7 SP1") || APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP5")) {
				// Password no longer exposed by API-Manager REST-API - Can't use it anymore to compare the state
				otherParameters.remove("password");
				thisParameters.remove("password");
			}

			boolean rc = StringUtils.equals(authenticationProfile.getName(), this.getName())
					&& authenticationProfile.getIsDefault() == this.getIsDefault() 
					&& StringUtils.equals(authenticationProfile.getType().name(),this.getType().name())
					&& otherParameters.equals(thisParameters);
			return rc;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		String parametersString = this.getParameters().toString();
		if(type.equals(AuthType.ssl)) {
			String pfx = (String)parameters.get("pfx");
			if(pfx.length()>50) pfx = pfx.substring(0, 49) + "...";
			parametersString = "{trustAll="+this.getParameters().get("trustAll")+", password=********, pfx="+pfx+"}";
		}
		return "AuthenticationProfile [name=" + name + ", isDefault=" + isDefault + ", parameters=" + parametersString
				+ ", type=" + type + "]";
	}
}
