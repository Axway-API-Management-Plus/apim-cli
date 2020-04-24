package com.axway.apim.api.state;

import java.util.List;
import java.util.Map;

import com.axway.apim.actions.tasks.props.APICaCertsPropertyHandler;
import com.axway.apim.actions.tasks.props.APIDescriptionPropertyHandler;
import com.axway.apim.actions.tasks.props.APINamePropertyHandler;
import com.axway.apim.actions.tasks.props.APIPathPropertyHandler;
import com.axway.apim.actions.tasks.props.APISummaryPropertyHandler;
import com.axway.apim.actions.tasks.props.APITagsPropertyHandler;
import com.axway.apim.actions.tasks.props.APIVersionPropertyHandler;
import com.axway.apim.actions.tasks.props.AuthenticationProfileHandler;
import com.axway.apim.actions.tasks.props.CorsProfileHandler;
import com.axway.apim.actions.tasks.props.CustomPropertyHandler;
import com.axway.apim.actions.tasks.props.InboundProfileHandler;
import com.axway.apim.actions.tasks.props.OutboundProfileHandler;
import com.axway.apim.actions.tasks.props.RoutingKeyPropHandler;
import com.axway.apim.actions.tasks.props.SecurityProfileHandler;
import com.axway.apim.actions.tasks.props.ServiceProfileHandler;
import com.axway.apim.actions.tasks.props.VhostPropertyHandler;
import com.axway.apim.api.properties.APIDefintion;
import com.axway.apim.api.properties.APIImage;
import com.axway.apim.api.properties.APIQuota;
import com.axway.apim.api.properties.AuthenticationProfile;
import com.axway.apim.api.properties.CaCert;
import com.axway.apim.api.properties.ClientApplication;
import com.axway.apim.api.properties.CorsProfile;
import com.axway.apim.api.properties.InboundProfile;
import com.axway.apim.api.properties.OutboundProfile;
import com.axway.apim.api.properties.SecurityProfile;
import com.axway.apim.api.properties.ServiceProfile;
import com.axway.apim.api.properties.TagMap;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.lib.AppException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

/**
 * This class defines all common properties on an API and how each property should be 
 * treated during replication.
 * APIManagerAPI and APIImportDefintion are both an instance of this class.
 * 
 * Annotations for each property are used by the APIChangeState to decide:
 * - Is is a breaking change?
 * - Can the change be applied to the existing API?
 * - Which Change-Handler should finally do the required actions to replicate the change into the APIManager
 * 
 * When adding new properties, please make sure to create Getter and Setter as Jackson is used to create the Instances.
 * 
 * Perhaps a way to simplify the code is to use for many of the properties is to use a SimplePropertyHandler 
 * as many properties are handled in the same way.
 * 
 * 
 * @author cwiechmann@axway.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractAPI {
	
	@APIPropertyAnnotation(isBreaking = true, writableStates = {})
	protected APIDefintion APIDefinition = null;

	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPI.STATE_UNPUBLISHED}, 
			propHandler = APICaCertsPropertyHandler.class)
	protected List<CaCert> caCerts = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED, IAPI.STATE_PUBLISHED, IAPI.STATE_DEPRECATED}, 
			propHandler = APIDescriptionPropertyHandler.class)
	protected String descriptionType = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED, IAPI.STATE_PUBLISHED, IAPI.STATE_DEPRECATED}, 
			propHandler = APIDescriptionPropertyHandler.class)
	protected String descriptionManual = null;
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED, IAPI.STATE_PUBLISHED, IAPI.STATE_DEPRECATED}, 
			propHandler = APIDescriptionPropertyHandler.class)
	protected String descriptionMarkdown = null;
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED, IAPI.STATE_PUBLISHED, IAPI.STATE_DEPRECATED}, 
			propHandler = APIDescriptionPropertyHandler.class)
	protected String descriptionUrl = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPI.STATE_UNPUBLISHED}, 
			propHandler = SecurityProfileHandler.class)
	@JsonSetter(nulls=Nulls.SKIP)
	protected List<SecurityProfile> securityProfiles = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPI.STATE_UNPUBLISHED}, 
			propHandler = AuthenticationProfileHandler.class)
	@JsonSetter(nulls=Nulls.SKIP)
	protected List<AuthenticationProfile> authenticationProfiles = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED}, 
			propHandler = APITagsPropertyHandler.class)
	protected TagMap<String, String[]> tags = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPI.STATE_UNPUBLISHED}, 
			propHandler = OutboundProfileHandler.class)
	protected Map<String, OutboundProfile> outboundProfiles = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPI.STATE_UNPUBLISHED}, 
			propHandler = ServiceProfileHandler.class)
	protected Map<String, ServiceProfile> serviceProfiles = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPI.STATE_UNPUBLISHED}, 
			propHandler = InboundProfileHandler.class)
	protected Map<String, InboundProfile> inboundProfiles = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPI.STATE_UNPUBLISHED}, 
			propHandler = CorsProfileHandler.class)
	protected List<CorsProfile> corsProfiles;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED, IAPI.STATE_PUBLISHED, IAPI.STATE_DEPRECATED})
	protected List<String> clientOrganizations;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED, IAPI.STATE_PUBLISHED, IAPI.STATE_DEPRECATED})
	@JsonSetter(nulls=Nulls.SKIP)
	protected List<ClientApplication> applications = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {}, 
			propHandler = APIPathPropertyHandler.class)
	protected String path = null;

	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED, IAPI.STATE_PUBLISHED, IAPI.STATE_DEPRECATED})
	protected String state = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED}, 
			propHandler = APIVersionPropertyHandler.class)	
	protected String version;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPI.STATE_UNPUBLISHED, IAPI.STATE_PUBLISHED, IAPI.STATE_DEPRECATED}, 
			propHandler = VhostPropertyHandler.class)
	protected String vhost = null;
	
	@APIPropertyAnnotation(isBreaking = false, writableStates = {IAPI.STATE_UNPUBLISHED}, propHandler = APINamePropertyHandler.class)
	protected String name = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED, IAPI.STATE_DEPRECATED}, 
			propHandler = APISummaryPropertyHandler.class)
	protected String summary = null;

	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED, IAPI.STATE_PUBLISHED, IAPI.STATE_DEPRECATED})
	protected APIImage image = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED, IAPI.STATE_PUBLISHED, IAPI.STATE_DEPRECATED}, 
			propHandler = CustomPropertyHandler.class)
	protected Map<String, String> customProperties = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED, IAPI.STATE_PUBLISHED, IAPI.STATE_DEPRECATED})	
	protected APIQuota applicationQuota = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED, IAPI.STATE_PUBLISHED, IAPI.STATE_DEPRECATED})
	protected APIQuota systemQuota = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPI.STATE_UNPUBLISHED}, propHandler = RoutingKeyPropHandler.class)
	protected String apiRoutingKey = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {})
	protected String organization = null;
	
	protected String organizationId = null;
	
	protected String id = null;
	
	protected String apiId = null;
	
	protected String deprecated = null;

	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPI.STATE_UNPUBLISHED, IAPI.STATE_PUBLISHED, IAPI.STATE_DEPRECATED})
	protected Long retirementDate = null;
	
	protected boolean isValid = false;
	
	protected String orgId = null;
	
	protected List<APIMethod> apiMethods = null;
	

	public boolean isValid() {
		return this.isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public APIDefintion getAPIDefinition() {
		return this.APIDefinition;
	}
	
	public void setAPIDefinition(APIDefintion APIDefinition) {
		this.APIDefinition = APIDefinition;
	}

	public Map<String, OutboundProfile> getOutboundProfiles() {
		return this.outboundProfiles;
	}
	
	public void setOutboundProfiles(Map<String, OutboundProfile> outboundProfiles) {
		this.outboundProfiles = outboundProfiles;
	}

	public List<SecurityProfile> getSecurityProfiles() {
		return this.securityProfiles;
	}
	
	public void setSecurityProfiles(List<SecurityProfile> securityProfiles) {
		this.securityProfiles = securityProfiles;
	}

	public List<AuthenticationProfile> getAuthenticationProfiles() {
		return authenticationProfiles;
	}

	public void setAuthenticationProfiles(List<AuthenticationProfile> authenticationProfiles) {
		this.authenticationProfiles = authenticationProfiles;
	}

	public Map<String, InboundProfile> getInboundProfiles() {
		return this.inboundProfiles;
	}

	public void setInboundProfiles(Map<String, InboundProfile> inboundProfiles) {
		this.inboundProfiles = inboundProfiles;
	}

	public List<CorsProfile> getCorsProfiles() {
		return corsProfiles;
	}
	
	public void setCorsProfiles(List<CorsProfile> corsProfiles) {
		this.corsProfiles = corsProfiles;
	}

	public String getVhost() {
		return vhost;
	}

	public void setVhost(String vhost) {
		this.vhost = vhost;
	}

	public TagMap<String, String[]> getTags() {
		return tags;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	public String getState() throws AppException {
		return this.state;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public APIImage getImage() {
		return image;
	}

	public void setImage(APIImage image) {
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public String getDeprecated() {
		return deprecated;
	}

	public void setDeprecated(String deprecated) {
		this.deprecated = deprecated;
	}

	public Long getRetirementDate() {
		return retirementDate;
	}

	public void setRetirementDate(Long retirementDate) {
		this.retirementDate = retirementDate;
	}

	public Map<String, String> getCustomProperties() {
		return customProperties;
	}

	public void setCustomProperties(Map<String, String> customProperties) {
		this.customProperties = customProperties;
	}

	public String getDescriptionType() {
		return descriptionType;
	}

	public void setDescriptionType(String descriptionType) {
		this.descriptionType = descriptionType;
	}

	public String getDescriptionManual() {
		return descriptionManual;
	}

	public void setDescriptionManual(String descriptionManual) {
		this.descriptionManual = descriptionManual;
	}

	public String getDescriptionMarkdown() {
		return descriptionMarkdown;
	}

	public void setDescriptionMarkdown(String descriptionMarkdown) {
		this.descriptionMarkdown = descriptionMarkdown;
	}

	public String getDescriptionUrl() {
		return descriptionUrl;
	}

	public void setDescriptionUrl(String descriptionUrl) {
		this.descriptionUrl = descriptionUrl;
	}

	public List<CaCert> getCaCerts() {
		return caCerts;
	}

	public void setCaCerts(List<CaCert> caCerts) {
		this.caCerts = caCerts;
	}
	
	public APIQuota getApplicationQuota() {
		return applicationQuota;
	}

	public void setApplicationQuota(APIQuota applicationQuota) {
		if(applicationQuota!=null && applicationQuota.getType()==null) applicationQuota.setType("APPLICATION");
		this.applicationQuota = applicationQuota;
	}

	public APIQuota getSystemQuota() {
		return systemQuota;
	}

	public void setSystemQuota(APIQuota systemQuota) {
		if(systemQuota!=null && systemQuota.getType()==null) systemQuota.setType("SYSTEM");
		this.systemQuota = systemQuota;
	}

	public Map<String, ServiceProfile> getServiceProfiles() {
		return serviceProfiles;
	}

	public List<String> getClientOrganizations() {
		return clientOrganizations;
	}

	public void setClientOrganizations(List<String> clientOrganizations) {
		this.clientOrganizations = clientOrganizations;
	}

	public List<ClientApplication> getApplications() {
		return applications;
	}

	public void setApplications(List<ClientApplication> applications) {
		this.applications = applications;
	}
	
	public String getApiRoutingKey() {
		return apiRoutingKey;
	}

	public void setApiRoutingKey(String apiRoutingKey) {
		this.apiRoutingKey = apiRoutingKey;
	}

	public List<APIMethod> getApiMethods() {
		return apiMethods;
	}

	public void setApiMethods(List<APIMethod> apiMethods) {
		this.apiMethods = apiMethods;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [path=" + path + ", id (FE-API)=" + id + ", apiId (BE-API)=" + apiId + "]";
	}

	public int getAPIType() {
		/*if(this.getAPIDefinition().getAPIDefinitionFile().endsWith("?wsdl")) {
			return IAPIDefinition.WSDL_API;
		} else {
			return IAPIDefinition.SWAGGGER_API;
		}*/
		return 0;
	}
}
