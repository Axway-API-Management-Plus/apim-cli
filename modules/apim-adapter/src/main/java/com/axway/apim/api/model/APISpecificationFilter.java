package com.axway.apim.api.model;

public class APISpecificationFilter {
	
	private APISpecIncludeExcludeFilter include = new APISpecIncludeExcludeFilter();
	private APISpecIncludeExcludeFilter exclude = new APISpecIncludeExcludeFilter();
	public APISpecIncludeExcludeFilter getInclude() {
		return include;
	}
	public void setInclude(APISpecIncludeExcludeFilter include) {
		this.include = include;
	}
	public APISpecIncludeExcludeFilter getExclude() {
		return exclude;
	}
	public void setExclude(APISpecIncludeExcludeFilter exclude) {
		this.exclude = exclude;
	}
}
