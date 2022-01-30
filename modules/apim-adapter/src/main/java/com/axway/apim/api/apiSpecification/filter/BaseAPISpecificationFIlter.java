package com.axway.apim.api.apiSpecification.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axway.apim.api.model.APISpecificationFilter;

public class BaseAPISpecificationFIlter {
	
	protected static class FilterConfig {
		
		/**
		 * Maps a path to a list of included HTTP-Verbs
		 */
		private Map<String, List<String>> includedPaths = new HashMap<String, List<String>>();
		
		/**
		 * Maps a path to a list of included HTTP-Verbs
		 */
		private Map<String, List<String>> excludedPaths = new HashMap<String, List<String>>();
		
		public FilterConfig(APISpecificationFilter filterConfig) {
			super();
			for(String pathAndVerb : filterConfig.getPaths().getInclude()) {
				add(pathAndVerb, includedPaths);
			}
			for(String pathAndVerb : filterConfig.getPaths().getExclude()) {
				add(pathAndVerb, excludedPaths);
			}
		}
		
		void add(String pathAndVerb, Map<String, List<String>> pathsAndVerb) {
			String path = pathAndVerb.split(":")[0];
			String verb = pathAndVerb.split(":")[1];
			List<String> verbs = pathsAndVerb.get(path);
			if(verbs == null) {
				verbs = new ArrayList<String>();
			}
			verbs.add(verb.toLowerCase());
			pathsAndVerb.put(path, verbs);
		}
		
		boolean isExcluded(String path, String verb) {
			if(excludedPaths.isEmpty()) return false;
			return isIncludedExcluded(path, verb, excludedPaths);
		}
		
		boolean isIncluded(String path, String verb) {
			if(includedPaths.isEmpty()) return true;
			return isIncludedExcluded(path, verb, includedPaths);
		}
		
		private boolean isIncludedExcluded(String path, String verb, Map<String, List<String>> paths) {
			// Check if the path is directly configured
			List<String> operations = paths.get(path);
			if(operations == null) {
				// If not, check for a wildcard
				if(paths.containsKey("*")) {
					operations = paths.get("*");
				} else {
					return false;
				}
			}
			if(operations.contains(verb.toLowerCase()) || operations.contains("*")) return true;
			return false;
		}
	}

}
