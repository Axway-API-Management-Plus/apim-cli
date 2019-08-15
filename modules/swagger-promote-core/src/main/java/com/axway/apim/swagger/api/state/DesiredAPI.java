package com.axway.apim.swagger.api.state;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.swagger.APIImportConfigAdapter;
import com.axway.apim.swagger.api.properties.inboundprofiles.InboundProfile;
import com.axway.apim.swagger.api.properties.outboundprofiles.OutboundProfile;
import com.axway.apim.swagger.api.properties.profiles.ServiceProfile;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Concrete class that is used to reflect the desired API as it's defined by the API-Developer. 
 * On the other hand, the APIManagerAPI reflects the actual state of the API inside the API-Manager.
 * 
 * Both classes extend the AbstractAPIDefinition which contains all the common API-Properties that 
 * are compared property by property in APIChangeState.
 * 
 * @author cwiechmann@axway.com
 */
public class DesiredAPI extends AbstractAPI implements IAPI {
	
	private static Logger LOG = LoggerFactory.getLogger(DesiredAPI.class);
	
	private String backendBasepath = null;
	
	private boolean requestForAllOrgs = false;
	
	/*
	 * Private fields to safe the original given profiles. This is required 
	 * given methodNames must be translated into internal operationIds for comparison.  
	 * But on Re-Creation the desired API still needs the original methodNames
	 */
	private Map<String, InboundProfile> originalInboundProfiles;
	private Map<String, OutboundProfile> originalOutboundProfiles;
	
	@JsonProperty("apiDefinition")
	public String apiDefinitionImport = null;
	
	public DesiredAPI() throws AppException {
		super();
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
		// If the backendBasePath has been changed already in the Swagger-File, don't to it here again
		// as it would duplicate the basePath
		if(backendBasepath!=null && !CommandParameters.getInstance().replaceHostInSwagger()) {
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
	 * requestForAllOrgs is used to decide if an API should grant access to ALL organizations. 
	 * That means, when an API-Developer is defining ALL as the organization name this flag 
	 * is set to true and it becomes the desired state.
	 * @return true, if the developer wants to have permissions to this API for all Orgs.
	 */
	public boolean isRequestForAllOrgs() {
		return requestForAllOrgs;
	}

	/**
	 * requestForAllOrgs is used to decide if an API should grant access to ALL organizations. 
	 * That means, when an API-Developer is defining ALL as the organization name this flag 
	 * is set to true and it becomes the desired state.
	 * This method is used during creation of APIImportDefinition in  APIImportConfig#handleAllOrganizations()
	 * @see APIImportConfigAdapter
	 * @param requestForAllOrgs when set to true, the APIs will be granted to ALL organizations.
	 */
	public void setRequestForAllOrgs(boolean requestForAllOrgs) {
		this.requestForAllOrgs = requestForAllOrgs;
	}

	@Override
	public String getApiDefinitionImport() {
		return apiDefinitionImport;
	}

	public void setApiDefinitionImport(String apiDefinitionImport) {
		this.apiDefinitionImport = apiDefinitionImport;
	}

	public Map<String, InboundProfile> getOriginalInboundProfiles() {
		return originalInboundProfiles;
	}

	public void setOriginalInboundProfiles(Map<String, InboundProfile> originalInboundProfiles) {
		if(originalInboundProfiles==null) return;
		this.originalInboundProfiles = new HashMap<String, InboundProfile>(originalInboundProfiles);
	}

	public Map<String, OutboundProfile> getOriginalOutboundProfiles() {
		return originalOutboundProfiles;
	}

	public void setOriginalOutboundProfiles(Map<String, OutboundProfile> originalOutboundProfiles) {
		if(originalOutboundProfiles==null) return;
		this.originalOutboundProfiles = new HashMap<String, OutboundProfile>(originalOutboundProfiles);
	}
}
