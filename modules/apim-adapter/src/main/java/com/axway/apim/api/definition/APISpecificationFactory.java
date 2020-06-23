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
		if(LOG.isDebugEnabled()) {
			String contentStart = new String(apiSpecificationContent, 0, 200);
			LOG.debug("Handle API-Specification: '" + contentStart + "...', apiDefinitionFile: '"+apiDefinitionFile+"'");	
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
				e.printStackTrace();
			}
		}
		throw new AppException("Can't handle API specification. No suiteable specification implementation found.", ErrorCode.UNXPECTED_ERROR);
	}
}
