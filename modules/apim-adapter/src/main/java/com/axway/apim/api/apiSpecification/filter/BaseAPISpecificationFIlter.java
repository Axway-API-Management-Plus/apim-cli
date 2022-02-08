package com.axway.apim.api.apiSpecification.filter;

import java.util.List;

import com.axway.apim.api.model.APISpecIncludeExcludeFilter;
import com.axway.apim.api.model.APISpecificationFilter;

public class BaseAPISpecificationFIlter {
	
	protected static class FilterConfig {
		
		APISpecificationFilter filterConfig;
		
		/**
		 * Maps a path to a list of included HTTP-Verbs
		 */
		//private Map<String, List<String>> includedPaths = new HashMap<String, List<String>>();
		
		/**
		 * Maps a path to a list of included HTTP-Verbs
		 */
		//private Map<String, List<String>> excludedPaths = new HashMap<String, List<String>>();
		
		//private List<String> includedTags = new ArrayList<String>();
		
		//private List<String> excludedTags = new ArrayList<String>();
		
		public FilterConfig(APISpecificationFilter filterConfig) {
			super();
/*			for(String pathAndVerb : filterConfig.getInclude().getPaths()) {
				add(pathAndVerb, includedPaths);
			}
			for(String pathAndVerb : filterConfig.getExclude().getPaths()) {
				add(pathAndVerb, excludedPaths);
			}*/
			/*
			excludedTags = filterConfig.getExclude().getTags();
			includedTags = filterConfig.getInclude().getTags();*/
			
			this.filterConfig = filterConfig;
		}
		
		/*void add(String pathAndVerb, Map<String, List<String>> pathsAndVerb) {
			String path = pathAndVerb.split(":")[0];
			String verb = pathAndVerb.split(":")[1];
			List<String> verbs = pathsAndVerb.get(path);
			if(verbs == null) {
				verbs = new ArrayList<String>();
			}
			verbs.add(verb.toLowerCase());
			pathsAndVerb.put(path, verbs);
		}*/
		
		/*boolean isIncluded(String path, String verb, List<String> tags) {
			// If included is empty, it should be included by default
			if(filterConfig.getInclude().isEmpty()) return true;
			// Iterate over each include configuration
			for(APISpecIncludeExcludeFilter filter : filterConfig.getInclude()) {
				// Check if the HTTP-Path is configured as included
				return (filter(path, verb, filter) 
						&& isTagsIncluded(tags, filter.getTags())); 
			}
			return false;
		}
		
		boolean isExcluded(String path, String verb, List<String> tags) {
			if(filterConfig.getExclude().isEmpty()) return false;
			for(APISpecIncludeExcludeFilter filter : filterConfig.getExclude()) {
				if((filter(path, verb, filter) 
						&& isTagsIncluded(tags, filter.getTags()))) {
					return true;
				}
			}
			return false;
		}*/

		public boolean filter(String path, String verb, List<String> tags) {
			// Nothing to filter at all
			if(filterConfig.getExclude().isEmpty() && filterConfig.getInclude().isEmpty()) return false;
			// Check if there is any specific EXCLUDE filter is excluding the operation
			for(APISpecIncludeExcludeFilter filter : filterConfig.getExclude()) {
				if(filter.filter(path, verb, tags, false, true) == true ) {
					// Must be filtered in any case, as it is specific, even it might be included as exclude overwrite includes
					return true;
				}
			}
			// Check if there is any specific INCLUDE filter is including the operation
			for(APISpecIncludeExcludeFilter filter : filterConfig.getInclude()) {
				if(filter.filter(path, verb, tags, false, true)) {
					// Should be included as it is given with a specific filter
					return false;
				}
			}
			// Now, check for wildcards, which have less priority, than the specific filters
			for(APISpecIncludeExcludeFilter filter : filterConfig.getExclude()) {
				if(filter.filter(path, verb, tags, true, false) == true) {
					// Should be filtered
					return true;
				}
			}
			// Check if there is any wildcard configured as include
			for(APISpecIncludeExcludeFilter filter : filterConfig.getInclude()) {
				if(filter.filter(path, verb, tags, true, false) == true) {
					return false;
				}
			}
			
			/*if(filter.getPaths().isEmpty()) return true;
			List<String> httpMethods = filter.getHttpMethods(path);
			// Check if the path is configured directly and if the requested verb is configured
			if(httpMethods!=null) {
				if(httpMethods.contains(verb.toLowerCase()) || httpMethods.contains("*")) return true;
			}*/
			if(!filterConfig.getInclude().isEmpty()) {
				// If there is at least one include - Filter it anyway
				return true;
			} else {
				return false; // Otherwise dont filter
			}
			/*
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
			return false;*/
		}
		
		private boolean isPathExcluded(List<String> httpMethods, String httpMethod) {
			if(httpMethods == null) return false; // If path is not configured as excluded, it should not be removed/filtered
			return containsHttpMethod(httpMethods, httpMethod);
		}
		
		private boolean isIncluded(List<String> httpMethods, String httpMethod) {
			if(httpMethods == null) return true; // If not configured as included, it should be included
			return containsHttpMethod(httpMethods, httpMethod);
		}
		
		private boolean containsHttpMethod(List<String> httpMethods, String httpMethod) {
			return httpMethods.contains(httpMethod.toLowerCase()) || httpMethods.contains("*"); 
		}
		
		private boolean containsTags(List<String> includedTags, List<String> tags) {
			if(includedTags.isEmpty()) return true;
			for(String tag : tags) {
				if(includedTags.contains(tag)) return true;
			}
			return false;
		}
	}

}
