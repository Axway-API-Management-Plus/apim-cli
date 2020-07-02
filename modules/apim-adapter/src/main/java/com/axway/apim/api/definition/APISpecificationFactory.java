package com.axway.apim.api.definition; 

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;

public class APISpecificationFactory {
	
	static Logger LOG = LoggerFactory.getLogger(APISpecificationFactory.class);
	
	private static ArrayList<Class<?>> specificationTypes = new ArrayList<Class<?>>() {
		private static final long serialVersionUID = 1L;

	{
	    add(Swagger2xSpecification.class);
	    add(Swagger1xSpecification.class);
	    add(OAS3xSpecification.class);
	    add(WSDLSpecification.class);
	}};
	
	public static APISpecification getAPISpecification(byte[] apiSpecificationContent, String apiDefinitionFile, String backendBasepath) throws AppException {
		return getAPISpecification(apiSpecificationContent, apiDefinitionFile, backendBasepath, true);
	}
	
	public static APISpecification getAPISpecification(byte[] apiSpecificationContent, String apiDefinitionFile, String backendBasepath, boolean failOnError) throws AppException {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Handle API-Specification: '" + getContentStart(apiSpecificationContent) + "...', apiDefinitionFile: '"+apiDefinitionFile+"'");	
		}
		for(Class clazz : specificationTypes) {
			try {
	            Class<?>[] argTypes = {byte[].class, String.class};
	            Constructor<?> constructor;
				
				constructor = clazz.getDeclaredConstructor(argTypes);
	            Object[] arguments = {apiSpecificationContent, backendBasepath};
				APISpecification spec = (APISpecification) constructor.newInstance(arguments);
				spec.setApiSpecificationFile(apiDefinitionFile);
				if(!spec.configure()) {
					if(ErrorState.getInstance().hasError()) break;
					continue;			
				} else {
					return spec;
				}
			} catch (Exception e) {
				if(LOG.isDebugEnabled()) {
					LOG.error("Can't handle API specification with class: " + clazz.getName(), e);
				}
			}
		}
		if(!failOnError) {
			LOG.error("Can't handle API specification: '" + getContentStart(apiSpecificationContent) + "'");
			return new UnknownAPISpecification(apiSpecificationContent, backendBasepath);
		}
		LOG.error("Can't handle API specification: '" + getContentStart(apiSpecificationContent) + "'");
		throw new AppException("Can't handle API specification. No suiteable API-Specification implementation available.", ErrorCode.UNXPECTED_ERROR);
	}
	
	private static String getContentStart(byte[] apiSpecificationContent) {
		try {
			if(apiSpecificationContent == null) return "API-Specificaion is null";
			return (apiSpecificationContent.length<200) ? new String(apiSpecificationContent, 0, apiSpecificationContent.length) : new String(apiSpecificationContent, 0, 200) + "...";
		} catch (Exception e) {
			return "Cannot get content from API-Specification. " + e.getMessage();
		}		
	}
}
