package com.axway.apim.actions.tasks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.axway.apim.actions.rest.PUTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.actions.tasks.props.PropertyHandler;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.state.IAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateAPIProxy extends AbstractAPIMTask implements IResponseParser {
	
	public UpdateAPIProxy(IAPI desiredState, IAPI actualState) {
		super(desiredState, actualState);
	}

	public void execute(List<String> changedProps) throws AppException {
		LOG.debug("Updating API-Proxy");
		URI uri;
		HttpEntity entity;
		ObjectMapper objectMapper = new ObjectMapper();
		
		Transaction context = Transaction.getInstance();
		
		try {
			JsonNode lastJsonReponse = (JsonNode)context.get("lastResponse");
			if(lastJsonReponse==null) { // This class is called as the first, so, first load the API
				lastJsonReponse = initActualAPIContext(this.actualState);
			}
			handledChangedProps(lastJsonReponse, this.desiredState, changedProps);
		
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/"+context.get("virtualAPIId")).build();
			entity = new StringEntity(objectMapper.writeValueAsString(lastJsonReponse));
			
			RestAPICall updateAPIProxy = new PUTRequest(entity, uri, this);
			updateAPIProxy.execute();
		} catch (Exception e) {
			throw new AppException("Cannot update API-Proxy.", ErrorCode.CANT_UPDATE_API_PROXY, e);
		}
	}
	
	@Override
	public JsonNode parseResponse(HttpResponse httpResponse) throws AppException {
		ObjectMapper objectMapper = new ObjectMapper();
		String response = null;
		try {
			response = EntityUtils.toString(httpResponse.getEntity());
			if(httpResponse.getStatusLine().getStatusCode()!=200) {
				throw new AppException("Error updating API-Proxy. "
						+ "[Return-Code: " + httpResponse.getStatusLine().getStatusCode() + ", Response: '"+response+"'", ErrorCode.CANT_UPDATE_API_PROXY);
			}
			JsonNode jsonNode = objectMapper.readTree(response);
			String backendAPIId = jsonNode.findPath("id").asText();
			Transaction.getInstance().put("backendAPIId", backendAPIId);
		} catch (Exception e) {
			try {
				Transaction context = Transaction.getInstance();
				Object lastRequest = context.get("lastRequest");
				LOG.error("Last request: " + EntityUtils.toString(((HttpEntityEnclosingRequestBase)lastRequest).getEntity()));
				LOG.error("Unable to parse received response from API-Manager: '" + response + "'");
				throw new AppException("Unable to parse received response from API-Manager", ErrorCode.UNXPECTED_ERROR, e);
			} catch (Exception e1) {
				throw new AppException("Unable to parse response", ErrorCode.UNXPECTED_ERROR, e1);
			}
		}
		return null;
	}	
	
	private static JsonNode handledChangedProps(JsonNode lastJsonReponse, IAPI desired, List<String> changedProps) throws AppException {
		Field field = null;
		if(changedProps!=null && changedProps.size()!=0) {
			boolean propsChangedInProxy = false;
			String logMessage = "Updating proxy for the following properties: ";
			for(String fieldName : changedProps) {
				try {
					field = desired.getClass().getSuperclass().getDeclaredField(fieldName);
					if (field.isAnnotationPresent(APIPropertyAnnotation.class)) {
						LOG.debug("Going to update property: " + field.getName());
						APIPropertyAnnotation property = field.getAnnotation(APIPropertyAnnotation.class);
						if(void.class != property.propHandler()) { // Properties going this way, must be migrated
							Class clazz = property.propHandler();
							LOG.trace("Calling property handler: " + clazz.getCanonicalName());
							PropertyHandler propHandler = (PropertyHandler) clazz.newInstance();
							lastJsonReponse = propHandler.handleProperty(desired, lastJsonReponse);
							logMessage = logMessage + field.getName() + " ";
							propsChangedInProxy = true;
						} else {
							try {
								String getterMethodName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
								Method method = desired.getClass().getSuperclass().getMethod(getterMethodName, null);
								Object handler = method.invoke(desired, null);
								if(handler instanceof PropertyHandler) { // This is NEW/Preferred way
									LOG.trace("Calling property handler: " + handler.getClass());
									((PropertyHandler)handler).handleProperty(desired, lastJsonReponse);
									logMessage = logMessage + field.getName() + " ";
									propsChangedInProxy = true;
								} else {
									LOG.debug("Property: " + field.getName() + " has no handler configured and is not a propertyHandler");
								}
							} catch (Exception e) {
								LOG.debug("Property: " + field.getName() + " has no handler configured and is not a propertyHandler");
							}
						}
					}
				} catch (Exception e) {
					throw new AppException("Can't handle property: "+field+" to update API-Proxy.", ErrorCode.CANT_UPDATE_API_PROXY, e);
				}
			}
			if(propsChangedInProxy)
				LOG.info(logMessage);
		}
		return lastJsonReponse;
	}
}
