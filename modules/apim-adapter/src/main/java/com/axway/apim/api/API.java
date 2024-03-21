package com.axway.apim.api;

import java.util.List;
import java.util.Map;

import com.axway.apim.adapter.jackson.MarkdownLocalDeserializer;
import com.axway.apim.adapter.jackson.OrganizationDeserializer;
import com.axway.apim.adapter.jackson.RemotehostDeserializer;
import com.axway.apim.api.specification.APISpecification;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.AuthenticationProfile;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.api.model.CorsProfile;
import com.axway.apim.api.model.CustomPropertiesEntity;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.InboundProfile;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.api.model.SecurityProfile;
import com.axway.apim.api.model.ServiceProfile;
import com.axway.apim.api.model.TagMap;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * This class defines all common properties on an API and how each property should be
 * treated during replication.
 * APIManagerAPI and APIImportDefinition are both an instance of this class.
 * <p>
 * Annotations for each property are used by the APIChangeState to decide:
 * - Is it a breaking change?
 * - Can the change be applied to the existing API?
 * - Which Change-Handler should finally do the required actions to replicate the change into the APIManager
 * <p>
 * When adding new properties, please make sure to create Getter and Setter as Jackson is used to create the Instances.
 * <p>
 * Perhaps a way to simplify the code is to use for many of the properties is to use a SimplePropertyHandler
 * as many properties are handled in the same way.
 * <p>
 *
 * @author cwiechmann@axway.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("APIFilter")
public class API implements CustomPropertiesEntity {

    public static final String STATE_PUBLISHED = "published";
    public static final String STATE_UNPUBLISHED = "unpublished";
    public static final String STATE_DEPRECATED = "deprecated";
    public static final String STATE_DELETED = "deleted";
    public static final String STATE_PENDING = "pending";

    JsonNode apiConfiguration;

    @JsonIgnore
    private boolean requestForAllOrgs = false;

    @APIPropertyAnnotation(isBreaking = true, writableStates = {}, isRecreate = true)
    protected APISpecification apiDefinition = null;

    @APIPropertyAnnotation(isBreaking = true, writableStates = {API.STATE_UNPUBLISHED})
    protected List<CaCert> caCerts = null;

    @APIPropertyAnnotation(writableStates = {API.STATE_UNPUBLISHED, API.STATE_PUBLISHED, API.STATE_DEPRECATED})
    protected String descriptionType = null;

    @APIPropertyAnnotation(writableStates = {API.STATE_UNPUBLISHED, API.STATE_PUBLISHED, API.STATE_DEPRECATED})
    protected String descriptionManual = null;

    @APIPropertyAnnotation(writableStates = {API.STATE_UNPUBLISHED, API.STATE_PUBLISHED, API.STATE_DEPRECATED})
    protected String descriptionMarkdown = null;

    @APIPropertyAnnotation(writableStates = {API.STATE_UNPUBLISHED, API.STATE_PUBLISHED, API.STATE_DEPRECATED})
    protected String descriptionUrl = null;

    @JsonDeserialize(using = MarkdownLocalDeserializer.class)
    protected List<String> markdownLocal = null;

    @APIPropertyAnnotation(isBreaking = true, writableStates = {API.STATE_UNPUBLISHED})
    @JsonSetter(nulls = Nulls.SKIP)
    protected List<SecurityProfile> securityProfiles = null;

    @APIPropertyAnnotation(isBreaking = true, writableStates = {API.STATE_UNPUBLISHED})
    @JsonSetter(nulls = Nulls.SKIP)
    protected List<AuthenticationProfile> authenticationProfiles = null;

    @APIPropertyAnnotation(writableStates = {API.STATE_UNPUBLISHED})
    protected TagMap tags = null;

    @APIPropertyAnnotation(isBreaking = true, writableStates = {API.STATE_UNPUBLISHED})
    protected Map<String, OutboundProfile> outboundProfiles = null;

    @APIPropertyAnnotation(copyProp = false, isBreaking = true, writableStates = {API.STATE_UNPUBLISHED})
    protected Map<String, ServiceProfile> serviceProfiles = null;

    @APIPropertyAnnotation(isBreaking = true, writableStates = {API.STATE_UNPUBLISHED})
    protected Map<String, InboundProfile> inboundProfiles = null;

    @APIPropertyAnnotation(isBreaking = true, writableStates = {API.STATE_UNPUBLISHED})
    protected List<CorsProfile> corsProfiles;

    @APIPropertyAnnotation(copyProp = false, writableStates = {API.STATE_UNPUBLISHED, API.STATE_PUBLISHED, API.STATE_DEPRECATED})
    protected List<Organization> clientOrganizations;

    @APIPropertyAnnotation(copyProp = false,
        writableStates = {API.STATE_UNPUBLISHED, API.STATE_PUBLISHED, API.STATE_DEPRECATED})
    @JsonSetter(nulls = Nulls.SKIP)
    protected List<ClientApplication> applications = null;

    @APIPropertyAnnotation(isBreaking = true, writableStates = {})
    protected String path = null;

    @APIPropertyAnnotation(copyProp = false,
        writableStates = {API.STATE_UNPUBLISHED, API.STATE_PUBLISHED, API.STATE_DEPRECATED})
    protected String state = null;

    @APIPropertyAnnotation(writableStates = {API.STATE_UNPUBLISHED})
    protected String version;

    @APIPropertyAnnotation(isBreaking = true, writableStates = {API.STATE_UNPUBLISHED, API.STATE_PUBLISHED, API.STATE_DEPRECATED}, ignoreNull = false)
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    protected String vhost = null;

    @APIPropertyAnnotation(writableStates = {API.STATE_UNPUBLISHED})
    protected String name = null;

    @APIPropertyAnnotation(writableStates = {API.STATE_UNPUBLISHED, API.STATE_DEPRECATED})
    protected String summary = null;

    protected Long createdOn = null;

    protected String createdBy = null;

    @APIPropertyAnnotation(
        writableStates = {API.STATE_UNPUBLISHED, API.STATE_PUBLISHED, API.STATE_DEPRECATED})
    protected Image image = null;

    @APIPropertyAnnotation(
        writableStates = {API.STATE_UNPUBLISHED})
    protected Map<String, String> customProperties = null;

    @APIPropertyAnnotation(copyProp = false,
        writableStates = {API.STATE_UNPUBLISHED, API.STATE_PUBLISHED, API.STATE_DEPRECATED})
    protected APIQuota applicationQuota = null;

    @APIPropertyAnnotation(copyProp = false,
        writableStates = {API.STATE_UNPUBLISHED, API.STATE_PUBLISHED, API.STATE_DEPRECATED})
    protected APIQuota systemQuota = null;

    @APIPropertyAnnotation(isBreaking = true,
        writableStates = {API.STATE_UNPUBLISHED})
    protected String apiRoutingKey = null;

    @APIPropertyAnnotation(writableStates = {}, isRecreate = true)
    @JsonDeserialize(using = OrganizationDeserializer.class)
    @JsonAlias({"organizationId", "organization"})
    // Alias to read Organization based on the id as given by the API-Manager
    protected Organization organization = null;

    protected String id = null;

    protected String apiId = null;

    protected String deprecated = null;

    @JsonIgnore
    protected String backendImportedUrl;

    @JsonIgnore
    protected String resourcePath = null;

    @APIPropertyAnnotation(copyProp = false,
        writableStates = {API.STATE_UNPUBLISHED, API.STATE_PUBLISHED, API.STATE_DEPRECATED})
    protected Long retirementDate = null;

    @JsonDeserialize(using = RemotehostDeserializer.class)
    protected RemoteHost remoteHost = null;

    @APIPropertyAnnotation(writableStates = {API.STATE_UNPUBLISHED})
    protected List<APIMethod> apiMethods = null;

    public APISpecification getApiDefinition() {
        return apiDefinition;
    }

    public void setApiDefinition(APISpecification apiDefinition) {
        this.apiDefinition = apiDefinition;
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

    public TagMap getTags() {
        return tags;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The tool handles deprecation as an additional state (might not be best choice), but
     * the API-Manager internally doesn't. In API-Manager deprecation is just a true/false toggle.
     * To make Desired and Actual API comparable this method is encapsulating the difference.
     *
     * @return the state of the API (unpublished, deprecated, etc.)
     * @see com.axway.apim.api.API#getState()
     */
    public String getState() {
        if (this.deprecated != null
            && this.deprecated.equals("true")) return STATE_DEPRECATED;
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

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Organization getOrganization() {
        return organization;
    }

    public String getOrganizationId() {
        if (organization != null) return organization.getId();
        return null;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
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

    public List<String> getMarkdownLocal() {
        return markdownLocal;
    }

    public void setMarkdownLocal(List<String> markdownLocal) {
        this.markdownLocal = markdownLocal;
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
        if (applicationQuota != null && applicationQuota.getType() == null) applicationQuota.setType("APPLICATION");
        this.applicationQuota = applicationQuota;
    }

    public APIQuota getSystemQuota() {
        return systemQuota;
    }

    public void setSystemQuota(APIQuota systemQuota) {
        if (systemQuota != null && systemQuota.getType() == null) systemQuota.setType("SYSTEM");
        this.systemQuota = systemQuota;
    }

    public Map<String, ServiceProfile> getServiceProfiles() {
        return serviceProfiles;
    }

    public void setTags(TagMap tags) {
        this.tags = tags;
    }

    public void setServiceProfiles(Map<String, ServiceProfile> serviceProfiles) {
        this.serviceProfiles = serviceProfiles;
    }

    public List<Organization> getClientOrganizations() {
        return clientOrganizations;
    }

    public void setClientOrganizations(List<Organization> clientOrganizations) {
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
        return "API{" +
            "path='" + path + '\'' +
            ", state='" + state + '\'' +
            ", version='" + version + '\'' +
            ", vhost='" + vhost + '\'' +
            ", name='" + name + '\'' +
            ", apiRoutingKey='" + apiRoutingKey + '\'' +
            ", id='" + id + '\'' +
            ", apiId='" + apiId + '\'' +
            '}';
    }

    public String toStringHuman() {
        return getName() + " (" + getVersion() + ") exposed on path: " + getPath();
    }

    public String getApiDefinitionImport() {
        return null;
    }

    public JsonNode getApiConfiguration() {
        return apiConfiguration;
    }

    public void setApiConfiguration(JsonNode apiConfiguration) {
        this.apiConfiguration = apiConfiguration;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public RemoteHost getRemotehost() {
        return remoteHost;
    }

    public void setRemotehost(RemoteHost remotehost) {
        this.remoteHost = remotehost;
    }

    /**
     * @return path of the resource registered for the belonging backend API or null if not set
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * @param resourcePath is the path of the resource registered for the belonging backend API
     */
    public void setBackendResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * requestForAllOrgs is used to decide if an API should grant access to ALL organizations.
     * That means, when an API-Developer is defining ALL as the organization name this flag
     * is set to true and it becomes the desired state.
     *
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
     *
     * @param requestForAllOrgs when set to true, the APIs will be granted to ALL organizations.
     */
    public void setRequestForAllOrgs(boolean requestForAllOrgs) {
        this.requestForAllOrgs = requestForAllOrgs;
    }

    public String getBackendImportedUrl() {
        return backendImportedUrl;
    }

    public void setBackendImportedUrl(String backendImportedUrl) {
        this.backendImportedUrl = backendImportedUrl;
    }
}
