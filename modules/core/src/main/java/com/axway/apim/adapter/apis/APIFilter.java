package com.axway.apim.adapter.apis;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;

import com.axway.apim.adapter.APIManagerAdapter;

public class APIFilter {
	
	public static int NO_TRANSLATION = -1;
	public static int METHODS_AS_NAME = 0;
	public static int METHODS_AS_ID = 1;

	public static int TO_INTERNAL_POLICY_NAME = 10;
	public static int TO_EXTERNAL_POLICY_NAME = 15;
	
	String id;
	String vhost;
	String apiPath;
	String queryStringVersion;
	
	private String apiType;
	
	int translateMethodMode = NO_TRANSLATION;
	
	private boolean useBackendAPI = false;
	
	boolean includeOperations = false;
	boolean includeQuotas = false;
	boolean includeClientOrganizations = false;
	boolean includeClientApplications = false;
	
	int translatePolicyMode = NO_TRANSLATION;
	
	List<NameValuePair> filters = new ArrayList<NameValuePair>();

	private APIFilter() {
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
		String vhost;
		String apiPath;
		String queryStringVersion;
		
		int translateMethodMode = NO_TRANSLATION;
		
		boolean useBackendAPI = false;
		
		boolean includeOperations = false;
		boolean includeQuotas = false;
		boolean includeClientOrganizations = false;
		boolean includeClientApplications = false;
		
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
			apiFilter.apiPath = this.apiPath;
			apiFilter.queryStringVersion = this.queryStringVersion;
			apiFilter.vhost = this.vhost;
			apiFilter.filters = this.filters;
			apiFilter.id = this.id;
			apiFilter.includeOperations = this.includeOperations;
			apiFilter.includeQuotas = this.includeQuotas;
			apiFilter.translateMethodMode = this.translateMethodMode;
			apiFilter.translatePolicyMode = this.translatePolicyMode;
			apiFilter.includeClientOrganizations = this.includeClientOrganizations;
			apiFilter.includeClientApplications = this.includeClientApplications;
			apiFilter.useBackendAPI = this.useBackendAPI;
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

		public Builder hasVHost(String vhost) {
			this.vhost = vhost;
			return this;
		}

		public Builder hasApiPath(String apiPath) {
			this.apiPath = apiPath;
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
