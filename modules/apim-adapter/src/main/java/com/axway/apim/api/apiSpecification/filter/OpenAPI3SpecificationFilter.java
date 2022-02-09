package com.axway.apim.api.apiSpecification.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.api.apiSpecification.filter.BaseAPISpecificationFIlter.FilterConfig;
import com.axway.apim.api.model.APISpecificationFilter;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
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
		List<String> modelsToBeRemoved = new ArrayList<String>();
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
				Operation operation = getOperation4HttpMethod(operations, httpMethod);
				List<String> tags = operation.getTags();
				// If path OR tag is excluded, if must be removed no matter if configured as included
				if(filter.filterOperations(specPath, httpMethod, tags)) {
					toBeRemoved.add(specPath+":"+httpMethod);
					LOG.debug("Removed: " + specPath + ":"+httpMethod + " and tags: " + tags);
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
		if(openAPI.getComponents()!=null && openAPI.getComponents().getSchemas()!=null) {
			Iterator<String> schemaNamesIt = openAPI.getComponents().getSchemas().keySet().iterator();
			while(schemaNamesIt.hasNext()) {
				String modelName = schemaNamesIt.next();
				if(filter.filterModel(modelName)) {
					modelsToBeRemoved.add(modelName);
					LOG.debug("Removed: " + modelName + " from API-Specification.");
				}
			}
			for(String modelName : modelsToBeRemoved) {
				openAPI.getComponents().getSchemas().remove(modelName);
			}
		}
		LOG.info("API-Specification successfully filtered.");
	}
	
	private static Operation getOperation4HttpMethod(PathItem operations, String httpMethod) {
		switch(httpMethod){
		case "get":
			return operations.getGet();
		case "put":
			return operations.getPut();
		case "post":
			return operations.getPost();
		case "delete":
			return operations.getDelete();
		case "patch":
			return operations.getPatch();
		case "head":
			return operations.getHead();
		}
		return null;
	}
}
