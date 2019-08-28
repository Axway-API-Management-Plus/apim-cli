package com.axway.apim.swagger.api.properties.authenticationProfiles;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.axway.apim.lib.ErrorCode;
import com.axway.apim.lib.ErrorState;

public class AuthenticationProfile {

	private String name;

	private boolean isDefault;

	private Properties parameters;
	
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
	
	public Properties getParameters() {
		if(parameters==null) return new Properties();
		return parameters;
	}

	public void setParameters(Properties parameters) {
		this.parameters = parameters;
	}

	public AuthType getType() {
		return type;
	}

	public void setType(AuthType type) {
		Pattern pattern = Pattern.compile("^("+AuthType.values()+")$");
		Matcher matcher = pattern.matcher(type.name());
		if(!matcher.matches()) {
			ErrorState.getInstance().setError("Invalid authenticationProfile. Type: '"+type+"' must be one of the following: "+AuthType.values(), ErrorCode.CANT_READ_CONFIG_FILE, false);
			throw new RuntimeException("Invalid authenticationProfile. Type type: '"+type+"' must be one of the following: "+AuthType.values());
		}
		this.type = type;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other instanceof AuthenticationProfile) {
			AuthenticationProfile authenticationProfile = (AuthenticationProfile) other;

			return StringUtils.equals(authenticationProfile.getName(), this.getName())
					&& authenticationProfile.getIsDefault() == this.getIsDefault() 
					&& StringUtils.equals(authenticationProfile.getType().name(),this.getType().name())
					&& authenticationProfile.getParameters().equals(this.getParameters());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		String parametersString = this.getParameters().toString();
		if(type.equals(AuthType.ssl)) {
			String pfx = parameters.getProperty("pfx");
			if(pfx.length()>50) pfx = pfx.substring(0, 49) + "...";
			parametersString = "{trustAll="+this.getParameters().getProperty("trustAll")+", password=********, pfx="+pfx+"}";
		}
		return "AuthenticationProfile [name=" + name + ", isDefault=" + isDefault + ", parameters=" + parametersString
				+ ", type=" + type + "]";
	}
}
