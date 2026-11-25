package com.axway.apim.adapter.client.apps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.axway.apim.adapter.apis.FilterHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.User;
import com.axway.apim.api.model.apps.APIKey;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.api.model.apps.ExtClients;
import com.axway.apim.api.model.apps.OAuth;
import com.axway.apim.lib.CustomPropertiesFilter;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;

public class ClientAppFilter implements CustomPropertiesFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ClientAppFilter.class);
    public static final String FIELD = "field";
    public static final String VALUE = "value";
    public static final List<String> SUPPORTED_CREDENTIAL_TYPES = Collections.unmodifiableList(Arrays.asList("oauth", "apikeys", "extclients"));

    boolean includeQuota;

    boolean includeAppPermissions;

    boolean includeCredentials;

    boolean includeAPIAccess;

    boolean includeImage;

    private String credential;

    private String redirectUrl;

    /**
     * Credential types (oauth, apikeys, extclients), of which an application must have at least one. Null or empty means no restriction.
     */
    private Set<String> credentialTypes;

    private String applicationName;

    private String state;

    private Organization organization;

    private User createdBy;

    private String applicationId;

    private String apiName;

    boolean includeOauthResources;

    private List<String> customProperties;
    private String organizationId; // To support multi org


    List<NameValuePair> filters = new ArrayList<>();

    private ClientAppFilter() {
    }

    public boolean isIncludeQuota() {
        return includeQuota;
    }

    public boolean isIncludeAppPermissions() {
        return includeAppPermissions;
    }

    public void setIncludeAppPermissions(boolean includeAppPermissions) {
        this.includeAppPermissions = includeAppPermissions;
    }

    public boolean isIncludeCredentials() {
        return includeCredentials;
    }

    public boolean isIncludeImage() {
        return includeImage;
    }

    public boolean isIncludeAPIAccess() {
        return includeAPIAccess;
    }

    public void setIncludeQuota(boolean includeQuota) {
        this.includeQuota = includeQuota;
    }

    public void setIncludeCredentials(boolean includeCredentials) {
        this.includeCredentials = includeCredentials;
    }

    public void setIncludeAPIAccess(boolean includeAPIAccess) {
        this.includeAPIAccess = includeAPIAccess;
    }

    public void setIncludeImage(boolean includeImage) {
        this.includeImage = includeImage;
    }


    public boolean isIncludeOauthResources() {
        return includeOauthResources;
    }

    public void setIncludeOauthResources(boolean includeOauthResources) {
        this.includeOauthResources = includeOauthResources;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public List<NameValuePair> getFilters() {
        return filters;
    }

    public void setOrganization(Organization organization) {
        if (organization == null) return;
        this.organization = organization;
        filters.add(new BasicNameValuePair(FIELD, "orgid"));
        filters.add(new BasicNameValuePair("op", "eq"));
        filters.add(new BasicNameValuePair(VALUE, organization.getId()));
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setCreatedBy(User createdBy) {
        if (createdBy == null) return;
        this.createdBy = createdBy;
        filters.add(new BasicNameValuePair(FIELD, "userid"));
        filters.add(new BasicNameValuePair("op", "eq"));
        filters.add(new BasicNameValuePair(VALUE, createdBy.getId()));
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public List<String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(List<String> customProperties) {
        this.customProperties = customProperties;
    }

    public void setApplicationName(String applicationName) {
        if (applicationName == null) return;
        // All applications are requested - We ignore this filter
        if (applicationName.equals("*")) return;
        this.applicationName = applicationName;
        FilterHelper.setFilter(applicationName, filters);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public Set<String> getCredentialTypes() {
        return credentialTypes;
    }

    public void setCredentialTypes(Set<String> credentialTypes) {
        this.credentialTypes = credentialTypes;
    }

    private boolean hasCredentialTypeFilter() {
        return credentialTypes != null && !credentialTypes.isEmpty();
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public void setState(String state) {
        if (state == null) return;
        this.state = state;
        filters.add(new BasicNameValuePair(FIELD, "state"));
        filters.add(new BasicNameValuePair("op", "eq"));
        filters.add(new BasicNameValuePair(VALUE, state));
    }

    public String getState() {
        return state;
    }

    void useFilter(List<NameValuePair> filter) {
        this.filters.addAll(filter);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof ClientAppFilter)) return false;
        ClientAppFilter other = (ClientAppFilter) obj;
        return (
            Strings.CS.equals(other.getApplicationName(), this.getApplicationName()) &&
                        Strings.CS.equals(other.getState(), this.getState()) &&
                        (other.getOrganization() == null || other.getOrganization().equals(this.getOrganization()))
        );
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode += (this.state != null) ? this.state.hashCode() : 0;
        hashCode += (this.applicationName != null) ? this.applicationName.hashCode() : 0;
        return hashCode;
    }

    @Override
    public String toString() {
        if (LOG.isDebugEnabled()) {
            return "ClientAppFilter [includeQuota=" + includeQuota + ", includeCredentials=" + includeCredentials
                    + ", includeAPIAccess=" + includeAPIAccess + ", includeImage=" + includeImage + ", applicationName="
                    + applicationName + ", state=" + state + ", organization=" + organization + ", applicationId="
                    + applicationId + ", filters=" + filters + "]";
        } else {
            return "ClientAppFilter [applicationName=" + applicationName + ", state=" + state + ", organization="
                    + organization + ", applicationId=" + applicationId + "]";
        }
    }

    public boolean filter(ClientApplication app) {
        boolean credentialFilter = this.getCredential() != null || this.getRedirectUrl() != null || hasCredentialTypeFilter();
        if (!credentialFilter && this.getApiName() == null) { // Nothing given to filter out.
            return false;
        }
        // Filter this app, if none of its credentials matches
        if (credentialFilter && !hasMatchingCredential(app)) {
            return true;
        }
        // Filter this app, if none of its APIs matches
        if (this.getApiName() != null) {
            return !hasMatchingApiAccess(app);
        }
        // Nothing filtered this application!
        return false;
    }

    private boolean hasMatchingCredential(ClientApplication app) {
        // An app without any credentials can't match
        if (app.getCredentials() == null) return false;
        for (ClientAppCredential cred : app.getCredentials()) {
            // Ignore credentials of a type, which is not requested
            if (hasCredentialTypeFilter() && !credentialTypes.contains(cred.getCredentialType())) continue;
            if (credentialMatches(cred, app.getName())) return true;
        }
        return false;
    }

    private boolean credentialMatches(ClientAppCredential cred, String appName) {
        switch (cred.getCredentialType()) {
            case "oauth":
                return filterCredential(((OAuth) cred).getClientId(), ((OAuth) cred).getRedirectUrls(), appName);
            case "extclients":
                return filterCredential(((ExtClients) cred).getClientId(), null, appName);
            case "apikeys":
                return filterCredential(((APIKey) cred).getApiKey(), null, appName);
            default:
                return false;
        }
    }

    private boolean hasMatchingApiAccess(ClientApplication app) {
        // App has no access to any API
        if (app.getApiAccess() == null || app.getApiAccess().isEmpty()) return false;
        Pattern pattern = Pattern.compile(this.apiName.replace("*", ".*"));
        for (APIAccess apiAccess : app.getApiAccess()) {
            if (pattern.matcher(apiAccess.getApiName()).matches()) return true;
        }
        return false;
    }

    private boolean filterCredential(String appCredential, String[] appRedirectUrls, String appName) {
        if (appCredential == null) {
            LOG.warn("Inconsistent application: {} found. API-Key/Client-ID is NULL for credential.", appName);
            return false;
        }
        if (this.credential != null) {
            Pattern pattern = Pattern.compile(this.credential.replace("*", ".*"));
            Matcher matcher = pattern.matcher(appCredential);
            if (!matcher.matches()) return false;
        }
        if (this.redirectUrl != null) {
            if (appRedirectUrls == null || appRedirectUrls.length == 0) return false;
            Pattern pattern = Pattern.compile(this.redirectUrl.replace("*", ".*"));
            boolean redirectUrlMatch = false;
            for (String appRedirectUrl : appRedirectUrls) {
                Matcher matcher = pattern.matcher(appRedirectUrl);
                if (matcher.matches()) {
                    redirectUrlMatch = true;
                    break;
                }
            }
            return redirectUrlMatch;
        }
        return true;
    }


    /**
     * Build an applicationAdapter based on the given configuration
     */
    public static class Builder {

        private boolean includeQuota;

        private boolean includeAppPermissions;

        private boolean includeCredentials;

        private boolean includeImage;

        private boolean includeAPIAccess;

        private Organization organization;

        private User createdBy;

        /**
         * The name of the application
         */
        private String applicationName;

        private String applicationId;

        private String state;

        private String credential;

        private String redirectUrl;

        private Set<String> credentialTypes;

        private boolean includeOauthResources;

        private String apiName;

        private List<String> customProperties;

        public Builder() {
            super();
        }

        /**
         * Creates a ClientAppAdapter based on the provided configuration using all registered Adapters
         *
         * @return ClientAppFilter used to filder for Client-Apps
         */
        public ClientAppFilter build() {
            ClientAppFilter filter = new ClientAppFilter();
            filter.setApplicationId(this.applicationId);
            filter.setApplicationName(this.applicationName);
            filter.setOrganization(this.organization);
            filter.setCreatedBy(this.createdBy);
            filter.setState(this.state);
            filter.setIncludeQuota(this.includeQuota);
            filter.setIncludeAppPermissions(this.includeAppPermissions);
            filter.setIncludeCredentials(this.includeCredentials);
            filter.setIncludeImage(this.includeImage);
            filter.setIncludeAPIAccess(this.includeAPIAccess);
            filter.setCredential(this.credential);
            filter.setRedirectUrl(this.redirectUrl);
            filter.setCredentialTypes(this.credentialTypes);
            filter.setIncludeOauthResources(this.includeOauthResources);
            filter.setCustomProperties(this.customProperties);
            filter.setApiName(this.apiName);
            return filter;
        }

        public Builder hasName(String name) throws AppException {
            if (name == null) return this;
            if (name.contains("|")) {
                Organization org = APIManagerAdapter.getInstance().getOrgAdapter().getOrgForName(name.substring(name.indexOf("|") + 1));
                hasOrganization(org);
                this.applicationName = name.substring(0, name.indexOf("|"));
            } else {
                this.applicationName = name;
            }
            return this;
        }

        public Builder hasId(String id) {
            this.applicationId = id;
            return this;
        }

        public Builder hasOrganizationId(String organizationId) {
            if (organizationId == null) return this;
            Organization org = new Organization();
            org.setId(organizationId);
            return hasOrganization(org);
        }

        public Builder hasOrganizationName(String organizationName) throws AppException {
            if (organizationName == null) return this;
            Organization org = APIManagerAdapter.getInstance().getOrgAdapter().getOrgForName(organizationName);
            if (org == null) {
                throw new AppException("The organization with name: '" + organizationName + "' is unknown.", ErrorCode.UNKNOWN_ORGANIZATION);
            }
            return hasOrganization(org);
        }

        public Builder hasOrganization(Organization organization) {
            this.organization = organization;
            return this;
        }

        public Builder hasCreatedByLoginName(String loginName) throws AppException {
            if (loginName == null) return this;
            User user = APIManagerAdapter.getInstance().getUserAdapter().getUserForLoginName(loginName);
            if (user == null) {
                throw new AppException("The user with login name: '" + loginName + "' is unknown.", ErrorCode.UNKNOWN_USER);
            }
            return hasCreatedBy(user);
        }

        public Builder hasCreatedBy(User user) {
            this.createdBy = user;
            return this;
        }

        public Builder hasState(String state) {
            this.state = state;
            return this;
        }

        public Builder hasCredential(String credential) {
            this.credential = credential;
            return this;
        }

        public Builder hasRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
            return this;
        }

        /**
         * Restricts the applications to those having at least one credential of the given type(s).
         *
         * @param credentialTypes comma separated list of credential types: oauth, apikeys, extclients (case-insensitive). Null or empty means no restriction.
         * @return the builder
         * @throws AppException if an unsupported credential type is given
         */
        public Builder hasCredentialType(String credentialTypes) throws AppException {
            if (StringUtils.isBlank(credentialTypes)) return this;
            Set<String> types = new LinkedHashSet<>();
            for (String type : credentialTypes.split(",")) {
                String normalized = type.trim().toLowerCase();
                if (normalized.isEmpty()) continue;
                if (!SUPPORTED_CREDENTIAL_TYPES.contains(normalized)) {
                    throw new AppException("Unsupported credential type: '" + type.trim() + "'. Supported types are: " + String.join(", ", SUPPORTED_CREDENTIAL_TYPES), ErrorCode.INVALID_PARAMETER);
                }
                types.add(normalized);
            }
            this.credentialTypes = types.isEmpty() ? null : types;
            return this;
        }

        public Builder includeQuotas(boolean includeQuota) {
            this.includeQuota = includeQuota;
            return this;
        }

        public Builder includeAppPermissions(boolean includeAppPermissions) {
            this.includeAppPermissions = includeAppPermissions;
            return this;
        }

        public Builder includeCredentials(boolean includeCredentials) {
            this.includeCredentials = includeCredentials;
            return this;
        }

        public Builder includeImage(boolean includeImage) {
            this.includeImage = includeImage;
            return this;
        }

        public Builder includeAPIAccess(boolean includeAPIAccess) {
            this.includeAPIAccess = includeAPIAccess;
            return this;
        }

        public Builder includeOauthResources(boolean includeOauthResources) {
            this.includeOauthResources = includeOauthResources;
            return this;
        }

        public Builder includeCustomProperties(List<String> customProperties) {
            this.customProperties = customProperties;
            return this;
        }

        public Builder hasApiName(String apiName) {
            this.apiName = apiName;
            return this;
        }
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        if (organizationId == null) return;
        filters.add(new BasicNameValuePair("organizationId", organizationId));
    }
}
