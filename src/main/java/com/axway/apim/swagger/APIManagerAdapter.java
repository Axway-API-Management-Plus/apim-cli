package com.axway.apim.swagger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.CreateNewAPI;
import com.axway.apim.actions.RecreateToUpdateAPI;
import com.axway.apim.actions.UpdateExistingAPI;
import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.POSTRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.actions.rest.Transaction;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.APIManagerAPI;
import com.axway.apim.swagger.api.AbstractAPIDefinition;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
import com.axway.apim.swagger.api.properties.cacerts.CaCert;
import com.axway.apim.swagger.api.properties.quota.APIQuota;
import com.axway.apim.swagger.api.properties.quota.QuotaRestriction;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author cwiechmann
 */
public class APIManagerAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerAdapter.class);
	
	private static String apiManagerVersion = null;
	
	private boolean enforceBreakingChange = false;
	
	public static APIQuota sytemQuotaConfig = null;
	public static APIQuota applicationQuotaConfig = null;
	
	public APIManagerAdapter() throws AppException {
		super();
		loginToAPIManager();
		this.enforceBreakingChange = CommandParameters.getInstance().isEnforceBreakingChange();
	}

	public void applyChanges(APIChangeState changeState) throws AppException {
		// No existing API found (means: No match for APIPath), creating a complete new
		if(!changeState.getActualAPI().isValid()) {
			// --> CreateNewAPI
			LOG.info("Strategy: No existing API found, creating new!");
			CreateNewAPI createAPI = new CreateNewAPI();
			createAPI.execute(changeState);
		// We do have a breaking change!
		} else {
			LOG.info("Strategy: Going to update existing API: " + changeState.getActualAPI().getName() +" (Version: "+ changeState.getActualAPI().getVersion() + ")");
			if(!changeState.hasAnyChanges()) {
				LOG.warn("BUT, no changes detected between Import- and API-Manager-API. Exiting now...");
				throw new AppException("No changes detected between Import- and API-Manager-API", ErrorCode.NO_CHANGE, false);
			}			
			if (changeState.isBreaking()) {
				LOG.info("Recognized the following changes. Breaking: " + changeState.getBreakingChanges() + 
						" plus Non-Breaking: " + changeState.getNonBreakingChanges());
				if(changeState.getActualAPI().getState().equals(IAPIDefinition.STATE_UNPUBLISHED)) {
					LOG.info("Strategy: Applying ALL changes on existing UNPUBLISHED API.");
					UpdateExistingAPI updateAPI = new UpdateExistingAPI();
					updateAPI.execute(changeState);
					return;
				} else { // Breaking-Changes for PUBLISHED APIs and Non-Breaking
					if(enforceBreakingChange) {
						if(changeState.isUpdateExistingAPI()) {
							LOG.info("Strategy: Breaking changes - Updating existing API: " + changeState.getBreakingChanges() + 
									" plus Non-Breaking: " + changeState.getNonBreakingChanges());
							UpdateExistingAPI updateAPI = new UpdateExistingAPI();
							updateAPI.execute(changeState);
							return;
						} else {
							LOG.info("Strategy: Apply breaking changes: "+changeState.getBreakingChanges()+" & and "
									+ "Non-Breaking: "+changeState.getNonBreakingChanges()+", for PUBLISHED API. Recreating it!");
							RecreateToUpdateAPI recreate = new RecreateToUpdateAPI();
							recreate.execute(changeState);
						}
					} else {
						LOG.error("A breaking change can't be applied without enforcing it! Try option: -f true");
						return;
					}
				}
			// A NON-Breaking change
			} else if(!changeState.isBreaking()) {
				if(changeState.isUpdateExistingAPI()) {
					// Contains only changes, that can be applied to the existing API (even depends on the status)
					LOG.info("Strategy: No breaking change - Updating existing API: " + changeState.getNonBreakingChanges());
					UpdateExistingAPI updateAPI = new UpdateExistingAPI();
					updateAPI.execute(changeState);
					return;
				} else {
					// We have changes requiring a new API to be imported
					LOG.info("Strategy: No breaking change - Create and Update API, delete existing: " +  changeState.getNonBreakingChanges());
					RecreateToUpdateAPI recreate = new RecreateToUpdateAPI();
					recreate.execute(changeState);
				}
			}
		}
	}
	
	private void loginToAPIManager() throws AppException {
		URI uri;
		CommandParameters cmd = CommandParameters.getInstance();
		Transaction transaction = Transaction.getInstance();
		transaction.beginTransaction();
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/login").build();
			
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
		    params.add(new BasicNameValuePair("username", cmd.getUsername()));
		    params.add(new BasicNameValuePair("password", cmd.getPassword()));
		    POSTRequest loginRequest = new POSTRequest(new UrlEncodedFormEntity(params), uri, null);
			loginRequest.setContentType(null);
		    
			loginRequest.execute();
		} catch (Exception e) {
			throw new AppException("Can't login to API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		
		
	}
	
	/**
	 * Creates the API-Manager API-Representation. Basically the "Current" state of the API. 
	 * @param jsonConfiguration The JSON-Configuration returned from the API-Manager REST-Proxy endpoint
	 * @param importCustomProperties list of customProps declared (basically from the ImportAPI, as the API-Manager REST-API don't know it)
	 * @return an APIManagerAPI instance, which is flagged as valid, if the API was found or invalid, if not found
	 * @throws AppException when the API-Manager API-State can't be created
	 */
	public static IAPIDefinition getAPIManagerAPI(JsonNode jsonConfiguration, Map<String, String> importCustomProperties) throws AppException {
		if(jsonConfiguration == null) {
			IAPIDefinition apiManagerAPI = new APIManagerAPI();
			apiManagerAPI.setValid(false);
			return apiManagerAPI;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		IAPIDefinition apiManagerApi;
		try {
			apiManagerApi = mapper.readValue(jsonConfiguration.toString(), APIManagerAPI.class);
			apiManagerApi.setSwaggerDefinition(new APISwaggerDefinion(getOriginalSwaggerFromAPIM(apiManagerApi.getApiId())));
			if(apiManagerApi.getImage()!=null) {
				apiManagerApi.getImage().setImageContent(getAPIImageFromAPIM(apiManagerApi.getId()));
			}
			apiManagerApi.setValid(true);
			// As the API-Manager REST doesn't provide information about Custom-Properties, we have to setup 
			// the Custom-Properties based on the Import API.
			if(importCustomProperties != null) {
				Map<String, String> customProperties = new LinkedHashMap<String, String>();
				Iterator<String> it = importCustomProperties.keySet().iterator();
				while(it.hasNext()) {
					String customPropKey = it.next();
					JsonNode value = jsonConfiguration.get(customPropKey);
					String customPropValue = (value == null) ? null : value.asText();
					customProperties.put(customPropKey, customPropValue);
				}
				((AbstractAPIDefinition)apiManagerApi).setCustomProperties(customProperties);
			}
			addQuotaConfiguration(apiManagerApi);
			return apiManagerApi;
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-State.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	private static void addQuotaConfiguration(IAPIDefinition api) throws AppException {
		//APPLICATION:	00000000-0000-0000-0000-000000000001
		//SYSTEM: 		00000000-0000-0000-0000-000000000000
		APIManagerAPI managerAPI = (APIManagerAPI)api;
		try {
			applicationQuotaConfig = getQuotaFromAPIManager("00000000-0000-0000-0000-000000000001"); // Get the Application-Default-Quota
			sytemQuotaConfig = getQuotaFromAPIManager("00000000-0000-0000-0000-000000000000"); // Get the System-Default-Quota
			managerAPI.setApplicationQuota(getAPIQuota(applicationQuotaConfig, managerAPI.getId()));
			managerAPI.setSystemQuota(getAPIQuota(sytemQuotaConfig, managerAPI.getId()));
		} catch (Exception e) {
			LOG.error("Application-Default quota response: '"+applicationQuotaConfig+"'");
			LOG.error("System-Default quota response: '"+sytemQuotaConfig+"'");
			throw new AppException("Can't initialize API-Manager Quota-Configuration", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	private static APIQuota getQuotaFromAPIManager(String type) throws AppException {
		ObjectMapper mapper = new ObjectMapper();
		URI uri;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/quotas/"+type).build();
			RestAPICall getRequest = new GETRequest(uri, null);
			HttpEntity response = getRequest.execute().getEntity();
			String config = IOUtils.toString(response.getContent(), "UTF-8");
			APIQuota quotaConfig = mapper.readValue(config, APIQuota.class);
			return quotaConfig;
		} catch (Exception e) {
			throw new AppException("Can't get API-Manager Quota-Configuration.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	private static APIQuota getAPIQuota(APIQuota quotaConfig, String apiId) throws AppException {
		APIQuota apiQuota;
		try {
			for(QuotaRestriction restriction : quotaConfig.getRestrictions()) {
				if(restriction.getApi().equals(apiId)) {
					apiQuota = new APIQuota();
					apiQuota.setDescription(quotaConfig.getDescription());
					apiQuota.setName(quotaConfig.getName());
					apiQuota.setRestrictions(new ArrayList<QuotaRestriction>());
					apiQuota.getRestrictions().add(restriction);
					return apiQuota;
				}
			}
		} catch (Exception e) {
			throw new AppException("Can't parse quota from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
		return null;
	}
	
	public static JsonNode getExistingAPI(String apiPath) throws AppException {
		CommandParameters cmd = CommandParameters.getInstance();
		ObjectMapper mapper = new ObjectMapper();
		URI uri;
		try {
			uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/proxies").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();
			
			JsonNode jsonResponse;
			String path;
			String apiId = null;
			try {
				jsonResponse = mapper.readTree(response);
				for(JsonNode node : jsonResponse) {
					path = node.get("path").asText();
					if(path.equals(apiPath)) {
						LOG.info("Found existing API on path: '"+path+"' / "+node.get("state").asText()+" ('" + node.get("id").asText()+"')");
						apiId = node.get("id").asText();
						break;
					}
				}
				if(apiId==null) {
					LOG.info("No existing API found exposed on: " + apiPath);
					return null;
				}
				uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/proxies/"+apiId).build();
				getRequest = new GETRequest(uri, null);
				response = getRequest.execute().getEntity().getContent();
				jsonResponse = mapper.readTree(response);
				return jsonResponse;
			} catch (IOException e) {
				throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
			}
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-Representation.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	private static byte[] getOriginalSwaggerFromAPIM(String backendApiID) throws AppException {
		URI uri;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/apirepo/"+backendApiID+"/download")
					.setParameter("original", "true").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			InputStream response = getRequest.execute().getEntity().getContent();
			return IOUtils.toByteArray(response);
		} catch (Exception e) {
			throw new AppException("Can't read Swagger-File.", ErrorCode.CANT_READ_SWAGGER_FILE, e);
		}
	}
	
	private static byte[] getAPIImageFromAPIM(String backendApiID) throws AppException {
		URI uri;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/proxies/"+backendApiID+"/image").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			HttpEntity response = getRequest.execute().getEntity();
			if(response == null) return null; // no Image found in API-Manager
			InputStream is = response.getContent();
			return IOUtils.toByteArray(is);
		} catch (Exception e) {
			throw new AppException("Can't read Image from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	public static String getApiManagerVersion() throws AppException {
		if(APIManagerAdapter.apiManagerVersion!=null) {
			return apiManagerVersion;
		}
		ObjectMapper mapper = new ObjectMapper();
		String response = null;
		URI uri;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/config").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			HttpResponse httpResponse = getRequest.execute();
			response = EntityUtils.toString(httpResponse.getEntity());
			JsonNode jsonResponse;
			jsonResponse = mapper.readTree(response);
			String apiManagerVersion = jsonResponse.get("productVersion").asText();
			LOG.debug("API-Manager version is: " + apiManagerVersion);
			return jsonResponse.get("productVersion").asText();
		} catch (Exception e) {
			LOG.error("Error AppInfo from API-Manager. Can't parse response: " + response);
			throw new AppException("Can't get version from API-Manager", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	public static JsonNode getCustomPropertiesConfig() throws AppException {
		
		String appConfig = null;
		URI uri;
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath("/vordel/apiportal/app/app.config").build();
			RestAPICall getRequest = new GETRequest(uri, null);
			HttpEntity response = getRequest.execute().getEntity();
			appConfig = IOUtils.toString(response.getContent(), "UTF-8");
			return parseAppConfig(appConfig);
		} catch (Exception e) {
			throw new AppException("Can't read app.config from API-Manager: '" + appConfig + "'", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	public static JsonNode parseAppConfig(String appConfig) throws AppException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			appConfig = appConfig.substring(appConfig.indexOf("customPropertiesConfig:")+23, appConfig.indexOf("wizardModels"));
			//appConfig = appConfig.substring(0, appConfig.length()-1); // Remove the tail comma
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
			mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
			mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
			return mapper.readTree(appConfig);
		} catch (Exception e) {
			throw new AppException("Can't parse API-Manager app.config.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
	
	public static JsonNode getCertInfo(InputStream certFile, CaCert cert) throws AppException {
		URI uri;
		ObjectMapper mapper = new ObjectMapper();
		try {
			uri = new URIBuilder(CommandParameters.getInstance().getAPIManagerURL()).setPath(RestAPICall.API_VERSION + "/certinfo/").build();
			
			HttpEntity entity = MultipartEntityBuilder.create()
					.addBinaryBody("file", IOUtils.toByteArray(certFile), ContentType.create("application/x-x509-ca-cert"), cert.getCertFile())
					.addTextBody("inbound", cert.getInbound())
					.addTextBody("outbound", cert.getOutbound())
					.build();
			POSTRequest postRequest = new POSTRequest(entity, uri, null);
			postRequest.setContentType(null);
			HttpEntity response = postRequest.execute().getEntity();
			JsonNode jsonResponse = mapper.readTree(response.getContent());
			return jsonResponse;
		} catch (Exception e) {
			throw new AppException("Can't read certificate information from API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}
}
