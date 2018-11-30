package com.axway.apim.swagger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.CreateNewAPI;
import com.axway.apim.actions.RecreateToUpdateAPI;
import com.axway.apim.actions.UpdateExistingAPI;
import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.APIManagerAPI;
import com.axway.apim.swagger.api.AbstractAPIDefinition;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author cwiechmann
 * This class is taking the API-Contract + API-Definition and imports 
 * it into the API. 
 * 
 * Internal note - to be removed, adjusted:
 * What we have to keep in mind:
 * - Import an API-Definition
 *   - the "Importer" should not take into consideration, if an API exists. Just imports it
 * - At certain place, the program must compare an existing API:
 *   - Identified with URI-Path + Version + (perhaps something else))
 *   - Maybe using Comparable Interface for that
 * - perhaps it makes sense to have an APIImportDefinition + ExistingAPIDefinition
 *   - Existing API-Definition is created based on the importAPI (looking up the API-Manager REST-API)
 *   - now two entities sharing the same interface (API-Definition, incl. the Contract) can be compared
 *   - or the existing API-Definition is just null
 * 
 * - both, can be handed over to the API-Manager-ImportHandler
 *   - it can compare ImportAPI vs. ExistingAPI
 *   - if "Existing == null" 
 *     - just import the new API
 *     
 * 
 * - Also must be possible:
 *   - Delete API
 *     - again ImportAPI is provided and existing must be found!
 *     - API-Contract status the API to be deleted, which will be synchronized with API-Manager
 *   - Deprecate API
 *     - API-Contract status the API as Deprecated, which will be synchronized with API-Manager
 *   - Handling Breaking vs. Non-Breaking-Changes
 *     - it is Internally defined what is breaking 
 *       - is Desired state vs. Actual state is breaking
 *         - which will be case quite often on the first development stage (having many deployment cycles)
 *         --> If the actual state is published: API-Developer must force the update
 *             and the version number must be different --> Otherwise error!
 *             The enforcement will be done based on the configured CI/CD Job not in the contract 
 *             to avoid forced updates by mistake on the Production environment
 *       - If Non-Breaking
 *         - the program will just update the existing API
 *         - BUT: If the desired API only contains changes, that can be applied to the existing API
 *                (like the description today, later Tags, Custom-Props)
 *                we update the existing API
 *                Or if just the state changes from Unpublished, to Published, etc. 
 *         - incl. Deprecation and removal of the existing API (should is be configurable?)
 *           --> How should this be configured (not in the contract)
 *          
 *   
 *   - The overall purpose (just came in my mind):
 *   The Swagger-Tool must basically synchronize the desired state (as provided with Contract + Swagger) 
 *   with the Actual State (as actually represented by the API-Manager for that API)
 */
public class APIManagerAdapter {
	
	private static Logger LOG = LoggerFactory.getLogger(APIManagerAdapter.class);
	
	private boolean enforceBreakingChange = false;
	
	public APIManagerAdapter() {
		super();
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
					LOG.error("Strategy: Applying ALL changes on existing UNPUBLISHED API.");
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
						LOG.error("A breaking change can't be applied without enforcing it!");
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
	
	/**
	 * Creates the API-Manager API-Representation. Basically the "Current" state of the API. 
	 * @param jsonConfiguration The JSON-Configuration returned from the API-Manager REST-Proxy endpoint
	 * @param importCustomProperties list of customProps declared (basically from the ImportAPI, as the API-Manager REST-API don't know it)
	 * @return an APIManagerAPI instance, which is flagged as valid, if the API was found or invalid, if not found
	 * @throws AppException
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
			
			return apiManagerApi;
		} catch (Exception e) {
			throw new AppException("Can't initialize API-Manager API-State.", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
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
}