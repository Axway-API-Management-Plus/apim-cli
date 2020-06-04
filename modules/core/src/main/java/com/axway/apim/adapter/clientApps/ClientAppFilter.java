package com.axway.apim.adapter.clientApps;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;

public class ClientAppFilter {
	
	boolean includeQuota;
	
	boolean includeCredentials;
	
	boolean includeAPIAccess;
	
	boolean includeImage;
	
	String applicationName;
	
	String state;
	
	Organization organization;
	
	String applicationId;
	
	List<NameValuePair> filters = new ArrayList<NameValuePair>();

	private ClientAppFilter() {	}
	
	public boolean isIncludeQuota() {
		return includeQuota;
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

	public String getApplicationId() {
		return applicationId;
	}

	public List<NameValuePair> getFilters() {
		return filters;
	}

	public void setOrganization(Organization organization) {
		if(organization==null) return;
		this.organization = organization;
		filters.add(new BasicNameValuePair("field", "orgid"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", organization.getId()));
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setApplicationName(String applicationName) {
		if(applicationName==null) return;
		this.applicationName = applicationName;
		filters.add(new BasicNameValuePair("field", "name"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", applicationName));
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}



	public void setState(String state) {
		if(state==null) return;
		this.state = state;
		filters.add(new BasicNameValuePair("field", "state"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", state));
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
		if(obj instanceof ClientAppFilter == false) return false;
		ClientAppFilter other = (ClientAppFilter)obj;
		return (
				StringUtils.equals(other.getApplicationName(), this.getApplicationName()) && 
				StringUtils.equals(other.getState(), this.getState()) &&
				(other.getOrganization()!=null || other.getOrganization().equals(this.getOrganization()))
				);
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		hashCode += (this.applicationId!=null) ? this.applicationId.hashCode() : 0;
		hashCode += (this.state!=null) ? this.state.hashCode() : 0;
		hashCode += (this.applicationName!=null) ? this.applicationName.hashCode() : 0;
		return hashCode;
	}

	@Override
	public String toString() {
		return "ClientAppFilter [name=" + applicationName + ", id=" + applicationId + "]";
	}

	/**
	 * Build an applicationAdapter based on the given configuration
	 */
	public static class Builder {
		
		boolean includeQuota;
		
		boolean includeCredentials;
		
		boolean includeImage;
		
		boolean includeAPIAccess;
		
		Organization organization;
		
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
			filter.includeAPIAccess = this.includeAPIAccess;
			return filter;
		}
		
		public Builder hasName(String name) throws AppException {
			if(name==null) return this;
			if(name.contains("|")) {
				Organization org = APIManagerAdapter.getInstance().orgAdapter.getOrgForName(name.substring(name.indexOf("|")+1));
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
		
		public Builder hasOrganizationId(String organizationId) throws AppException {
			if(organizationId==null) return this;
			Organization org = new Organization();
			org.setId(organizationId);
			return hasOrganization(org);
		}
		
		public Builder hasOrganizationName(String organizationName) throws AppException {
			if(organizationName==null) return this;
			Organization org = APIManagerAdapter.getInstance().orgAdapter.getOrgForName(organizationName);
			if(org==null) {
				ErrorState.getInstance().setError("The organization with name: '"+organizationName+"' is unknown.", ErrorCode.UNKNOWN_ORGANIZATION, false);
				throw new AppException("The organization with name: '"+organizationName+"' is unknown.", ErrorCode.UNKNOWN_ORGANIZATION);
			}
			return hasOrganization(org);
		}
		
		public Builder hasOrganization(Organization organization) throws AppException {
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
		
		public Builder includeAPIAccess(boolean includeAPIAccess) {
			this.includeAPIAccess = includeAPIAccess;
			return this;
		}
	}
}
