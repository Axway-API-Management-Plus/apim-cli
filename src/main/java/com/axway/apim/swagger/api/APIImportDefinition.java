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
import com.axway.apim.swagger.APIImportConfig;
import com.axway.apim.swagger.APIManagerAdapter;
import com.axway.apim.swagger.api.properties.profiles.ServiceProfile;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Concrete class that is used to reflect the desired API as it's defined by the API-Developer. 
 * On the other hand, the APIManagerAPI reflects the actual state of the API inside the API-Manager.</br>
 * </br>
 * Both classes extend the AbstractAPIDefinition which contains all the common API-Properties that 
 * are compared property by property in APIChangeState.
 * 
 * @author cwiechmann@axway.com
 */
public class APIImportDefinition extends AbstractAPIDefinition implements IAPIDefinition {
	
	private static Logger LOG = LoggerFactory.getLogger(APIImportDefinition.class);
	
	private String orgId = null;
	
	private String backendBasepath = null;
	
	private boolean requestForAllOrgs = false;
	
	private String wsdlURL = null;
	
	public APIImportDefinition() throws AppException {
		super();
	}

	/**
	 * This method translates from the given Org-Name to the Org-Id!</br>
	 * </br> 
	 * TODO: Potential duplicate method:
	 * @see APIManagerAdapter#getOrgId(String)
	 * @see com.axway.apim.swagger.api.AbstractAPIDefinition#getOrgId()
	 */
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

	/**
	 * BackendBasePath is a property which doesn't exists in API-Manager naturally. 
	 * It simplifies the use of the tool.
	 * @return the URL to the BE-API-Host.
	 */
	public String getBackendBasepath() {
		return backendBasepath;
	}

	/**
	 * BackendBasePath is a property which doesn't exists in API-Manager naturally. 
	 * It simplifies the use of the tool. If the backendBasePath is set internally a 
	 * ServiceProfile is created by this method.
	 * @param backendBasepath the URL to the BE-API-Host.
	 */
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
	
	/**
	 * requestForAllOrgs is used to decide if an API should grant access to ALL organizations.</br> 
	 * That means, when an API-Developer is defining ALL as the organization name this flag 
	 * is set to true and it becomes the desired state.
	 * @return true, if the developer wants to have permissions to this API for all Orgs.
	 */
	public boolean isRequestForAllOrgs() {
		return requestForAllOrgs;
	}

	/**
	 * requestForAllOrgs is used to decide if an API should grant access to ALL organizations.</br> 
	 * That means, when an API-Developer is defining ALL as the organization name this flag 
	 * is set to true and it becomes the desired state.</br>
	 * This method is used during creation of APIImportDefinition in  APIImportConfig#handleAllOrganizations()
	 * @see APIImportConfig
	 * @param requestForAllOrgs
	 */
	public void setRequestForAllOrgs(boolean requestForAllOrgs) {
		this.requestForAllOrgs = requestForAllOrgs;
	}

	@Override
	public void setWsdlURL(String wsdlURL) {
		this.wsdlURL=wsdlURL;
		
	}

	@Override
	public String getWsdlURL() {
		return this.wsdlURL;
	}
}
