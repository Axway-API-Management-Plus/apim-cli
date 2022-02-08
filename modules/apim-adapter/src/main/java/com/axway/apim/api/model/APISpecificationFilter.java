package com.axway.apim.api.model;

import java.util.ArrayList;
import java.util.List;

public class APISpecificationFilter {
	
	private List<APISpecIncludeExcludeFilter> include = new ArrayList<APISpecIncludeExcludeFilter>();
	private List<APISpecIncludeExcludeFilter> exclude = new ArrayList<APISpecIncludeExcludeFilter>();
	

	public List<APISpecIncludeExcludeFilter> getInclude() {
		return include;
	}
	public void setInclude(List<APISpecIncludeExcludeFilter> include) {
		this.include = include;
	}
	public List<APISpecIncludeExcludeFilter> getExclude() {
		return exclude;
	}
	public void setExclude(List<APISpecIncludeExcludeFilter> exclude) {
		this.exclude = exclude;
	}
	
	public List<APISpecIncludeExcludeFilter> getPathAndTagsExclude() {
		List<APISpecIncludeExcludeFilter> result = new ArrayList<APISpecIncludeExcludeFilter>();
		for(APISpecIncludeExcludeFilter filter : this.include) {
			if(!filter.getPaths().isEmpty() && !filter.getTags().isEmpty()) {
				result.add(filter);
			}
		}
		return result;
	}
	
	public List<APISpecIncludeExcludeFilter> getPathAndTagsInclude() {
		List<APISpecIncludeExcludeFilter> result = new ArrayList<APISpecIncludeExcludeFilter>();
		for(APISpecIncludeExcludeFilter filter : this.include) {
			if(!filter.getPaths().isEmpty() && !filter.getTags().isEmpty()) {
				result.add(filter);
			}
		}
		return result;
	}
	
	/**
	 * This method is used for tests only
	 * @param pathAndVerb the path and verb
	 */
	public void addInclude(String[] pathAndVerbs, String[] tags) {
		APISpecIncludeExcludeFilter filter = new APISpecIncludeExcludeFilter();
		if(pathAndVerbs!=null) filter.addPath(pathAndVerbs);
		if(tags!=null) filter.addTag(tags);
		this.include.add(filter);
	}
	
	/**
	 * This method is used for tests only
	 * @param tag the tag to include or exclude
	 */
	public void addExclude(String[] pathAndVerbs, String[] tags) {
		APISpecIncludeExcludeFilter filter = new APISpecIncludeExcludeFilter();
		if(pathAndVerbs!=null) filter.addPath(pathAndVerbs);
		if(tags!=null) filter.addTag(tags);
		this.exclude.add(filter);
	}
	
}
