package com.axway.apim.swagger.api;

import java.util.List;
import java.util.Map;

import com.axway.apim.actions.tasks.props.APINamePropertyHandler;
import com.axway.apim.actions.tasks.props.APIPathPropertyHandler;
import com.axway.apim.actions.tasks.props.APISummaryPropertyHandler;
import com.axway.apim.actions.tasks.props.APITagsPropertyHandler;
import com.axway.apim.actions.tasks.props.CorsProfileHandler;
import com.axway.apim.actions.tasks.props.InboundProfileHandler;
import com.axway.apim.actions.tasks.props.OutboundProfileHandler;
import com.axway.apim.actions.tasks.props.SecurityProfileHandler;
import com.axway.apim.actions.tasks.props.VhostPropertyHandler;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.properties.APIImage;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
import com.axway.apim.swagger.api.properties.corsprofiles.CorsProfile;
import com.axway.apim.swagger.api.properties.inboundprofiles.InboundProfile;
import com.axway.apim.swagger.api.properties.outboundprofiles.OutboundProfile;
import com.axway.apim.swagger.api.properties.securityprofiles.SecurityProfile;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractAPIDefinition {
	
	protected CommandParameters cmd = CommandParameters.getInstance();
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	@APIPropertyAnnotation(isBreaking = true, writableStates = {})
	protected APISwaggerDefinion swaggerDefinition = null;

	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {}, 
			propHandler = SecurityProfileHandler.class)
	protected List<SecurityProfile> securityProfiles = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED}, 
			propHandler = OutboundProfileHandler.class)
	protected Map<String, OutboundProfile> outboundProfiles = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED}, 
			propHandler = APITagsPropertyHandler.class)
	protected Map<String, String[]> tags = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED}, 
			propHandler = InboundProfileHandler.class)
	protected Map<String, InboundProfile> inboundProfiles = null;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED}, 
			propHandler = CorsProfileHandler.class)
	protected List<CorsProfile> corsProfiles;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {}, 
			propHandler = APIPathPropertyHandler.class)
	protected String path = null;

	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED, IAPIDefinition.STATE_PUBLISHED, IAPIDefinition.STATE_DEPRECATED})
	protected String state = null;
	
	@APIPropertyAnnotation(isBreaking = false, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED})	
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
}
