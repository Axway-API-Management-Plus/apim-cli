package com.axway.apim.swagger.api.properties.quota;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.axway.apim.swagger.api.properties.securityprofiles.SecurityProfile;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class APIQuota {
	
	String id;
	
	String type;
	
	String name;
	
	String description;
	
	String system;
	
	List<QuotaRestriction> restrictions;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<QuotaRestriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<QuotaRestriction> restrictions) {
		this.restrictions = restrictions;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof APIQuota) {
			APIQuota apiQuota = (APIQuota)other;
			
			return
					apiQuota.getRestrictions().equals(this.getRestrictions());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "APIQuota [id=" + id + ", type=" + type + ", restrictions=" + restrictions + "]";
	}
}
