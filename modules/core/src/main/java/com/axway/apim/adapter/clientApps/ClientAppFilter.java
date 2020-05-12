package com.axway.apim.adapter.clientApps;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class ClientAppFilter {
	
	boolean includeQuota;
	
	boolean includeCredentials;
	
	boolean includeImage;
	
	String applicationId;
	
	List<NameValuePair> filters = new ArrayList<NameValuePair>();

	private ClientAppFilter() {
		// TODO Auto-generated constructor stub
	}
	
	public boolean isIncludeQuota() {
		return includeQuota;
	}
	
	public boolean isIncludeCredentials() {
		return includeCredentials;
	}

	public boolean isIncludeImage() {
		return includeImage;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public List<NameValuePair> getFilters() {
		return filters;
	}

	public void setOrganization(String organization) {
		if(organization==null) return;
		filters.add(new BasicNameValuePair("field", "orgid"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", organization));
	}



	public void setApplicationName(String applicationName) {
		if(applicationName==null) return;
		filters.add(new BasicNameValuePair("field", "name"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", applicationName));
	}



	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}



	public void setState(String state) {
		if(state==null) return;
		filters.add(new BasicNameValuePair("field", "state"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", state));
	}
	
	void useFilter(List<NameValuePair> filter) {
		this.filters.addAll(filter);
	}


	/**
	 * Build an applicationAdapter based on the given configuration
	 */
	public static class Builder {
		
		boolean includeQuota;
		
		boolean includeCredentials;
		
		boolean includeImage;
		
		String organization;
		
		/** The name of the application */
		String applicationName;
		
		String applicationId;
		
		String state;
		
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
		public ClientAppFilter build() {
			ClientAppFilter filter = new ClientAppFilter();
			filter.setApplicationId(this.applicationId);
			filter.setApplicationName(this.applicationName);
			filter.setOrganization(this.organization);
			filter.setState(this.state);
			filter.includeQuota = this.includeQuota;
			filter.includeCredentials = this.includeCredentials;
			filter.includeImage = this.includeImage;
			return filter;
		}
		
		public Builder hasName(String name) {
			this.applicationName = name;
			return this;
		}
		
		public Builder hasId(String id) {
			this.applicationId = id;
			return this;
		}
		
		public Builder hasOrganization(String organization) {
			this.organization = organization;
			return this;
		}
		
		public Builder hasState(String state) {
			this.state = state;
			return this;
		}
		
		public Builder includeQuotas(boolean includeQuota) {
			this.includeQuota = includeQuota;
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
	}

}
