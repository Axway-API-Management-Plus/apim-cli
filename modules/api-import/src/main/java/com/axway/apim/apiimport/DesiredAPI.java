package com.axway.apim.apiimport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.apis.jackson.OrganizationDeserializer;
import com.axway.apim.api.API;
import com.axway.apim.api.model.InboundProfile;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.api.model.ServiceProfile;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Concrete class that is used to reflect the desired API as it's defined by the API-Developer. 
 * On the other hand, the APIManagerAPI reflects the actual state of the API inside the API-Manager.
 * 
 * Both classes extend the AbstractAPIDefinition which contains all the common API-Properties that 
 * are compared property by property in APIChangeState.
 * 
 * @author cwiechmann@axway.com
 */
public class DesiredAPI extends API {
	
	private static Logger LOG = LoggerFactory.getLogger(DesiredAPI.class);
	
	private String backendBasepath = null;
	
	@JsonIgnore
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
	
	public void setRetirementDate(String retirementDate) throws AppException {
		List<String> dateFormats = Arrays.asList("dd.MM.yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy");
		SimpleDateFormat format;
		Date retDate = null;
		for (String dateFormat : dateFormats) {
			format = new SimpleDateFormat(dateFormat, Locale.US);
			format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Z")));
			try {
				retDate = format.parse(retirementDate);
			} catch (ParseException e) { }
			if(retDate!=null && retDate.after(new Date())) {
				LOG.info("Parsed retirementDate: '"+retirementDate+"' using format: '"+dateFormat+"' to: '"+retDate+"'");
				break;
			}
		}
		if(retDate==null || retDate.before(new Date())) {
			ErrorState.getInstance().setError("Unable to parse the given retirementDate using the following formats: " + dateFormats, ErrorCode.CANT_READ_CONFIG_FILE, false);
			throw new AppException("Cannnot parse given retirementDate", ErrorCode.CANT_READ_CONFIG_FILE);
		}
		this.retirementDate = retDate.getTime();
	}
}
