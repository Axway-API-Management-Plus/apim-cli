package com.axway.apim.apiimport;

import com.axway.apim.api.API;
import com.axway.apim.api.model.DesiredAPISpecification;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Concrete class that is used to reflect the desired API as it's defined by the API-Developer. 
 * On the other hand, the APIManagerAPI reflects the actual state of the API inside the API-Manager.
 * <p>
 * Both classes extend the AbstractAPIDefinition which contains all the common API-Properties that 
 * are compared property by property in APIChangeState.
 * 
 * @author cwiechmann@axway.com
 */
public class DesiredAPI extends API {
	
	private String backendBasepath = null;
	
	@JsonAlias("apiSpecification")
	private DesiredAPISpecification desiredAPISpecification = null;
	
	@JsonProperty("apiDefinition")
	public String apiDefinitionImport = null;
	
	public DesiredAPI() {
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
//		if(backendBasepath!=null && !CoreParameters.getInstance().isReplaceHostInSwagger()) {
//			ServiceProfile serviceProfile = new ServiceProfile();
//			serviceProfile.setBasePath(backendBasepath);
//			if(this.serviceProfiles == null) {
//				this.serviceProfiles = new LinkedHashMap<>();
//			}
//			serviceProfiles.put("_default", serviceProfile);
//		}
		this.backendBasepath = backendBasepath;
	}

	public String getApiDefinitionImport() {
		return apiDefinitionImport;
	}

	public void setApiDefinitionImport(String apiDefinitionImport) {
		this.apiDefinitionImport = apiDefinitionImport;
	}
	
	public void setRetirementDate(String retirementDate) throws AppException {
		this.retirementDate = Utils.getParsedDate(retirementDate);
	}

	public DesiredAPISpecification getDesiredAPISpecification() {
		return desiredAPISpecification;
	}

	public void setDesiredAPISpecification(DesiredAPISpecification desiredAPISpecification) {
		this.desiredAPISpecification = desiredAPISpecification;
	}
}
