package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter.Builder.APIType;
import com.axway.apim.api.API;
import com.axway.apim.api.model.*;
import com.axway.apim.lib.CustomPropertiesFilter;
import com.axway.apim.lib.utils.Utils;
import com.axway.apim.lib.utils.Utils.FedKeyType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIFilter implements CustomPropertiesFilter {

    private static final Logger LOG = LoggerFactory.getLogger(APIFilter.class);
    private static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+?^$\\\\|]");

    public static final String FIELD = "field";
    public static final String OP = "op";
    public static final String VALUE = "value";
    public static final String EQ = "eq";

    public enum METHOD_TRANSLATION {
        NONE,
        AS_NAME,
        AS_ID
    }

    public enum POLICY_TRANSLATION {
        NONE,
        TO_KEY,
        TO_NAME
    }

    public enum FILTER_OP {
        eq,
        ne,
        gt,
        lt,
        ge,
        le,
        like,
        gele
    }

    private String id;
    private String apiId;
    private String name;
    private String vhost;
    private String apiPath;
    private String queryStringVersion;
    private String state;
    private String backendBasepath;
    private String inboundSecurity;
    private String outboundAuthentication;
    private String organization;
    private String createdOn;
    private APIType type;
    private String policyName;
    private String tag;
    private List<String> customProperties;
    private boolean deprecated;
    private boolean retired;
    private String apiType;

    private METHOD_TRANSLATION translateMethodMode = METHOD_TRANSLATION.NONE;

    /**
     * If true, the API is loaded from the apirepo endpoint instead of proxies
     */
    private boolean loadBackendAPI = false;

    private boolean includeOperations = false;
    private boolean includeQuotas = false;
    private boolean includeClientOrganizations = false;
    private boolean includeClientApplications = false;
    private boolean includeClientAppQuota = false;
    private boolean includeImage = false;
    private boolean includeRemoteHost = false;
    private boolean includeOriginalAPIDefinition = false;
    private boolean useFEAPIDefinition = false;
    private boolean failOnError = true;
    private boolean includeMethods;
    POLICY_TRANSLATION translatePolicyMode = POLICY_TRANSLATION.NONE;
    List<NameValuePair> filters = new ArrayList<>();

    private APIFilter(APIType type) {
        this.type = type;
    }

    public List<NameValuePair> getFilters() {
        return filters;
    }

    public void setFilters(List<NameValuePair> filters) {
        this.filters.addAll(filters);
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
        if (apiId == null) return;
        this.apiId = apiId;
        filters.add(new BasicNameValuePair(FIELD, "apiid"));
        filters.add(new BasicNameValuePair(OP, EQ));
        filters.add(new BasicNameValuePair(VALUE, apiId));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) return;
        // All applications are requested - We ignore this filter
        if (name.equals("*")) return;
        this.name = name;
        FilterHelper.setFilter(name, filters);
    }

    public void setVhost(String vhost) {
        this.vhost = vhost;
    }

    public String getVhost() {
        return vhost;
    }

    public void setPolicyName(String policyName) {
        if (policyName != null) this.translatePolicyMode = POLICY_TRANSLATION.TO_NAME;
        this.policyName = policyName;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public String getApiType() {
        if (loadBackendAPI) {
            return APIManagerAdapter.TYPE_BACK_END;
        } else {
            return APIManagerAdapter.TYPE_FRONT_END;
        }
    }

    public APIType getType() {
        return type;
    }

    public boolean isIncludeOriginalAPIDefinition() {
        return includeOriginalAPIDefinition;
    }

    public void setIncludeOriginalAPIDefinition(boolean includeOriginalAPIDefinition) {
        this.includeOriginalAPIDefinition = includeOriginalAPIDefinition;
    }

    public boolean isUseFEAPIDefinition() {
        return useFEAPIDefinition;
    }

    public void setUseFEAPIDefinition(boolean useFEAPIDefinition) {
        this.useFEAPIDefinition = useFEAPIDefinition;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        if (apiPath == null) return;
        this.apiPath = apiPath;
        String op = EQ;
        if (apiPath.startsWith("*") || apiPath.endsWith("*")) {
            op = "like";
            apiPath = apiPath.replace("*", "");
        }
        // Only from version 7.7 on we can query for the path directly.
        if (APIManagerAdapter.hasAPIManagerVersion("7.7")) {
            filters.add(new BasicNameValuePair(FIELD, "path"));
            filters.add(new BasicNameValuePair(OP, op));
            filters.add(new BasicNameValuePair(VALUE, apiPath));
        }
    }

    public String getQueryStringVersion() {
        return queryStringVersion;
    }

    public void setQueryStringVersion(String queryStringVersion) {
        this.queryStringVersion = queryStringVersion;
    }

    public String getBackendBasepath() {
        return backendBasepath;
    }

    public void setBackendBasepath(String backendBasepath) {
        this.backendBasepath = backendBasepath;
    }

    public String getInboundSecurity() {
        return inboundSecurity;
    }

    public void setInboundSecurity(String inboundSecurity) {
        this.inboundSecurity = inboundSecurity;
    }

    public String getOutboundAuthentication() {
        return outboundAuthentication;
    }

    public void setOutboundAuthentication(String outboundAuthentication) {
        this.outboundAuthentication = outboundAuthentication;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public METHOD_TRANSLATION getTranslateMethodMode() {
        return translateMethodMode;
    }

    public void setTranslateMethodMode(METHOD_TRANSLATION translateMethodMode) {
        this.translateMethodMode = translateMethodMode;
    }

    public void setTranslatePolicyMode(POLICY_TRANSLATION translatePolicyMode) {
        this.translatePolicyMode = translatePolicyMode;
    }

    public void setLoadBackendAPI(boolean loadBackendAPI) {
        this.loadBackendAPI = loadBackendAPI;
    }

    public void setIncludeOperations(boolean includeOperations) {
        this.includeOperations = includeOperations;
    }

    public boolean isIncludeQuotas() {
        return includeQuotas;
    }

    public void setIncludeQuotas(boolean includeQuotas) {
        this.includeQuotas = includeQuotas;
    }

    public boolean isIncludeClientOrganizations() {
        return includeClientOrganizations;
    }

    public void setIncludeClientOrganizations(boolean includeClientOrganizations) {
        this.includeClientOrganizations = includeClientOrganizations;
    }

    public boolean isIncludeClientApplications() {
        return includeClientApplications;
    }

    public void setIncludeClientApplications(boolean includeClientApplications) {
        this.includeClientApplications = includeClientApplications;
    }

    public boolean isIncludeClientAppQuota() {
        return includeClientAppQuota;
    }

    public boolean isIncludeImage() {
        return includeImage;
    }

    public void setIncludeImage(boolean includeImage) {
        this.includeImage = includeImage;
    }

    public boolean isIncludeRemoteHost() {
        return includeRemoteHost;
    }

    public void setIncludeRemoteHost(boolean includeRemoteHost) {
        this.includeRemoteHost = includeRemoteHost;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        if (this.deprecated == deprecated) return;
        this.deprecated = deprecated;
        filters.add(new BasicNameValuePair(FIELD, "deprecated"));
        filters.add(new BasicNameValuePair(OP, EQ));
        filters.add(new BasicNameValuePair(VALUE, (deprecated) ? "true" : "false"));
    }

    public String getState() {
        return state;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        if (this.retired == retired) return;
        this.retired = retired;
        filters.add(new BasicNameValuePair(FIELD, "retired"));
        filters.add(new BasicNameValuePair(OP, EQ));
        filters.add(new BasicNameValuePair(VALUE, (retired) ? "true" : "false"));
    }

    public void setState(String state) {
        if (state == null) return;
        this.state = state;
        filters.add(new BasicNameValuePair(FIELD, "state"));
        filters.add(new BasicNameValuePair(OP, EQ));
        filters.add(new BasicNameValuePair(VALUE, state));
    }

    public void setCreatedOn(List<String[]> createdOn) {
        if (createdOn == null) return;
        for (String[] createdOnFilter : createdOn) {
            filters.add(new BasicNameValuePair(FIELD, "createdOn"));
            filters.add(new BasicNameValuePair(OP, createdOnFilter[1]));
            filters.add(new BasicNameValuePair(VALUE, createdOnFilter[0]));
        }
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }


    public List<String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(List<String> customProperties) {
        this.customProperties = customProperties;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof APIFilter)) return false;
        APIFilter other = (APIFilter) obj;
        return (
            StringUtils.equals(other.getId(), this.getId()) &&
                StringUtils.equals(other.getName(), this.getName()) &&
                StringUtils.equals(other.getApiId(), this.getApiId())
        );
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode += (this.id != null) ? this.id.hashCode() : 0;
        hashCode += (this.name != null) ? this.name.hashCode() : 0;
        return hashCode;
    }

    public boolean isIncludeMethods() {
        return includeMethods;
    }

    public void setIncludeMethods(boolean includeMethods) {
        this.includeMethods = includeMethods;
    }

    @Override
    public String toString() {
        return "APIFilter{" +
            "id='" + id + '\'' +
            ", apiId='" + apiId + '\'' +
            ", name='" + name + '\'' +
            ", vhost='" + vhost + '\'' +
            ", apiPath='" + apiPath + '\'' +
            ", queryStringVersion='" + queryStringVersion + '\'' +
            ", state='" + state + '\'' +
            ", backendBasepath='" + backendBasepath + '\'' +
            ", inboundSecurity='" + inboundSecurity + '\'' +
            ", outboundAuthentication='" + outboundAuthentication + '\'' +
            ", organization='" + organization + '\'' +
            ", createdOn='" + createdOn + '\'' +
            ", type=" + type +
            ", policyName='" + policyName + '\'' +
            ", tag='" + tag + '\'' +
            ", customProperties=" + customProperties +
            ", deprecated=" + deprecated +
            ", retired=" + retired +
            ", apiType='" + apiType + '\'' +
            ", translateMethodMode=" + translateMethodMode +
            ", loadBackendAPI=" + loadBackendAPI +
            ", includeOperations=" + includeOperations +
            ", includeQuotas=" + includeQuotas +
            ", includeClientOrganizations=" + includeClientOrganizations +
            ", includeClientApplications=" + includeClientApplications +
            ", includeClientAppQuota=" + includeClientAppQuota +
            ", includeImage=" + includeImage +
            ", includeRemoteHost=" + includeRemoteHost +
            ", includeOriginalAPIDefinition=" + includeOriginalAPIDefinition +
            ", useFEAPIDefinition=" + useFEAPIDefinition +
            ", failOnError=" + failOnError +
            ", includeMethods=" + includeMethods +
            ", translatePolicyMode=" + translatePolicyMode +
            '}';
    }

    public boolean filter(API api) {
        if (this.getApiPath() == null && this.getVhost() == null && this.getQueryStringVersion() == null && this.getPolicyName() == null && this.getBackendBasepath() == null
            && this.getTag() == null && this.getInboundSecurity() == null && this.getOutboundAuthentication() == null && this.getOrganization() == null) { // Nothing given to filter out.
            return false;
        }
        if (this.getPolicyName() != null && (!isPolicyUsed(api, this.getPolicyName()))) {
            return true;
        }
        if (this.getInboundSecurity() != null) {
            boolean match = false;
            if (api.getInboundProfiles() != null) {
                for (InboundProfile profile : api.getInboundProfiles().values()) {
                    if (profile.getSecurityProfile() != null) {
                        for (SecurityProfile securityProfile : api.getSecurityProfiles()) {
                            for (SecurityDevice securityDevice : securityProfile.getDevices()) {
                                List<String> deviceNames = Arrays.asList(securityDevice.getType().getAlternativeNames());
                                if (deviceNames.contains(this.getInboundSecurity().toLowerCase())) {
                                    match = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!match) { // No match found so far, check policy names
                match = isPolicyUsed(api, this.getInboundSecurity());
            }
            if (!match) return true; // Requested security is finally not found, return true
        }
        if (this.getBackendBasepath() != null) {
            Pattern pattern = Pattern.compile(this.getBackendBasepath().replace("*", ".*"));
            Matcher matcher = pattern.matcher(api.getServiceProfiles().get("_default").getBasePath());
            if (!matcher.matches()) {
                return true;
            }
        }
        if (this.getApiType().equals(APIManagerAdapter.TYPE_FRONT_END)) {
            if (this.getVhost() != null && !this.getVhost().equals(api.getVhost())) return true;
            if (this.getQueryStringVersion() != null && !this.getQueryStringVersion().equals(api.getApiRoutingKey()))
                return true;
        }
        if (this.getTag() != null) {
            // Simple filter format tag: "tagValue*"
            String tagGroupFilter = this.getTag();
            String tagValueFilter = this.getTag();
            if (this.getTag().contains("=")) { // Group specific format: "tagGroup=tagValue*"
                tagGroupFilter = this.getTag().split("=")[0];
                tagValueFilter = this.getTag().split("=")[1];
            }
            Pattern groupPattern = Pattern.compile(tagGroupFilter.toLowerCase().replace("*", ".*"));
            Pattern valuePattern = Pattern.compile(tagValueFilter.toLowerCase().replace("*", ".*"));
            Iterator<String> it = api.getTags().keySet().iterator();
            boolean match = false;
            while (it.hasNext()) {
                String tagGroup = it.next();
                Matcher matcher = groupPattern.matcher(tagGroup.toLowerCase());
                if (!matcher.matches()) {
                    // Search for specific group - No match - Ignore this group
                    if (getTag().contains("=")) break;
                } else {
                    // Filter match on the group
                    if (!getTag().contains("=")) match = true;
                }
                String[] tagValues = api.getTags().get(tagGroup);
                for (String tagValue : tagValues) {
                    matcher = valuePattern.matcher(tagValue.toLowerCase());
                    if (matcher.matches()) {
                        match = true;
                        break;
                    }
                }
                if (match) break;
            }
            // If none of the tags match, filter out this API
            if (!match) return true;
        }
        if (this.getOutboundAuthentication() != null) {
            boolean match = false;
            if (api.getOutboundProfiles() != null) {
                for (OutboundProfile profile : api.getOutboundProfiles().values()) {
                    if (profile.getAuthenticationProfile() != null) {
                        for (AuthenticationProfile authnProfile : api.getAuthenticationProfiles()) {
                            if (authnProfile.getName().equals(profile.getAuthenticationProfile())) {
                                List<String> authnNames = Arrays.asList(authnProfile.getType().getAlternativeNames());
                                if (authnNames.contains(this.getOutboundAuthentication().toLowerCase())) {
                                    match = true;
                                    break;
                                }
                                if (authnProfile.getType() == AuthType.oauth) {
                                    String providerProfile = (String) authnProfile.getParameters().get("providerProfile");
                                    providerProfile = Utils.getExternalPolicyName(providerProfile, FedKeyType.OAuthAppProfile);
                                    Pattern pattern = Pattern.compile(this.getOutboundAuthentication().toLowerCase().replace("*", ".*"));
                                    Matcher matcher = pattern.matcher(providerProfile.toLowerCase());
                                    if (matcher.matches()) {
                                        match = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!match) return true;
        }
        if (this.getOrganization() != null) {
            Pattern pattern = Pattern.compile(this.getOrganization().toLowerCase().replace("*", ".*"));
            Matcher matcher = pattern.matcher(api.getOrganization().getName().toLowerCase());
            return !matcher.matches();
        }
        return false;
    }

    /**
     * Build an applicationAdapter based on the given configuration
     */
    public static class Builder {

        public enum APIType {
            /**
             * APIs are created with:
             * - includingQuotas
             * - Methods translated to name
             * - Policies have the external name
             * - Client-Organizations and -Applications are initialized
             */
            ACTUAL_API,
            DESIRED_API,
            CUSTOM
        }

        String id;
        String apiId;
        String name;
        String vhost;
        String policyName;
        String tag;
        String apiPath;
        String queryStringVersion;
        String state;
        String backendBasepath;
        String inboundSecurity;
        String outboundAuthentication;
        String organization;
        List<String[]> createdOn;
        APIType apiType;
        List<String> customProperties;
        boolean deprecated;
        boolean retired;
        METHOD_TRANSLATION translateMethodMode = METHOD_TRANSLATION.NONE;
        boolean loadBackendAPI;
        boolean includeOperations = false;
        boolean includeQuotas = false;
        boolean includeClientOrganizations = false;
        boolean includeClientApplications = false;
        boolean includeClientAppQuota = false;
        boolean includeImage = false;
        boolean includeRemoteHost = false;
        boolean includeOriginalAPIDefinition = false;
        boolean useFEAPIDefinition = false;
        boolean failOnError = true;
        boolean includeMethods;
        POLICY_TRANSLATION translatePolicyMode = POLICY_TRANSLATION.NONE;
        List<NameValuePair> filters = new ArrayList<>();

        public Builder() {
            this(APIType.CUSTOM, false);
        }

        /**
         * Creates a ClientAppAdapter based on the provided configuration using all registered Adapters
         *
         * @param type of the APIFilter
         */
        public Builder(APIType type) {
            this(type, false);
        }

        /**
         * Creates a ClientAppAdapter based on the provided configuration using all registered Adapters
         *
         * @param type           of the APIFilter
         * @param loadBackendAPI is search backendEndAPI if set to true
         */
        public Builder(APIType type, boolean loadBackendAPI) {
            super();
            initType(type);
            this.apiType = type;
            this.loadBackendAPI = loadBackendAPI;
        }

        public APIFilter build() {
            APIFilter apiFilter = new APIFilter(this.apiType);
            apiFilter.setApiPath(this.apiPath);
            apiFilter.setQueryStringVersion(this.queryStringVersion);
            apiFilter.setVhost(this.vhost);
            apiFilter.setName(this.name);
            apiFilter.setPolicyName(this.policyName);
            apiFilter.setTag(this.tag);
            apiFilter.setFilters(this.filters);
            apiFilter.setId(this.id);
            apiFilter.setApiId(apiId);
            apiFilter.setIncludeOperations(this.includeOperations);
            apiFilter.setIncludeQuotas(this.includeQuotas);
            apiFilter.setTranslateMethodMode(this.translateMethodMode);
            apiFilter.setTranslatePolicyMode(this.translatePolicyMode);
            apiFilter.setIncludeClientOrganizations(this.includeClientOrganizations);
            apiFilter.setIncludeClientApplications(this.includeClientApplications);
            apiFilter.setIncludeOriginalAPIDefinition(this.includeOriginalAPIDefinition);
            apiFilter.setUseFEAPIDefinition(this.useFEAPIDefinition);
            apiFilter.setIncludeImage(this.includeImage);
            apiFilter.setIncludeRemoteHost(this.includeRemoteHost);
            apiFilter.setLoadBackendAPI(this.loadBackendAPI);
            apiFilter.setState(this.state);
            apiFilter.setRetired(this.retired);
            apiFilter.setDeprecated(this.deprecated);
            apiFilter.setCustomProperties(this.customProperties);
            apiFilter.setCreatedOn(this.createdOn);
            apiFilter.setBackendBasepath(this.backendBasepath);
            apiFilter.setInboundSecurity(this.inboundSecurity);
            apiFilter.setOutboundAuthentication(this.outboundAuthentication);
            apiFilter.setFailOnError(this.failOnError);
            apiFilter.setOrganization(organization);
            apiFilter.setIncludeMethods(includeMethods);
            return apiFilter;
        }

        private void initType(APIType type) {
            switch (type) {
                case ACTUAL_API:
                    this.includeQuotas = true;
                    this.translateMethodMode = METHOD_TRANSLATION.AS_NAME;
                    this.translatePolicyMode = POLICY_TRANSLATION.TO_NAME;
                    this.includeClientOrganizations = true;
                    this.includeClientApplications = true;
                    this.includeClientAppQuota = true;
                    this.includeOriginalAPIDefinition = true;
                    this.includeImage = true;
                    break;
                case DESIRED_API:
                default:
                    break;
            }
        }

        public Builder hasId(String id) {
            this.id = id;
            return this;
        }

        public Builder hasApiId(String apiId) {
            this.apiId = apiId;
            return this;
        }

        public Builder hasName(String name) {
            this.name = name;
            return this;
        }

        public Builder hasVHost(String vhost) {
            if (vhost != null && vhost.equals("NOT_SET")) return this; // NOT_SET is used for testing
            this.vhost = vhost;
            return this;
        }

        public Builder hasApiPath(String apiPath) {
            this.apiPath = apiPath;
            return this;
        }

        public Builder hasState(String state) {
            this.state = state;
            return this;
        }

        public Builder hasPolicyName(String policyName) {
            this.policyName = policyName;
            return this;
        }

        public Builder hasTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder isDeprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Builder isRetired(boolean retired) {
            this.retired = retired;
            return this;
        }

        public Builder isCreatedOnBefore(String createdOn) {
            if (createdOn == null) return this;
            if (this.createdOn == null) this.createdOn = new ArrayList<>();
            this.createdOn.add(new String[]{createdOn, FILTER_OP.lt.name()});
            return this;
        }

        public Builder isCreatedOnAfter(String createdOn) {
            if (createdOn == null) return this;
            if (this.createdOn == null) this.createdOn = new ArrayList<>();
            this.createdOn.add(new String[]{createdOn, FILTER_OP.gt.name()});
            return this;
        }


        public Builder hasQueryStringVersion(String queryStringVersion) {
            this.queryStringVersion = queryStringVersion;
            return this;
        }

        public Builder useFilter(List<NameValuePair> filters) {
            this.filters = filters;
            return this;
        }

        public Builder includeQuotas(boolean includeQuotas) {
            this.includeQuotas = includeQuotas;
            return this;
        }

        public Builder includeClientOrganizations(boolean includeClientOrganizations) {
            this.includeClientOrganizations = includeClientOrganizations;
            return this;
        }

        public Builder includeClientApplications(boolean includeClientApplications) {
            this.includeClientApplications = includeClientApplications;
            return this;
        }

        public Builder includeClientAppQuota(boolean includeClientAppQuota) {
            this.includeClientAppQuota = includeClientAppQuota;
            return this;
        }

        public Builder includeOriginalAPIDefinition(boolean includeOriginalAPIDefinition) {
            this.includeOriginalAPIDefinition = includeOriginalAPIDefinition;
            return this;
        }

        public Builder useFEAPIDefinition(boolean useFEAPIDefinition) {
            this.useFEAPIDefinition = useFEAPIDefinition;
            return this;
        }

        public Builder includeImage(boolean includeImage) {
            this.includeImage = includeImage;
            return this;
        }

        public Builder includeRemoteHost(boolean includeRemoteHost) {
            this.includeRemoteHost = includeRemoteHost;
            return this;
        }

        public Builder includeCustomProperties(List<String> customProperties) {
            this.customProperties = customProperties;
            return this;
        }

        public Builder includeCustomProperties(Map<String, String> customProperties) {
            if (customProperties == null) return this;
            this.customProperties = new ArrayList<>(customProperties.keySet());
            return this;
        }

        public Builder includeMethods(boolean includeMethods) {
            this.includeMethods = includeMethods;
            return this;
        }

        public Builder translatePolicies(POLICY_TRANSLATION translatePolicyMode) {
            this.translatePolicyMode = translatePolicyMode;
            return this;
        }

        public Builder translateMethods(METHOD_TRANSLATION translateMethodMode) {
            this.translateMethodMode = translateMethodMode;
            return this;
        }

        public Builder hasBackendBasepath(String backendBasepath) {
            this.backendBasepath = backendBasepath;
            return this;
        }

        public Builder hasOutboundAuthentication(String outboundAuthentication) {
            this.outboundAuthentication = outboundAuthentication;
            return this;
        }

        public Builder hasInboundSecurity(String inboundSecurity) {
            this.inboundSecurity = inboundSecurity;
            return this;
        }

        public Builder hasOrganization(String organization) {
            this.organization = organization;
            return this;
        }

        public Builder failOnError(boolean failOnError) {
            this.failOnError = failOnError;
            return this;
        }
    }

    private static boolean isPolicyUsed(API api, String policyName) {
        // pattern for escaping special regex characters (except *)
        String escaped = SPECIAL_REGEX_CHARS.matcher(policyName).replaceAll("\\\\$0");
        Pattern pattern = Pattern.compile(escaped.toLowerCase().replace("*", ".*"));
        if (api.getOutboundProfiles() != null) {
            for (OutboundProfile profile : api.getOutboundProfiles().values()) {
                for (Policy policy : profile.getAllPolices()) {
                    if (policy.getName() == null) {
                        LOG.warn("Cannot check policy: {} as policy name is empty.", policy);
                        continue;
                    }
                    Matcher matcher = pattern.matcher(policy.getName().toLowerCase());
                    if (matcher.matches()) {
                        return true;
                    }
                }
            }
        }
        if (api.getInboundProfiles() != null) {
            for (InboundProfile profile : api.getInboundProfiles().values()) {
                if (profile.getSecurityProfile() != null) {
                    for (SecurityProfile securityProfile : api.getSecurityProfiles()) {
                        if (securityProfile.getName().equals(profile.getSecurityProfile())) {
                            for (SecurityDevice device : securityProfile.getDevices()) {
                                if (device.getType() == DeviceType.authPolicy) {
                                    String securityPolicy = device.getProperties().get("authenticationPolicy");
                                    if (securityPolicy == null) return false;
                                    Matcher matcher = pattern.matcher(Utils.getExternalPolicyName(securityPolicy).toLowerCase());
                                    if (matcher.matches()) {
                                        return true;
                                    }
                                } else if (device.getType() == DeviceType.oauthExternal) {
                                    String tokenInfoPolicy = device.getProperties().get("tokenStore");
                                    if (tokenInfoPolicy != null) {
                                        Matcher matcher = pattern.matcher(Utils.getExternalPolicyName(tokenInfoPolicy).toLowerCase());
                                        if (matcher.matches()) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
