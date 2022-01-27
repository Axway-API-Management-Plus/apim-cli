package com.axway.apim.api.specification;

public class DesiredAPISpecification {
	
	/**
	 * The resource path to the API-Specification
	 */
	private String resource;
	
	private APISpecificationFilter filter;

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public APISpecificationFilter getFilter() {
		return filter;
	}

	public void setFilter(APISpecificationFilter filter) {
		this.filter = filter;
	}
}
