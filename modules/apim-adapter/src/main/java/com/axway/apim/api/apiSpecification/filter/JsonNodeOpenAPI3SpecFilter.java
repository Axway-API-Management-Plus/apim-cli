package com.axway.apim.api.apiSpecification.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.apiSpecification.filter.BaseAPISpecificationFIlter.FilterConfig;
import com.axway.apim.api.model.APISpecificationFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonNodeOpenAPI3SpecFilter  {
	
	static Logger LOG = LoggerFactory.getLogger(JsonNodeOpenAPI3SpecFilter.class);

	public static void filter(JsonNode openAPISpec, APISpecificationFilter filterConfig) {
		JsonNode paths = openAPISpec.get("paths");
		FilterConfig filter = new FilterConfig(filterConfig);
		// If includes are given - Remove all others not defined as included
		List<String> toBeRemoved = new ArrayList<String>();
		// Iterate over the API specification and create a list of all paths 
		// that must to be removed because they were not configured as included
		Iterator<String> it = paths.fieldNames();
		while(it.hasNext()) {
			String specPath = it.next();
			JsonNode operations = paths.get(specPath);
			Iterator<String> it2 = operations.fieldNames();
			while(it2.hasNext()) {
				String httpMethod = it2.next();
				JsonNode operation = operations.get(httpMethod);
				ArrayNode tags = (ArrayNode)operation.get("tags");
				if(filter.filter(specPath, httpMethod, toList(tags))) {
					toBeRemoved.add(specPath+":"+httpMethod);
					LOG.info("Removed excluded: " + specPath + ":"+httpMethod + " and tags: " + tags);
				}
				
				
				// If path OR tag is excluded, if must be removed no matter if configured as included
				/*if(filter.isExcluded(specPath, httpMethod) || filter.isTagsExcluded(toList(tags))) {
					toBeRemoved.add(specPath+":"+httpMethod);
					LOG.info("Removed excluded: " + specPath + ":"+httpMethod + " and tags: " + tags);
				// If the path AND tags are not included in either path- or tag-config it must be removed
				} else if(!filter.isPathOrTagIncluded(specPath, httpMethod, toList(tags))) {
					toBeRemoved.add(specPath+":"+httpMethod);
					LOG.info("Removed not included: " + specPath + ":"+httpMethod + " and tags: " + tags);
				}*/
			}
		}
		for(String pathAndVerb : toBeRemoved) {
			String excludePath = pathAndVerb.split(":")[0];
			String excludeVerb = pathAndVerb.split(":")[1];
			JsonNode path = paths.get(excludePath);
			// Remote operation from path
			((ObjectNode)path).remove(excludeVerb);
			// Remove the entire path, if no more remaining operations
			if(path.size()==0) {
				((ObjectNode)paths).remove(excludePath);
			}
		}
		LOG.info("API-Specification successfully filtered.");
	}
	
	private static List<String> toList(ArrayNode arrayNode) {
		List<String> list = new ArrayList<String>();
		for(JsonNode node : arrayNode) {
			list.add(node.asText());
		}
		return list;
	}
}
