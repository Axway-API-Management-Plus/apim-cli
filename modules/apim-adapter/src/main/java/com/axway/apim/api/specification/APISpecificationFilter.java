package com.axway.apim.api.specification;

import java.util.Map;

public class APISpecificationFilter {
	
	private Map<String, String> include;
	
	private Map<String, String> exclude;

	public Map<String, String> getInclude() {
		return include;
	}

	public void setInclude(Map<String, String> include) {
		this.include = include;
	}

	public Map<String, String> getExclude() {
		return exclude;
	}

	public void setExclude(Map<String, String> exclude) {
		this.exclude = exclude;
	}
}
