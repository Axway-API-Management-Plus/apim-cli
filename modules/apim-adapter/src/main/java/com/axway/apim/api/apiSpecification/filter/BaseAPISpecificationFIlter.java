package com.axway.apim.api.apiSpecification.filter;

import java.util.List;

import com.axway.apim.api.model.APISpecIncludeExcludeFilter;
import com.axway.apim.api.model.APISpecificationFilter;

public class BaseAPISpecificationFIlter {

	protected static class FilterConfig {

		APISpecificationFilter filterConfig;

		public FilterConfig(APISpecificationFilter filterConfig) {
			super();
			this.filterConfig = filterConfig;
		}

		public boolean filterOperations(String path, String verb, List<String> tags) {
			// Nothing to filter at all
			if (filterConfig.getExclude().isEmpty() && filterConfig.getInclude().isEmpty())
				return false;
			// Check if there is any SPECIFIC EXCLUDE filter is excluding the operation
			for (APISpecIncludeExcludeFilter filter : filterConfig.getExclude()) {
				if (filter.filter(path, verb, tags, false, true) == true) {
					// Must be filtered in any case, as it is specific, even it might be included as
					// exclude overwrite includes
					return true;
				}
			}
			// Check if there is any SPECIFIC INCLUDE filter is including the operation
			for (APISpecIncludeExcludeFilter filter : filterConfig.getInclude()) {
				if (filter.filter(path, verb, tags, false, true)) {
					// Should be included as it is given with a specific filter
					return false;
				}
			}
			// Now, check for WILDCARD EXCLUDES, which have less priority, than the specific
			// filters
			for (APISpecIncludeExcludeFilter filter : filterConfig.getExclude()) {
				if (filter.filter(path, verb, tags, true, false) == true) {
					// Should be filtered
					return true;
				}
			}
			// Check if there is any WILDCARD INCLUDE configured
			for (APISpecIncludeExcludeFilter filter : filterConfig.getInclude()) {
				if (filter.filter(path, verb, tags, true, false) == true) {
					return false;
				}
			}

			if (!filterConfig.getInclude().isEmpty()) {
				// If there is at least one include - Filter it anyway
				return true;
			} else {
				return false; // Otherwise dont filter
			}

		}
		
		public boolean filterModel(String modelName) {
			for (APISpecIncludeExcludeFilter filter : filterConfig.getExclude()) {
				for(String model2Exclude : filter.getModels()) {
					if(model2Exclude.equals(modelName)) return true;
				}
			}
			boolean modelIncludeFilterConfigured = false;
			for (APISpecIncludeExcludeFilter filter : filterConfig.getInclude()) {
				for(String model2Include : filter.getModels()) {
					modelIncludeFilterConfigured = true;
					if(model2Include.equals(modelName)) {
						return false;
					}
				}
			}
			// If at least one model include is defined, then all others must be filtered.
			if(modelIncludeFilterConfigured) {
				return true;
			} else {
				return false;
			}
		}
	}

}
