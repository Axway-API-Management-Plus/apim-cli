package com.axway.apim.api.model;

import java.util.ArrayList;
import java.util.List;

public class APISpecIncludeExcludeFilter {
	
	private List<String> include = new ArrayList<String>();
	
	private List<String> exclude = new ArrayList<String>();

	public List<String> getInclude() {
		return include;
	}

	public void setInclude(List<String> include) {
		this.include = include;
	}

	public List<String> getExclude() {
		return exclude;
	}

	public void setExclude(List<String> exclude) {
		this.exclude = exclude;
	}
	
	
	public void addExclude(String pathAndVerb) {
		if(this.exclude==null) {
			this.exclude = new ArrayList<String>();
		}
		this.exclude.add(pathAndVerb);
	}
	
	public void addInclude(String pathAndVerb) {
		if(this.include==null) {
			this.include = new ArrayList<String>();
		}
		this.include.add(pathAndVerb);
	}
}
