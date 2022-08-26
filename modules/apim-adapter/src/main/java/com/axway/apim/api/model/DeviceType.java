package com.axway.apim.api.model;

public enum DeviceType {
	apiKey ("API Key", new String[] {"api-key", "apikey"}),
	basic ("HTTP Basic", new String[] {"httpbasic", "http-basic"}), 
	oauth ("OAuth", new String[] {"oauth"}), 
	oauthExternal ("OAuth (External)", new String[] {"oauthext", "oauth-ext", "oauth-external"}), 
	authPolicy ("Policy", new String[] {"policy"}), 
	passThrough ("Pass Through", new String[] {"passthrough", "none", "pass-through"}), 
	awsHeader ("AWS Sign Header", new String[] {"aws", "aws-header", "aws-sign-header"}), 
	awsQuery ("AWS Sign Query", new String[] {"aws", "aws-query", "aws-sign-query"}), 
	twoWaySSL ("Two-Way SSL", new String[] {"ssl", "two-way-ssl", "mutual", "mutualssl"});
	
	private final String name;
	private final String[] alternativenames;
	
	DeviceType(String name, String[] alternativeNames) {
		this.name = name;
		this.alternativenames = alternativeNames;
	}
	
	public String getName() {
		return name;
	}
	public String[] getAlternativeNames() {
		return alternativenames;
	}
}
