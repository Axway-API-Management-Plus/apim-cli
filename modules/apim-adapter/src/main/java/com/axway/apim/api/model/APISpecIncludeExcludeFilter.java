package com.axway.apim.api.model;

import java.util.ArrayList;
import java.util.List;

public class APISpecIncludeExcludeFilter {
	
	private List<String> paths = new ArrayList<String>();
	
	private List<String> tags = new ArrayList<String>();
	
	public List<String> getPaths() {
		return paths;
	}

	public void setPaths(List<String> paths) {
		this.paths = paths;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	/**
	 * This method is used for tests only
	 * @param pathAndVerb the path and verb
	 */
	public void addPath(String pathAndVerb) {
		if(this.paths==null) {
			this.paths = new ArrayList<String>();
		}
		this.paths.add(pathAndVerb);
	}
	
	/**
	 * This method is used for tests only
	 * @param tag the tag to include or exclude
	 */
	public void addTag(String tag) {
		if(this.tags==null) {
			this.tags = new ArrayList<String>();
		}
		this.tags.add(tag);
	}
}
