package com.axway.apim.swagger.api.properties.securityprofiles;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class SecurityDevice {
	String name;
	
	String type;
	
	String order;
	
	Map<String, Object> properties;

	public SecurityDevice() {
		super();
		this.properties = new LinkedHashMap<String, Object>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof SecurityDevice) {
			SecurityDevice otherSecurityDevice = (SecurityDevice)other;
			if(!StringUtils.equals(otherSecurityDevice.getName(), this.getName())) return false;
			if(!StringUtils.equals(otherSecurityDevice.getType(), this.getType())) return false;
			if(!StringUtils.equals(otherSecurityDevice.getOrder(), this.getOrder())) return false;
			if(!otherSecurityDevice.getProperties().equals(this.getProperties())) return false;
		}
		return true;
	}
}
