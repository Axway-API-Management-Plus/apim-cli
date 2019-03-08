package com.axway.apim.swagger.api.properties.securityprofiles;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class SecurityProfile {
	
	String name;
	
	String isDefault;
	
	List<SecurityDevice> devices;


	public SecurityProfile() {
		super();
		this.devices = new ArrayList<SecurityDevice>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}

	public List<SecurityDevice> getDevices() {
		return devices;
	}

	public void setDevices(List<SecurityDevice> devices) {
		this.devices = devices;
	}

	@Override
	public String toString() {
		return "SecurityProfile [" + devices + "]";
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof SecurityProfile) {
			SecurityProfile securityProfile = (SecurityProfile)other;
			
			return
					StringUtils.equals(securityProfile.getName(), this.getName()) &&
					StringUtils.equals(securityProfile.getIsDefault(), this.getIsDefault()) &&
					securityProfile.getDevices().equals(this.getDevices());
		} else {
			return false;
		}
	}
}
