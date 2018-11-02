package com.axway.apim.actions.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;

import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.PUTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.actions.tasks.props.PropertyHandler;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

public class UpdateAPIProxy extends AbstractAPIMTask implements IResponseParser {

	public static RestAPICall execute(IAPIDefinition desired, IAPIDefinition actual, List<String> changedProps) {
		LOG.info("Updating API-Proxy");
		URI uri;
		HttpEntity entity;
		ObjectMapper objectMapper = new ObjectMapper();
		
		Transaction context = Transaction.getInstance();
		
		try {
			JsonNode lastJsonReponse = (JsonNode)context.get("lastResponse");
			if(lastJsonReponse==null) { // This class is called as the first, so, first load the API
				lastJsonReponse = initActualAPIContext(actual);
			}
			handledChangedProps(lastJsonReponse, desired, changedProps);
		
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/"+context.get("virtualAPIId")).build();
			entity = new StringEntity(objectMapper.writeValueAsString(lastJsonReponse));
			
			RestAPICall updateAPIProxy = new PUTRequest(entity, uri);
			updateAPIProxy.registerResponseCallback(new UpdateAPIProxy());
			return updateAPIProxy;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public JsonNode parseResponse(InputStream response) {
		String backendAPIId = JsonPath.parse(response).read("$.id", String.class);
		Transaction.getInstance().put("backendAPIId", backendAPIId);
		return null;
	}	
	
	private static JsonNode handledChangedProps(JsonNode lastJsonReponse, IAPIDefinition desired, List<String> changedProps) {
		if(changedProps!=null) {
			String logMessage = "Considering changed properties found: ";
			for(String fieldName : changedProps) {
				try {
					Field field = desired.getClass().getSuperclass().getDeclaredField(fieldName);
					if (field.isAnnotationPresent(APIPropertyAnnotation.class)) {
						APIPropertyAnnotation property = field.getAnnotation(APIPropertyAnnotation.class);
						if(void.class != property.propHandler()) {
							Class clazz = property.propHandler();
							PropertyHandler propHandler = (PropertyHandler) clazz.newInstance();
							lastJsonReponse = propHandler.handleProperty(desired, lastJsonReponse);
							logMessage = logMessage + field.getName() + " ";
						} else {
							LOG.warn("Property: " + field.getName() + " has not handler configured and WILL NOT BE UPDATED!");
						}
					}
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			LOG.info(logMessage);
		}
		return lastJsonReponse;
	}
}
