package com.axway.apim.api.export;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerOrganizationAdapter;
import com.axway.apim.adapter.apis.OrgFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.IAPI;
import com.axway.apim.api.definition.APISpecification;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.AuthType;
import com.axway.apim.api.model.AuthenticationProfile;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.api.model.CorsProfile;
import com.axway.apim.api.model.DeviceType;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.InboundProfile;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.api.model.SecurityDevice;
import com.axway.apim.api.model.SecurityProfile;
import com.axway.apim.api.model.ServiceProfile;
import com.axway.apim.api.model.TagMap;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonPropertyOrder({ "name", "path", "state", "version", "organization", "apiDefinition", "summary", "descriptionType", "descriptionManual", "vhost", 
	"backendBasepath", "image", "inboundProfiles", "outboundProfiles", "securityProfiles", "authenticationProfiles", "tags", "customProperties", 
	"corsProfiles", "caCerts", "applicationQuota", "systemQuota" })
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ExportAPI {
	
	API actualAPIProxy = null;
	
	public String getPath() throws AppException {
		return this.actualAPIProxy.getPath();
	}
	
	public ExportAPI() {
		
	}

	public ExportAPI(API actualAPIProxy) {
		super();
		this.actualAPIProxy = actualAPIProxy;
	}
	
	@JsonIgnore
	public Organization getOrganization() {
		return this.actualAPIProxy.getOrganization();
	}

	
	@JsonIgnore
	public APISpecification getAPIDefinition() {
		return this.actualAPIProxy.getAPIDefinition();
	}

	
	public Map<String, OutboundProfile> getOutboundProfiles() throws AppException {
		if(this.actualAPIProxy.getOutboundProfiles()==null) return null;
		if(this.actualAPIProxy.getOutboundProfiles().isEmpty()) return null;
		if(this.actualAPIProxy.getOutboundProfiles().size()==1) {
			OutboundProfile defaultProfile = this.actualAPIProxy.getOutboundProfiles().get("_default");
			if(defaultProfile.getRouteType().equals("proxy")
				&& defaultProfile.getAuthenticationProfile().equals("_default")
				&& defaultProfile.getRequestPolicy() == null 
				&& defaultProfile.getRequestPolicy() == null
				&& (APIManagerAdapter.hasAPIManagerVersion("7.6.2") && defaultProfile.getFaultHandlerPolicy() == null)
			) return null;
		}
		Iterator<OutboundProfile> it = this.actualAPIProxy.getOutboundProfiles().values().iterator();
		while(it.hasNext()) {
			OutboundProfile profile = it.next();
			profile.setApiId(null);
			if(profile.getRequestPolicy()!=null) profile.setRequestPolicy(getExternalPolicyName(profile.getRequestPolicy()));
			if(profile.getResponsePolicy()!=null) profile.setResponsePolicy(getExternalPolicyName(profile.getResponsePolicy()));
			if(profile.getRoutePolicy()!=null) profile.setRoutePolicy(getExternalPolicyName(profile.getRoutePolicy()));
			if(profile.getFaultHandlerPolicy()!=null) profile.setFaultHandlerPolicy(getExternalPolicyName(profile.getFaultHandlerPolicy()));
		}
		return this.actualAPIProxy.getOutboundProfiles();
	}
	
	private static String getExternalPolicyName(String policy) {
		if(policy.startsWith("<key")) {
			policy = policy.substring(policy.indexOf("<key type='FilterCircuit'>"));
			policy = policy.substring(policy.indexOf("value='")+7, policy.lastIndexOf("'/></key>"));
		}
		return policy;
	}

	
	public List<SecurityProfile> getSecurityProfiles() throws AppException {
		if(this.actualAPIProxy.getSecurityProfiles().size()==1) {
			if(this.actualAPIProxy.getSecurityProfiles().get(0).getDevices().get(0).getType()==DeviceType.passThrough)
				return null;
		}
		ListIterator<SecurityProfile> it = this.actualAPIProxy.getSecurityProfiles().listIterator();
		while(it.hasNext()) {
			SecurityProfile profile = it.next();
			for(SecurityDevice device : profile.getDevices()) {
				if(device.getType().equals(DeviceType.oauthExternal)) {
					String tokenStore = device.getProperties().get("tokenStore");
					if(tokenStore!=null) {
						device.getProperties().put("tokenStore", getExternalPolicyName(tokenStore));
					}
				} else if(device.getType().equals(DeviceType.authPolicy)) {
					String authenticationPolicy = device.getProperties().get("authenticationPolicy");
					if(authenticationPolicy!=null) {
						device.getProperties().put("authenticationPolicy", getExternalPolicyName(authenticationPolicy));
					}
				}
				device.setConvertPolicies(false);
			}
		}
		return this.actualAPIProxy.getSecurityProfiles();
	}

	
	public List<AuthenticationProfile> getAuthenticationProfiles() {
		if(this.actualAPIProxy.getAuthenticationProfiles().size()==1) {
			if(this.actualAPIProxy.getAuthenticationProfiles().get(0).getType()==AuthType.none)
			return null;
		}
		return this.actualAPIProxy.getAuthenticationProfiles();
	}
	
	public Map<String, InboundProfile> getInboundProfiles() {
		if(this.actualAPIProxy.getInboundProfiles()==null) return null;
		if(this.actualAPIProxy.getInboundProfiles().isEmpty()) return null;
		if(this.actualAPIProxy.getInboundProfiles().size()==1) {
			InboundProfile defaultProfile = this.actualAPIProxy.getInboundProfiles().get("_default");
			if(defaultProfile.getSecurityProfile().equals("_default")
				&& defaultProfile.getCorsProfile().equals("_default")) return null;
		}
		return this.actualAPIProxy.getInboundProfiles();
	}

	
	public List<CorsProfile> getCorsProfiles() {
		if(this.actualAPIProxy.getCorsProfiles()==null) return null;
		if(this.actualAPIProxy.getCorsProfiles().isEmpty()) return null;
		if(this.actualAPIProxy.getCorsProfiles().size()==1) {
			CorsProfile corsProfile = this.actualAPIProxy.getCorsProfiles().get(0);
			if(corsProfile.equals(CorsProfile.getDefaultCorsProfile())) return null;
		}
		return this.actualAPIProxy.getCorsProfiles();
	}

	
	public String getVhost() {
		return this.actualAPIProxy.getVhost();
	}

	
	public TagMap<String, String[]> getTags() {
		if(this.actualAPIProxy.getTags()==null) return null;
		if(this.actualAPIProxy.getTags().isEmpty()) return null;
		return this.actualAPIProxy.getTags();
	}

	
	public String getState() throws AppException {
		return this.actualAPIProxy.getState();
	}

	
	public String getVersion() {
		return this.actualAPIProxy.getVersion();
	}

	
	public String getSummary() {
		return this.actualAPIProxy.getSummary();
	}

	public String getImage() {
		if(this.actualAPIProxy.getImage()==null) return null;
		// We don't have an Image provided from the API-Manager
		return "api-image"+this.actualAPIProxy.getImage().getFileExtension();
	}
	
	@JsonIgnore
	public Image getAPIImage() {
		if(this.actualAPIProxy.getImage()==null) return null;
		return this.actualAPIProxy.getImage();
	}

	
	public String getName() {
		return this.actualAPIProxy.getName();
	}

	/*
	public String getOrganization() {
		String orgId = null;
		try {
			orgId = getOrganization();
			return new APIManagerOrganizationAdapter().getOrg(new OrgFilter.Builder().hasId(orgId).build()).getName();
		} catch (Exception e) {
			throw new RuntimeException("Can't read orgName for orgId: '"+orgId+"'");
		}
	}*/

	
	@JsonIgnore
	public String getDeprecated() {
		return ((API)this.actualAPIProxy).getDeprecated();
	}

	
	public Map<String, String> getCustomProperties() {
		return this.actualAPIProxy.getCustomProperties();
	}

	
	@JsonIgnore
	public int getAPIType() {
		return ((API)this.actualAPIProxy).getAPIType();
	}

	
	public String getDescriptionType() {
		if(this.actualAPIProxy.getDescriptionType().equals("original")) return null;
		return this.actualAPIProxy.getDescriptionType();
	}

	
	public String getDescriptionManual() {
		return this.actualAPIProxy.getDescriptionManual();
	}

	
	public String getDescriptionMarkdown() {
		return this.actualAPIProxy.getDescriptionMarkdown();
	}

	
	public String getDescriptionUrl() {
		return this.actualAPIProxy.getDescriptionUrl();
	}

	public List<CaCert> getCaCerts() {
		if(this.actualAPIProxy.getCaCerts()==null) return null;
		if(this.actualAPIProxy.getCaCerts().size()==0) return null;
		return this.actualAPIProxy.getCaCerts();
	}

	
	public APIQuota getApplicationQuota() {
		return this.actualAPIProxy.getApplicationQuota();
	}

	
	public APIQuota getSystemQuota() {
		return this.actualAPIProxy.getSystemQuota();
	}

	
	@JsonIgnore
	public Map<String, ServiceProfile> getServiceProfiles() {
		return this.actualAPIProxy.getServiceProfiles();
	}

	
	public List<Organization> getClientOrganizations() throws AppException {
		if(!APIManagerAdapter.hasAdminAccount()) return null; 
		if(this.actualAPIProxy.getClientOrganizations().size()==0) return null;
		if(this.actualAPIProxy.getClientOrganizations().size()==1 && 
				this.actualAPIProxy.getClientOrganizations().get(0).equals(getOrganization())) 
			return null;
		return this.actualAPIProxy.getClientOrganizations();
	}

	
	public List<ClientApplication> getApplications() {
		if(this.actualAPIProxy.getApplications().size()==0) return null;
		for(ClientApplication app : this.actualAPIProxy.getApplications()) {
			app.setId(null); // Don't export the Application-ID
			app.setOrganizationId(null); // Don't export the Application-ID
			app.setAppQuota(null); // Swagger-Promote doesn't managed quotas per apps
			app.setApiAccess(null); // Don't export API-Access
			app.setState(null);
			app.setCredentials(null);
		}
		return this.actualAPIProxy.getApplications();
	}

	
	@JsonProperty("apiDefinition")
	public String getApiDefinitionImport() {
		return this.getAPIDefinition().getApiSpecificationFile();
	}
	
	@JsonIgnore
	public String getBackendBasepath() {
		return this.getServiceProfiles().get("_default").getBasePath();
	}
}
