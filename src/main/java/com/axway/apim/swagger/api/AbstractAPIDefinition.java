package com.axway.apim.swagger.api;

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
import com.axway.apim.actions.tasks.props.SecurityProfileHandler;
import com.axway.apim.actions.tasks.props.ServiceProfileHandler;
import com.axway.apim.actions.tasks.props.VhostPropertyHandler;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.properties.APIImage;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
import com.axway.apim.swagger.api.properties.applications.ClientApplication;
import com.axway.apim.swagger.api.properties.authenticationProfiles.AuthenticationProfile;
import com.axway.apim.swagger.api.properties.cacerts.CaCert;
import com.axway.apim.swagger.api.properties.corsprofiles.CorsProfile;
import com.axway.apim.swagger.api.properties.inboundprofiles.InboundProfile;
import com.axway.apim.swagger.api.properties.outboundprofiles.OutboundProfile;
import com.axway.apim.swagger.api.properties.profiles.ServiceProfile;
import com.axway.apim.swagger.api.properties.quota.APIQuota;
import com.axway.apim.swagger.api.properties.securityprofiles.SecurityProfile;
import com.axway.apim.swagger.api.properties.tags.TagMap;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class defines all common properties on an API and how each property should be 
 * treated during replication.</br>
 * APIManagerAPI & APIImportDefintion are both an instance of this class.</br>
 * </br>
 * Annotations for each property are used by the APIChangeState to decide:
 * <li>Is is a breaking change?</li>
 * <li>Can the change be applied to the existing API?</li>
 * <li>Which Change-Handler should finally do the required actions to replicate the change into the APIManager</li>
 * </br>
 * When adding new properties, please make sure to create Getter & Setter as Jackson is used to create the Instances.
 * </br></br>
 * Perhaps a way to simplify the code is to use for many of the properties is to use a SimplePropertyHandler 
 * as many properties are handled in the same way.
 * 
 * 
 * @author cwiechmann@axway.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractAPIDefinition {
	
	protected CommandParameters cmd = CommandParameters.getInstance();
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	@APIPropertyAnnotation(isBreaking = true, writableStates = {})
	protected APISwaggerDefinion swaggerDefinition = null;

	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED}, 
			propHandler = APICaCertsPropertyHandler.class)
	protected List<CaCert> caCerts = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED}, 
			propHandler = APIDescriptionPropertyHandler.class)
	protected String descriptionType = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED}, 
			propHandler = APIDescriptionPropertyHandler.class)
	protected String descriptionManual = null;
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED}, 
			propHandler = APIDescriptionPropertyHandler.class)
	protected String descriptionMarkdown = null;
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED}, 
			propHandler = APIDescriptionPropertyHandler.class)
	protected String descriptionUrl = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {}, 
			propHandler = SecurityProfileHandler.class)
	@JsonSetter(nulls=Nulls.SKIP)
	protected List<SecurityProfile> securityProfiles = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {}, 
			propHandler = AuthenticationProfileHandler.class)
	@JsonSetter(nulls=Nulls.SKIP)
	protected List<AuthenticationProfile> authenticationProfiles = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED}, 
			propHandler = APITagsPropertyHandler.class)
	protected TagMap<String, String[]> tags = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED}, 
			propHandler = OutboundProfileHandler.class)
	protected Map<String, OutboundProfile> outboundProfiles = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED}, 
			propHandler = ServiceProfileHandler.class)
	protected Map<String, ServiceProfile> serviceProfiles = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED}, 
			propHandler = InboundProfileHandler.class)
	protected Map<String, InboundProfile> inboundProfiles = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED}, 
			propHandler = CorsProfileHandler.class)
	protected List<CorsProfile> corsProfiles;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED})
	protected List<String> clientOrganizations;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED})
	@JsonSetter(nulls=Nulls.SKIP)
	protected List<ClientApplication> applications = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {}, 
			propHandler = APIPathPropertyHandler.class)
	protected String path = null;

	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED})
	protected String state = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED}, 
			propHandler = APIVersionPropertyHandler.class)	
	protected String version;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED}, 
			propHandler = VhostPropertyHandler.class)
	protected String vhost = null;
	
	@APIPropertyAnnotation(isBreaking = false, writableStates = {IAPIDefinition.STATE_UNPUBLISHED}, propHandler = APINamePropertyHandler.class)
	protected String name = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED}, 
			propHandler = APISummaryPropertyHandler.class)
	protected String summary = null;

	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED})
	protected APIImage image = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED}, 
			propHandler = CustomPropertyHandler.class)
	protected Map<String, String> customProperties = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED})	
	protected APIQuota applicationQuota = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED})
	protected APIQuota systemQuota = null;
	
	protected String organization = null;
	
	protected String organizationId = null;
	
	protected String id = null;
	
	protected String apiId = null;
	
	protected String deprecated = null;
	
	protected boolean isValid = false;
	

	public boolean isValid() {
		return this.isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public String getOrgId() throws AppException {
		throw new AppException("This method must be implemented by concrete class.", ErrorCode.UNSUPPORTED_FEATURE);
	}
	
	public APISwaggerDefinion getSwaggerDefinition() {
		return this.swaggerDefinition;
	}
	
	public void setSwaggerDefinition(APISwaggerDefinion swaggerDefinition) {
		this.swaggerDefinition = swaggerDefinition;
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

	public String getVhost() {
		return vhost;
	}

	public void setVhost(String vhost) {
		this.vhost = vhost;
	}

	public Map<String, String[]> getTags() {
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
}
