package com.axway.apim.api.specification.filter;

import java.util.List;

import com.axway.apim.api.model.APISpecIncludeExcludeFilter;
import com.axway.apim.api.model.APISpecificationFilter;

public class BaseAPISpecificationFilter {

    private BaseAPISpecificationFilter() {
        throw new IllegalStateException("Utility class");
    }

    protected static class FilterConfig {

        APISpecificationFilter filterConfigFilter;

        public FilterConfig(APISpecificationFilter filterConfigFilter) {
            super();
            this.filterConfigFilter = filterConfigFilter;
        }

        public boolean filterOperations(String path, String verb, List<String> tags) {
            // Nothing to filter at all
            if (filterConfigFilter.getExclude().isEmpty() && filterConfigFilter.getInclude().isEmpty())
                return false;
            // Check if there is any SPECIFIC EXCLUDE filter is excluding the operation
            for (APISpecIncludeExcludeFilter filter : filterConfigFilter.getExclude()) {
                if (filter.filter(path, verb, tags, false, true)) {
                    // Must be filtered in any case, as it is specific, even it might be included as
                    // exclude overwrite includes
                    return true;
                }
            }
            // Check if there is any SPECIFIC INCLUDE filter is including the operation
            for (APISpecIncludeExcludeFilter filter : filterConfigFilter.getInclude()) {
                if (filter.filter(path, verb, tags, false, true)) {
                    // Should be included as it is given with a specific filter
                    return false;
                }
            }
            // Now, check for WILDCARD EXCLUDES, which have less priority, than the specific
            // filters
            for (APISpecIncludeExcludeFilter filter : filterConfigFilter.getExclude()) {
                if (filter.filter(path, verb, tags, true, false)) {
                    // Should be filtered
                    return true;
                }
            }
            // Check if there is any WILDCARD INCLUDE configured
            for (APISpecIncludeExcludeFilter filter : filterConfigFilter.getInclude()) {
                if (filter.filter(path, verb, tags, true, false)) {
                    return false;
                }
            }
            // If there is at least one include - Filter it anyway
            // Otherwise dont filter
            return !filterConfigFilter.getInclude().isEmpty();

        }

        public boolean filterModel(String modelName) {
            for (APISpecIncludeExcludeFilter filter : filterConfigFilter.getExclude()) {
                for (String model2Exclude : filter.getModels()) {
                    if (model2Exclude.equals(modelName)) return true;
                }
            }
            boolean modelIncludeFilterConfigured = false;
            for (APISpecIncludeExcludeFilter filter : filterConfigFilter.getInclude()) {
                for (String model2Include : filter.getModels()) {
                    modelIncludeFilterConfigured = true;
                    if (model2Include.equals(modelName)) {
                        return false;
                    }
                }
            }
            // If at least one model include is defined, then all others must be filtered.
            return modelIncludeFilterConfigured;
        }
    }
}
