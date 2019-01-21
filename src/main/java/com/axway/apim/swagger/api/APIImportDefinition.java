package com.axway.apim.swagger.api;

import java.net.URI;
import java.util.LinkedHashMap;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.properties.profiles.ServiceProfile;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author cwiechmann
 * This class reflects the given API-Swagger-Specification plus 
 * the API-Contract and it used by the APIManagerImporter to 
 * import it.
 * 
 * TODO: Support JSON and YAML files 
 */
public class APIImportDefinition extends AbstractAPIDefinition implements IAPIDefinition {
	
	private static Logger LOG = LoggerFactory.getLogger(APIImportDefinition.class);
	
	private String orgId = null;
	
	private String backendBasepath = null;
	
	private boolean requestForAllOrgs = false;
	
	public APIImportDefinition() throws AppException {
		super();
	}

	@Override
	public String getOrgId() throws AppException {
		if(this.orgId!=null) return this.orgId;
		String response = null;
		try {
			LOG.info("Getting details for organization: " + this.organization + " from API-Manager!");
			URI uri = new URIBuilder(cmd.getAPIManagerURL()).setPath(RestAPICall.API_VERSION+"/organizations/")
					.setParameter("field", "name")
					.setParameter("op", "eq")
					.setParameter("value", this.organization).build();
			GETRequest getRequest = new GETRequest(uri, null);
			response = EntityUtils.toString(getRequest.execute().getEntity());
			JsonNode jsonNode = objectMapper.readTree(response);
			if(jsonNode==null) LOG.error("Unable to read details for org: " + this.organization);
			return jsonNode.get(0).get("id").asText();
		} catch (Exception e) {
			LOG.error("Received response: " + response);
			throw new AppException("Can't read Org-Details from API-Manager. Is the API-Manager running and "
					+ "does the Organization: '"+this.organization+"' exists?", ErrorCode.API_MANAGER_COMMUNICATION, e);
		}
	}

	public String getBackendBasepath() {
		return backendBasepath;
	}

	public void setBackendBasepath(String backendBasepath) {
		if(backendBasepath!=null) {
			ServiceProfile serviceProfile = new ServiceProfile();
			serviceProfile.setBasePath(backendBasepath);
			if(this.serviceProfiles == null) {
				this.serviceProfiles = new LinkedHashMap<String, ServiceProfile>();
			}
			serviceProfiles.put("_default", serviceProfile);
		}
		this.backendBasepath = backendBasepath;
	}
	
	public boolean isRequestForAllOrgs() {
		return requestForAllOrgs;
	}

	public void setRequestForAllOrgs(boolean requestForAllOrgs) {
		this.requestForAllOrgs = requestForAllOrgs;
	}
}
