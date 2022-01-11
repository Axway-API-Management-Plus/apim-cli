package com.axway.apim.adapter.apis;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Alerts;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.POSTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class APIManagerAlertsAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerAlertsAdapter.class);
	
	ObjectMapper mapper = APIManagerAdapter.mapper;
	
	CoreParameters cmd = CoreParameters.getInstance();

	public APIManagerAlertsAdapter() {}
	
	String apiManagerResponse = null;
	
	Alerts managerAlerts = null;
	
	private void readAlertsFromAPIManager() throws AppException {
		if(apiManagerResponse != null) return;
		URI uri;
		HttpResponse httpResponse = null;
		try {			
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath() + "/alerts").build();
			RestAPICall getRequest = new GETRequest(uri, true);
			LOG.debug("Load configured alerts from API-Manager using filter");
			httpResponse = getRequest.execute();
			String response = EntityUtils.toString(httpResponse.getEntity());
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				LOG.error("Error loading alerts from API-Manager. Response-Code: "+statusCode+". Got response: '"+response+"'");
				throw new AppException("Error loading alerts from API-Manager. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			apiManagerResponse = response;
		} catch (Exception e) {
			LOG.error("Error cant read alerts from API-Manager. Can't parse response: " + httpResponse, e);
			throw new AppException("Can't read alerts from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public Alerts getAlerts() throws AppException {
		if(managerAlerts!=null) return managerAlerts;
		readAlertsFromAPIManager();
		try {
			Alerts alerts = mapper.readValue(apiManagerResponse, Alerts.class);
			managerAlerts = alerts;
			return alerts;
		} catch (IOException e) {
			throw new AppException("Error parsing API-Manager alerts", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	public Alerts updateAlerts(Alerts alerts) throws AppException {
		HttpResponse httpResponse = null;
		Alerts updatedAlerts;
		try {
			if(!APIManagerAdapter.hasAdminAccount()) {
				throw new AppException("An Admin Account is required to update the API-Manager alerts configuration.", ErrorCode.NO_ADMIN_ROLE_USER);
			}
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(cmd.getApiBasepath()+"/alerts").build();
			FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
					SimpleBeanPropertyFilter.serializeAllExcept());
			mapper.setFilterProvider(filter);
			mapper.setSerializationInclusion(Include.NON_NULL);
			try {
				RestAPICall request;
				String json = mapper.writeValueAsString(alerts);
				HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
				request = new POSTRequest(entity, uri, true);
				httpResponse = request.execute();
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if(statusCode < 200 || statusCode > 299){
					LOG.error("Error updating API-Manager alert configuration. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
					throw new AppException("Error updating API-Manager alert configuration. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
				}
				updatedAlerts = mapper.readValue(httpResponse.getEntity().getContent(), Alerts.class);
			} catch (Exception e) {
				throw new AppException("Error updating API-Manager alert configuration.", ErrorCode.API_MANAGER_COMMUNICATION, e);
			} finally {
				try {
					((CloseableHttpResponse)httpResponse).close();
				} catch (Exception ignore) { }
			}
			return updatedAlerts;

		} catch (Exception e) {
			throw new AppException("Error updating API-Manager alert configuration.", ErrorCode.CANT_CREATE_API_PROXY, e);
		}
	}
	
	void setAPIManagerTestResponse(String jsonResponse) {
		if(jsonResponse==null) {
			LOG.error("Test-Response is empty. Ignoring!");
			return;
		}
		this.apiManagerResponse = jsonResponse;
	}
}
