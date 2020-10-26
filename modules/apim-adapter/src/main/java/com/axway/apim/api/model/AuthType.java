package com.axway.apim.api.model;

public enum AuthType {

	none ("None", new String[] {"none"}), 
	http_basic ("HTTP Basic", new String[] {"http-basic", "httpbasic", "basic"}), 
	http_digest("HTTP Digest", new String[] {"http-digest", "httpdigest", "basic", "digest"}), 
	apiKey("API-Key", new String[] {"apikey", "api-key", "key"}), 
	oauth("OAuth", new String[] {"oauth", "o-auth"}), 
	ssl("SSL", new String[] {"ssl", "mutual", "mutualssl", "mutualssl"});
	
	private final String name;
	private final String[] alternativenames;
	
	private AuthType(String name, String[] alternativeNames) {
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