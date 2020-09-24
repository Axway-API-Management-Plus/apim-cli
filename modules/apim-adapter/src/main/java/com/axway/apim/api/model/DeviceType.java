package com.axway.apim.api.model;

public enum DeviceType {
	apiKey ("API Key"), 
	basic ("HTTP Basic"), 
	oauth ("OAuth"), 
	oauthExternal ("OAuth (External)"), 
	authPolicy ("Policy"), 
	passThrough ("Pass Through"), 
	awsHeader ("AWS Sign Header"), 
	awsQuery ("AWS Sign Query"), 
	twoWaySSL ("Two-Way SSL");
	
	private final String name;
	
	private DeviceType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
