package com.axway.apim.api.apiSpecification.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.apiSpecification.filter.BaseAPISpecificationFIlter.FilterConfig;
import com.axway.apim.api.model.APISpecificationFilter;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.Paths;

public class OpenAPI3SpecificationFilter {
	
	static Logger LOG = LoggerFactory.getLogger(OpenAPI3SpecificationFilter.class);
	
	public static void filter(OpenAPI openAPI, APISpecificationFilter filterConfig) {
		Paths paths = openAPI.getPaths();
		FilterConfig filter = new FilterConfig(filterConfig);
		// If includes are given - Remove all others not defined as included
		List<String> toBeRemoved = new ArrayList<String>();
		List<String> pathsToBeRemoved = new ArrayList<String>();
		// Iterate over the API specification and create a list of all paths 
		// that must to be removed because they were not configured as included
		Iterator<String> it = paths.keySet().iterator();
		while(it.hasNext()) {
			boolean removePath = true;
			String specPath = it.next();
			PathItem operations = paths.get(specPath);
			Iterator<HttpMethod> it2 = operations.readOperationsMap().keySet().iterator();
			while(it2.hasNext()) {
				HttpMethod next = it2.next();
				String httpMethod = next.toString().toLowerCase();
				if(filter.isExcluded(specPath, httpMethod) || !filter.isIncluded(specPath, httpMethod)) {
					toBeRemoved.add(specPath+":"+httpMethod);
				} else {
					removePath = false;
				}
			}
			if(removePath) {
				pathsToBeRemoved.add(specPath);
			}
		}
		for(String pathAndVerb : toBeRemoved) {
			String excludePath = pathAndVerb.split(":")[0];
			String excludeVerb = pathAndVerb.split(":")[1];
			PathItem pathItem = paths.get(excludePath);
			if(pathsToBeRemoved.contains(excludePath)) {
				paths.remove(excludePath);
				continue;
			}
			// Remote operation from path
			switch(excludeVerb){
			case "get":
				pathItem.setGet(null);
				break;
			case "put":
				pathItem.setPut(null);
				break;
			case "post":
				pathItem.setPost(null);
				break;
			case "delete":
				pathItem.setDelete(null);
				break;
			case "patch":
				pathItem.setPatch(null);
				break;
			case "head":
				pathItem.setHead(null);
				break;
			}
		}
		LOG.info("API-Specification successfully filtered.");
	}
}
