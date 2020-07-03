package com.axway.apim.api.model;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("OrganizationFilter")
public class Organization extends AbstractEntity {
	
	private String email;
	
	@JsonIgnore
	private String image;
	
	private boolean restricted;
	
	private String virtualHost;
	
	private String phone;
	
	private boolean enabled;
	
	private boolean development;
	
	private String dn;
	
	private Long createdOn;
	
	private String startTrialDate;
	
	private String endTrialDate;
	
	private String trialDuration;
	
	private String isTrial;
	
	public Organization() {
		super();
	}
	
	public Organization(String name) {
		super();
		setName(name);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public boolean isRestricted() {
		return restricted;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	public String getVirtualHost() {
		return virtualHost;
	}

	public void setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean getDevelopment() {
		return development;
	}

	public void setDevelopment(boolean development) {
		this.development = development;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public Long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Long createdOn) {
		this.createdOn = createdOn;
	}

	public String getStartTrialDate() {
		return startTrialDate;
	}

	public void setStartTrialDate(String startTrialDate) {
		this.startTrialDate = startTrialDate;
	}

	public String getEndTrialDate() {
		return endTrialDate;
	}

	public void setEndTrialDate(String endTrialDate) {
		this.endTrialDate = endTrialDate;
	}

	public String getTrialDuration() {
		return trialDuration;
	}

	public void setTrialDuration(String trialDuration) {
		this.trialDuration = trialDuration;
	}

	public String getIsTrial() {
		return isTrial;
	}

	public void setIsTrial(String isTrial) {
		this.isTrial = isTrial;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof Organization) {
			return StringUtils.equals(((Organization)other).getName(), this.getName());
		}
		return false;
	}

	@Override
	public String toString() {
		return "'" + getName() + "'";
	}
	
	public static class Builder {
		String name;
		String id;
		
		public Organization build() {
			Organization org = new Organization();
			org.setName(name);
			org.setId(id);
			return org;
		}
		
		public Builder hasName(String name) {
			this.name = name;
			return this;
		}
		
		public Builder hasId(String id) {
			this.id = id;
			return this;
		}
	}
}
