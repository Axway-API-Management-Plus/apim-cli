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
		
		private List<String> includedTags = new ArrayList<String>();
		
		private List<String> excludedTags = new ArrayList<String>();
		
		public FilterConfig(APISpecificationFilter filterConfig) {
			super();
			for(String pathAndVerb : filterConfig.getInclude().getPaths()) {
				add(pathAndVerb, includedPaths);
			}
			for(String pathAndVerb : filterConfig.getExclude().getPaths()) {
				add(pathAndVerb, excludedPaths);
			}
			excludedTags = filterConfig.getExclude().getTags();
			includedTags = filterConfig.getInclude().getTags();
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
		
		boolean isPathOrTagIncluded(String path, String verb, List<String> tags) {
			// If nothing is configured, it should be included by default
			if(includedPaths.isEmpty() && includedTags.isEmpty()) return true;
			// Check if the path or the tag is included
			return (isIncludedExcluded(path, verb, includedPaths) ||  isTagsIncluded(tags)) ;
		}
		
		boolean isExcluded(String path, String verb) {
			if(excludedPaths.isEmpty()) return false;
			return isIncludedExcluded(path, verb, excludedPaths);
		}
		
		private boolean isTagsIncluded(List<String> tags) {
			for(String tag : tags) {
				if(includedTags.contains(tag)) return true;
			}
			return false;
		}
		
		boolean isTagsExcluded(List<String> tags) {
			if(excludedTags.isEmpty()) return false;
			for(String tag : tags) {
				if(excludedTags.contains(tag)) return true;
			}
			return false;
		}

		private boolean isIncludedExcluded(String path, String verb, Map<String, List<String>> paths) {
			List<String> operations = null;
			// Check if the path is configured directly and if the requested verb is configured
			if(paths.containsKey(path) && (paths.get(path).contains(verb) || paths.get(path).contains("*"))) {
				operations = paths.get(path);
			// If not, check there is a wildcard configured
			} else if(paths.containsKey("*")) {
				operations = paths.get("*");
			} else {
			// Otherwise no rule is declared
				return false;
			}
			if(operations.contains(verb.toLowerCase()) || operations.contains("*")) return true;
			return false;
		}
	}

}
