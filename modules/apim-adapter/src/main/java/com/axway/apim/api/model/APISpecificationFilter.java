package com.axway.apim.api.model;

public class APISpecificationFilter {
	
	private APISpecIncludeExcludeFilter paths = new APISpecIncludeExcludeFilter();

	public APISpecIncludeExcludeFilter getPaths() {
		return paths;
	}

	public void setPaths(APISpecIncludeExcludeFilter paths) {
		this.paths = paths;
	}
}
