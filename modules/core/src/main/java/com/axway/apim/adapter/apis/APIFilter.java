package com.axway.apim.adapter.apis;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;

public class APIFilter {
	
	String vhost;
	String apiPath;
	String queryStringVersion;
	
	String type;
	
	List<NameValuePair> filters = new ArrayList<NameValuePair>();

	private APIFilter() {
	}

	public void setVhost(String vhost) {
		this.vhost = vhost;
	}

	public String getVhost() {
		return vhost;
	}
	
	

	/**
	 * Build an applicationAdapter based on the given configuration
	 */
	public static class Builder {
		
		String vhost;
		String apiPath;
		String queryStringVersion;
		
		String type;
		
		List<NameValuePair> filters = new ArrayList<NameValuePair>();
		
		/**
		 * @param config the config that is used what kind of adapter should be used
		 */
		public Builder() {
			super();
		}

		/**
		 * Creates a ClientAppAdapter based on the provided configuration using all registered Adapters
		 * @return a valid Adapter able to handle the config or null
		 */
		public Builder(String type) {
			super();
			this.type = type;
		}
		
		public APIFilter build() {
			APIFilter apiFilter = new APIFilter();
			apiFilter.apiPath = this.apiPath;
			apiFilter.queryStringVersion = this.queryStringVersion;
			apiFilter.type = this.type;
			apiFilter.vhost = this.vhost;
			apiFilter.filters = this.filters;
			return apiFilter;
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
	}

}
