package com.axway.apim.apiimport.actions.tasks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter.METHOD_TRANSLATION;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.adapter.apis.jackson.PolicySerializer;
import com.axway.apim.api.API;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.props.PropertyHandler;
import com.axway.apim.lib.utils.rest.PUTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.axway.apim.lib.utils.rest.Transaction;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class UpdateAPIProxy extends AbstractAPIMTask {
	
	public UpdateAPIProxy(API desiredState, API actualState) {
		super(desiredState, actualState);
	}

	public void execute(List<String> changedProps) throws AppException {
		LOG.debug("Updating API-Proxy");
		URI uri;
		HttpEntity entity;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		HttpResponse httpResponse = null;
		Transaction context = Transaction.getInstance();
		
		try {
			JsonNode lastJsonReponse = (JsonNode)context.get("lastResponse");
			if(lastJsonReponse==null) { // This class is called as the first, so, first load the API
				lastJsonReponse = initActualAPIContext(this.actualState);
			}

			APIManagerAPIAdapter apiAdapter = (APIManagerAPIAdapter) APIManagerAdapter.getInstance().apiAdapter;
			apiAdapter.translateMethodIds(this.desiredState,this.actualState.getId(),  METHOD_TRANSLATION.AS_ID);
			
			lastJsonReponse = handledChangedProps(lastJsonReponse, this.desiredState, this.actualState, changedProps);
			if(lastJsonReponse == null) return; // No changes required for the API-Proxy
		
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/proxies/"+context.get("virtualAPIId")).build();
			entity = new StringEntity(objectMapper.writeValueAsString(lastJsonReponse), StandardCharsets.UTF_8);
			
			RestAPICall updateAPIProxy = new PUTRequest(entity, uri, null);
			httpResponse = updateAPIProxy.execute();
			String response = EntityUtils.toString(httpResponse.getEntity());
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				LOG.error("Error updating API-Proxy. Response-Code: "+statusCode+". Got response: '"+response+"'");
				LOG.debug("Request sent:" + lastJsonReponse);
				throw new AppException("Error updating API-Proxy. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
		} catch (Exception e) {
			throw new AppException("Cannot update API-Proxy.", ErrorCode.CANT_UPDATE_API_PROXY, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	private static JsonNode handledChangedProps(JsonNode lastJsonReponse, API desired, API actual, List<String> changedProps) throws AppException {
		Field field = null;
		if(changedProps!=null && changedProps.size()!=0) {
			boolean propsChangedInProxy = false;
			String logMessage = "Updating Frontend-API (Proxy) for the following properties: ";
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
							lastJsonReponse = propHandler.handleProperty(desired, actual, lastJsonReponse);
							logMessage = logMessage + field.getName() + " ";
							propsChangedInProxy = true;
						} else {
							try {
								String getterMethodName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
								Method method = desired.getClass().getSuperclass().getMethod(getterMethodName, null);
								Object handler = method.invoke(desired, null);
								if(handler instanceof PropertyHandler) { // This is NEW/Preferred way
									LOG.trace("Calling property handler: " + handler.getClass());
									((PropertyHandler)handler).handleProperty(desired, actual, lastJsonReponse);
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
			if(propsChangedInProxy) {
				LOG.info(logMessage);
			} else {
				LOG.debug("API-Proxy requires no updates");
				return null;
			}
		}
		return lastJsonReponse;
	}
}
