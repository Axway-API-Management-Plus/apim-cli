package com.axway.apim.adapter.apis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.clientApps.ClientAppFilter;

public class APIFilter {
	
	private static Logger LOG = LoggerFactory.getLogger(ClientAppFilter.class);
	
	public static enum METHOD_TRANSLATION {
		NONE, 
		AS_NAME, 
		AS_ID
	}
	
	public static enum POLICY_TRANSLATION {
		NONE, 
		TO_KEY, 
		TO_NAME
	}
	
	private String id;
	private String apiId;
	private String name;
	private String vhost;
	private String apiPath;
	private String queryStringVersion;
	private String state;
	
	private Map<String, String> customProperties;
	
	private boolean deprecated;
	private boolean retired;
	
	private String apiType;
	
	private METHOD_TRANSLATION translateMethodMode = METHOD_TRANSLATION.NONE;
	
	private boolean useBackendAPI = false;
	
	private boolean includeOperations = false;
	private boolean includeQuotas = false;
	private boolean includeClientOrganizations = false;
	private boolean includeClientApplications = false;
	private boolean includeImage = false;
	
	private boolean includeOriginalAPIDefinition = false;
	
	POLICY_TRANSLATION translatePolicyMode = POLICY_TRANSLATION.NONE;
	
	List<NameValuePair> filters = new ArrayList<NameValuePair>();

	private APIFilter() {
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
		if(apiId==null) return;
		this.apiId = apiId;
		filters.add(new BasicNameValuePair("field", "apiid"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", apiId));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(name==null) return;
		// All applications are requested - We ignore this filter
		if(name.equals("*")) return;
		this.name = name;
		String op = "eq";
		if(name.startsWith("*") || name.endsWith("*")) {
			op = "like";
			name = name.replace("*", "");
		}
		filters.add(new BasicNameValuePair("field", "name"));
		filters.add(new BasicNameValuePair("op", op));
		filters.add(new BasicNameValuePair("value", name));
	}

	public void setVhost(String vhost) {
		this.vhost = vhost;
	}

	public String getVhost() {
		return vhost;
	}

	public String getApiType() {
		if(useBackendAPI) {
			return APIManagerAdapter.TYPE_BACK_END;
		} else {
			return APIManagerAdapter.TYPE_FRONT_END;
		}
	}

	public boolean isIncludeOriginalAPIDefinition() {
		return includeOriginalAPIDefinition;
	}

	public void setIncludeOriginalAPIDefinition(boolean includeOriginalAPIDefinition) {
		this.includeOriginalAPIDefinition = includeOriginalAPIDefinition;
	}

	public String getApiPath() {
		return apiPath;
	}

	public void setApiPath(String apiPath) {
		if(apiPath==null) return;
		this.apiPath = apiPath;
		String op = "eq";
		if(apiPath.startsWith("*") || apiPath.endsWith("*")) {
			op = "like";
			apiPath = apiPath.replace("*", "");
		}
		// Only from version 7.7 on we can query for the path directly.
		if(APIManagerAdapter.hasAPIManagerVersion("7.7")) {
			filters.add(new BasicNameValuePair("field", "path"));
			filters.add(new BasicNameValuePair("op", op));
			filters.add(new BasicNameValuePair("value", apiPath));
		}
	}

	public String getQueryStringVersion() {
		return queryStringVersion;
	}

	public void setQueryStringVersion(String queryStringVersion) {
		this.queryStringVersion = queryStringVersion;
	}

	public METHOD_TRANSLATION getTranslateMethodMode() {
		return translateMethodMode;
	}

	public void setTranslateMethodMode(METHOD_TRANSLATION translateMethodMode) {
		this.translateMethodMode = translateMethodMode;
	}

	public POLICY_TRANSLATION getTranslatePolicyMode() {
		return translatePolicyMode;
	}

	public void setTranslatePolicyMode(POLICY_TRANSLATION translatePolicyMode) {
		this.translatePolicyMode = translatePolicyMode;
	}

	public boolean isUseBackendAPI() {
		return useBackendAPI;
	}

	public void setUseBackendAPI(boolean useBackendAPI) {
		this.useBackendAPI = useBackendAPI;
	}

	public boolean isIncludeOperations() {
		return includeOperations;
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
	
	

	public boolean isIncludeImage() {
		return includeImage;
	}

	public void setIncludeImage(boolean includeImage) {
		this.includeImage = includeImage;
	}

	public void setApiType(String apiType) {
		this.apiType = apiType;
	}

	public boolean isDeprecated() {
		return deprecated;
	}

	public void setDeprecated(boolean deprecated) {
		if(this.deprecated==deprecated) return;
		this.deprecated = deprecated;
		filters.add(new BasicNameValuePair("field", "deprecated"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", (deprecated) ? "true" : "false"));
	}

	public String getState() {
		return state;
	}

	public boolean isRetired() {
		return retired;
	}

	public void setRetired(boolean retired) {
		if(this.retired==retired) return;
		this.retired = retired;
		filters.add(new BasicNameValuePair("field", "retired"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", (retired) ? "true" : "false"));
	}

	public void setState(String state) {
		if(state==null) return;
		this.state = state;
		filters.add(new BasicNameValuePair("field", "state"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", state));
	}
	
	public Map<String, String> getCustomProperties() {
		return customProperties;
	}

	public void setCustomProperties(Map<String, String> customProperties) {
		this.customProperties = customProperties;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if(obj instanceof APIFilter == false) return false;
		APIFilter other = (APIFilter)obj;
		return (
				StringUtils.equals(other.getId(), this.getId()) && 
				StringUtils.equals(other.getName(), this.getName()) &&
				StringUtils.equals(other.getApiId(), this.getApiId())
				);
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		hashCode += (this.id!=null) ? this.id.hashCode() : 0;
		hashCode += (this.name!=null) ? this.name.hashCode() : 0;
		return hashCode;
	}

	@Override
	public String toString() {
		if(LOG.isTraceEnabled()) {
			return "APIFilter [id=" + id + ", apiId=" + apiId + ", name=" + name + ", vhost=" + vhost + ", apiPath="
					+ apiPath + ", queryStringVersion=" + queryStringVersion + ", state=" + state + ", customProperties="
					+ customProperties + ", deprecated=" + deprecated + ", retired=" + retired + ", apiType=" + apiType
					+ ", translateMethodMode=" + translateMethodMode + ", useBackendAPI=" + useBackendAPI
					+ ", includeOperations=" + includeOperations + ", includeQuotas=" + includeQuotas
					+ ", includeClientOrganizations=" + includeClientOrganizations + ", includeClientApplications="
					+ includeClientApplications + ", includeImage=" + includeImage + ", includeOriginalAPIDefinition="
					+ includeOriginalAPIDefinition + ", translatePolicyMode=" + translatePolicyMode + ", filters=" + filters
					+ "]";
		} else if(LOG.isDebugEnabled()) {
			return "APIFilter [id=" + id + ", name=" + name + ", vhost=" + vhost + ", apiPath=" + apiPath
					+ ", queryStringVersion=" + queryStringVersion + ", state=" + state + ", deprecated=" + deprecated
					+ ", retired=" + retired + ", useBackendAPI=" + useBackendAPI + "]";
		} else {
			return "APIFilter [id=" + id + ", name=" + name + ", vhost=" + vhost + ", apiPath=" + apiPath
					+ ", queryStringVersion=" + queryStringVersion + "]";			
		}
	}
	
	
	
	


	/**
	 * Build an applicationAdapter based on the given configuration
	 */
	public static class Builder {
		
		public static enum APIType {
			/**
			 * APIs are created with:</br> 
			 * - includingQuotas</br>
			 * - Methods translated to name</br>
			 * - Policies have the external name</br>
			 * - Client-Organizations and -Applications are initialized
			 */
			ACTUAL_API, 
			DESIRED_API, 
			CUSTOM
		}
		
		public static enum Type {
			/**
			 * APIs are created with:</br> 
			 * - including the original API-Definition
			 * - includingQuotas</br>
			 * - Methods are not translated and stay with ID</br>
			 * - Policies have the external name</br>
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
		String apiPath;
		String queryStringVersion;
		String state;
		
		Map<String, String> customProperties;
		
		boolean deprecated;
		boolean retired;
		
		METHOD_TRANSLATION translateMethodMode = METHOD_TRANSLATION.NONE;
		
		boolean useBackendAPI = false;
		
		boolean includeOperations = false;
		boolean includeQuotas = false;
		boolean includeClientOrganizations = false;
		boolean includeClientApplications = false;
		boolean includeImage = false;
		
		boolean includeOriginalAPIDefinition = false;
		
		POLICY_TRANSLATION translatePolicyMode = POLICY_TRANSLATION.NONE;
		
		List<NameValuePair> filters = new ArrayList<NameValuePair>();
		
		/**
		 * @param config the config that is used what kind of adapter should be used
		 */
		public Builder() {
			this(Type.CUSTOM, false);
		}

		/**
		 * Creates a ClientAppAdapter based on the provided configuration using all registered Adapters
		 * @return a valid Adapter able to handle the config or null
		 */
		public Builder(Type type) {
			this(type, false);
		}
		
		/**
		 * Creates a ClientAppAdapter based on the provided configuration using all registered Adapters
		 * @return a valid Adapter able to handle the config or null
		 */
		public Builder(Type type, boolean useBackendAPI) {
			super();
			initType(type);
			this.useBackendAPI = useBackendAPI;
		}
		
		public APIFilter build() {
			APIFilter apiFilter = new APIFilter();
			apiFilter.setApiPath(this.apiPath);
			apiFilter.setQueryStringVersion(this.queryStringVersion);
			apiFilter.setVhost(this.vhost);
			apiFilter.setName(this.name);
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
			apiFilter.setIncludeImage(this.includeImage);
			apiFilter.setUseBackendAPI(this.useBackendAPI);
			apiFilter.setState(this.state);
			apiFilter.setRetired(this.retired);
			apiFilter.setDeprecated(this.deprecated);
			apiFilter.setCustomProperties(this.customProperties);
			return apiFilter;
		}

		private void initType(Type type) {
			switch(type) {
			case ACTUAL_API:
				this.includeQuotas = true;
				this.translateMethodMode = METHOD_TRANSLATION.AS_NAME;
				this.translatePolicyMode = POLICY_TRANSLATION.TO_NAME;
				this.includeClientOrganizations = true;
				this.includeClientApplications = true;
				this.includeOriginalAPIDefinition = true;
				this.includeImage = true;
				break;
			case DESIRED_API:
				break;
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
			if(vhost!=null && vhost.equals("NOT_SET")) return this; // NOT_SET is used for testing
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
		
		public Builder isDeprecated(boolean deprecated) {
			this.deprecated = deprecated;
			return this;
		}
		
		public Builder isRetired(boolean retired) {
			this.retired = retired;
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
		
		public Builder includeOperations(boolean includeOperations) {
			this.includeOperations = includeOperations;
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
		
		public Builder includeOriginalAPIDefinition(boolean includeOriginalAPIDefinition) {
			this.includeOriginalAPIDefinition = includeOriginalAPIDefinition;
			return this;
		}
		
		public Builder includeImage(boolean includeImage) {
			this.includeImage = includeImage;
			return this;
		}
		
		public Builder includeCustomProperties(Map<String, String> customProperties) {
			this.customProperties = customProperties;
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
	}

}
