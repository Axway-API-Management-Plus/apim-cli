package com.axway.apim.swagger.api.properties.organization;

public class Organization {
	
	private String id;
	
	private String name;
	
	private String description;
	
	private String email;
	
	private String image;
	
	private String restricted;
	
	private String virtualHost;
	
	private String phone;
	
	private String enabled;
	
	private String development;
	
	private String dn;
	
	private String createdOn;
	
	private String startTrialDate;
	
	private String endTrialDate;
	
	private String trialDuration;
	
	private String isTrial;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getRestricted() {
		return restricted;
	}

	public void setRestricted(String restricted) {
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

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public String getDevelopment() {
		return development;
	}

	public void setDevelopment(String development) {
		this.development = development;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
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
	public String toString() {
		return "'" + name + "'";
	}
}
