package com.axway.apim.adapter.apis;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
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
import com.axway.apim.adapter.jackson.PolicySerializerModifier;
import com.axway.apim.api.model.Config;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.axway.apim.lib.utils.rest.GETRequest;
import com.axway.apim.lib.utils.rest.PUTRequest;
import com.axway.apim.lib.utils.rest.RestAPICall;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class APIManagerConfigAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerConfigAdapter.class);
	
	ObjectMapper mapper = APIManagerAdapter.mapper;
	
	CoreParameters cmd = CoreParameters.getInstance();

	public APIManagerConfigAdapter() {}
	
	Map<Boolean, String> apiManagerResponse = new HashMap<Boolean, String>();
	
	Map<Boolean, Config> managerConfig = new HashMap<Boolean, Config>();
	
	
	/**
	 * Config fields that were introduced with a certain API-Manager version. 
	 * This list is mainly used to filter out fields, when using an older API-Manager version.
	 */
	protected static enum ConfigFields {
		version7720200130 ("7.7.20200130", new String[] {"apiImportTimeout", "apiImportMimeValidation", "apiImportEditable", "lockUserAccount" }),
		version77 ("7.7.0", new String[] {
				"userNameRegex", "changePasswordOnFirstLogin", "passwordExpiryEnabled", "passwordLifetimeDays",  
				"applicationScopeRestrictions", "strictCertificateChecking", "serverCertificateVerification", "advisoryBannerEnabled", "advisoryBannerText"
				});
		
		private String[] ignoreFields;
		private String managerVersion;
		
		ConfigFields(String managerVersion, String[] ignoreFields) {
			this.ignoreFields = ignoreFields;
			this.managerVersion = managerVersion;
		}
		
		public String getManagerVersion() {
			return managerVersion;
		}
		
		public static String[] getIgnoredFields() {
			String[] restrictedFields = new String[] {};
			for(ConfigFields fields : values()) {
				// Add all ignore fields until we reached the used API-Manager version
				if(APIManagerAdapter.hasAPIManagerVersion(fields.getManagerVersion())) {
					break;
				}
				restrictedFields = ArrayUtils.addAll(restrictedFields, fields.ignoreFields);
			}
			return restrictedFields;
		}
		
	}
	
	private void readConfigFromAPIManager(boolean useAdmin) throws AppException {
		if(apiManagerResponse.get(useAdmin) != null) return;
		URI uri;
		HttpResponse httpResponse = null;
		try {			
			uri = new URIBuilder(CoreParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/config").build();
			RestAPICall getRequest = new GETRequest(uri, useAdmin);
			httpResponse = getRequest.execute();
			String response = EntityUtils.toString(httpResponse.getEntity());
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if(statusCode < 200 || statusCode > 299){
				LOG.error("Error loading configuration from API-Manager. Response-Code: "+statusCode+". Got response: '"+response+"'");
				throw new AppException("Error loading configuration from API-Manager. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
			}
			apiManagerResponse.put(useAdmin, response);
		} catch (Exception e) {
			LOG.error("Error cant read configuration from API-Manager. Can't parse response: " + httpResponse, e);
			throw new AppException("Can't read configuration from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		} finally {
			try {
				if(httpResponse!=null) 
					((CloseableHttpResponse)httpResponse).close();
			} catch (Exception ignore) {}
		}
	}
	
	public Config getConfig(boolean useAdmin) throws AppException {
		if(managerConfig.get(useAdmin)!=null) return managerConfig.get(useAdmin);
		readConfigFromAPIManager(useAdmin);
		try {
			Config config = mapper.readValue(apiManagerResponse.get(useAdmin), Config.class);
			managerConfig.put(useAdmin, config);
			return config;
		} catch (IOException e) {
			throw new AppException("Error parsing API-Manager configuration", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	public Config updateConfiguration(Config desiredConfig) throws AppException {
		HttpResponse httpResponse = null;
		Config updatedConfig;
		try {
			if(!APIManagerAdapter.hasAdminAccount()) {
				ErrorState.getInstance().setError("An Admin Account is required to update the API-Manager configuration.", ErrorCode.NO_ADMIN_ROLE_USER, false);
				throw new AppException("An Admin Account is required to update the API-Manager configuration.", ErrorCode.NO_ADMIN_ROLE_USER);
			}
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/config").build();
			FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(
					SimpleBeanPropertyFilter.serializeAllExcept(ConfigFields.getIgnoredFields()));
			mapper.setFilterProvider(filter);
			mapper.registerModule(new SimpleModule().setSerializerModifier(new PolicySerializerModifier(false)));
			mapper.setSerializationInclusion(Include.NON_NULL);
			try {
				RestAPICall request;
				String json = mapper.writeValueAsString(desiredConfig);
				HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
				request = new PUTRequest(entity, uri, true);
				request.setContentType("application/json");
				httpResponse = request.execute();
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if(statusCode < 200 || statusCode > 299){
					LOG.error("Error updating API-Manager configuration. Response-Code: "+statusCode+". Got response: '"+EntityUtils.toString(httpResponse.getEntity())+"'");
					throw new AppException("Error updating API-Manager configuration. Response-Code: "+statusCode+"", ErrorCode.API_MANAGER_COMMUNICATION);
				}
				updatedConfig = mapper.readValue(httpResponse.getEntity().getContent(), Config.class);
			} catch (Exception e) {
				throw new AppException("Error updating API-Manager configuration.", ErrorCode.API_MANAGER_COMMUNICATION, e);
			} finally {
				try {
					((CloseableHttpResponse)httpResponse).close();
				} catch (Exception ignore) { }
			}
			return updatedConfig;

		} catch (Exception e) {
			throw new AppException("Error updating API-Manager configuration.", ErrorCode.CANT_CREATE_API_PROXY, e);
		}
	}
	
	void setAPIManagerTestResponse(String jsonResponse, boolean useAdmin) {
		if(jsonResponse==null) {
			LOG.error("Test-Response is empty. Ignoring!");
			return;
		}
		this.apiManagerResponse.put(useAdmin, jsonResponse);
	}
}
