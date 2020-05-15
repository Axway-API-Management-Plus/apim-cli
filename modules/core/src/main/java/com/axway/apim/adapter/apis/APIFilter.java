package com.axway.apim.adapter.apis;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.axway.apim.adapter.APIManagerAdapter;

public class APIFilter {
	
	public static int NO_TRANSLATION = -1;
	public static int METHODS_AS_NAME = 0;
	public static int METHODS_AS_ID = 1;

	public static int TO_INTERNAL_POLICY_NAME = 10;
	public static int TO_EXTERNAL_POLICY_NAME = 15;
	
	private String id;
	private String apiId;
	private String name;
	private String vhost;
	private String apiPath;
	private String queryStringVersion;
	private String state;
	
	private boolean deprecated;
	private boolean retired;
	
	private String apiType;
	
	private int translateMethodMode = NO_TRANSLATION;
	
	private boolean useBackendAPI = false;
	
	private boolean includeOperations = false;
	private boolean includeQuotas = false;
	private boolean includeClientOrganizations = false;
	private boolean includeClientApplications = false;
	
	private boolean includeOriginalAPIDefinition = false;
	
	int translatePolicyMode = NO_TRANSLATION;
	
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
		this.name = name;
		filters.add(new BasicNameValuePair("field", "name"));
		filters.add(new BasicNameValuePair("op", "eq"));
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
		filters.add(new BasicNameValuePair("field", "path"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", apiPath));
	}

	public String getQueryStringVersion() {
		return queryStringVersion;
	}

	public void setQueryStringVersion(String queryStringVersion) {
		this.queryStringVersion = queryStringVersion;
	}

	public int getTranslateMethodMode() {
		return translateMethodMode;
	}

	public void setTranslateMethodMode(int translateMethodMode) {
		this.translateMethodMode = translateMethodMode;
	}

	public int getTranslatePolicyMode() {
		return translatePolicyMode;
	}

	public void setTranslatePolicyMode(int translatePolicyMode) {
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
			 * - Methods translated to name</br>
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
		
		boolean deprecated;
		boolean retired;
		
		int translateMethodMode = NO_TRANSLATION;
		
		boolean useBackendAPI = false;
		
		boolean includeOperations = false;
		boolean includeQuotas = false;
		boolean includeClientOrganizations = false;
		boolean includeClientApplications = false;
		
		boolean includeOriginalAPIDefinition = false;
		
		int translatePolicyMode = NO_TRANSLATION;
		
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
			apiFilter.setUseBackendAPI(this.useBackendAPI);
			apiFilter.setState(this.state);
			apiFilter.setRetired(this.retired);
			apiFilter.setDeprecated(this.deprecated);
			return apiFilter;
		}

		private void initType(Type type) {
			switch(type) {
			case ACTUAL_API:
				this.includeQuotas = true;
				this.translateMethodMode = METHODS_AS_NAME;
				this.translatePolicyMode = TO_EXTERNAL_POLICY_NAME;
				this.includeClientOrganizations = true;
				this.includeClientApplications = true;
				this.includeOriginalAPIDefinition = true;
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
		
		public Builder translatePolicies(int translatePolicyMode) {
			this.translatePolicyMode = translatePolicyMode;
			return this;
		}
		
		public Builder translateMethods(int translateMethodMode) {
			this.translateMethodMode = translateMethodMode;
			return this;
		}
	}

}
