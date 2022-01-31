package com.axway.apim.api.model;

import java.util.ArrayList;
import java.util.List;

public class APISpecIncludeExcludeFilter {
	
	private List<String> paths = new ArrayList<String>();
	
	public List<String> getPaths() {
		return paths;
	}

	public void setPaths(List<String> paths) {
		this.paths = paths;
	}

	public void addPath(String pathAndVerb) {
		if(this.paths==null) {
			this.paths = new ArrayList<String>();
		}
		this.paths.add(pathAndVerb);
	}
}
