package com.axway.apim.adapter.clientApps;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.apps.APIKey;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.api.model.apps.ExtClients;
import com.axway.apim.api.model.apps.OAuth;
import com.axway.apim.lib.CustomPropertiesFilter;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;

public class ClientAppFilter implements CustomPropertiesFilter {
	
	private static Logger LOG = LoggerFactory.getLogger(ClientAppFilter.class);
	
	boolean includeQuota;
	
	boolean includeCredentials;
	
	boolean includeAPIAccess;
	
	boolean includeImage;
	
	private String credential;
	
	private String redirectUrl;
	
	private String applicationName;
	
	private String state;
	
	private Organization organization;
	
	private String applicationId;
	
	boolean includeOauthResources;
	
	private List<String> customProperties;
	
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

	public List<String> getCustomProperties() {
		return customProperties;
	}

	public void setCustomProperties(List<String> customProperties) {
		this.customProperties = customProperties;
	}

	public void setApplicationName(String applicationName) {
		if(applicationName==null) return;
		// All applications are requested - We ignore this filter
		if(applicationName.equals("*")) return;
		this.applicationName = applicationName;
		String op = "eq";
		if(applicationName.startsWith("*") || applicationName.endsWith("*")) {
			op = "like";
			applicationName = applicationName.replace("*", "");
		}
		filters.add(new BasicNameValuePair("field", "name"));
		filters.add(new BasicNameValuePair("op", op));
		filters.add(new BasicNameValuePair("value", applicationName));
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

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
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
				(other.getOrganization()==null || other.getOrganization().equals(this.getOrganization()))
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
		if(LOG.isDebugEnabled()) {
			return "ClientAppFilter [includeQuota=" + includeQuota + ", includeCredentials=" + includeCredentials
					+ ", includeAPIAccess=" + includeAPIAccess + ", includeImage=" + includeImage + ", applicationName="
					+ applicationName + ", state=" + state + ", organization=" + organization + ", applicationId="
					+ applicationId + ", filters=" + filters + "]";
		} else {
			return "ClientAppFilter [applicationName=" + applicationName + ", state=" + state + ", organization="
					+ organization + ", applicationId=" + applicationId + "]";
		}
	}

	public boolean filter(ClientApplication app) throws AppException {
		if(this.getCredential()==null && this.getRedirectUrl()==null) { // Nothing given to filter out.
			return true;
		}
		if(app.getCredentials()==null) return false;
		boolean match = false;
		for(ClientAppCredential cred : app.getCredentials()) {
			switch(cred.getCredentialType()) {
			case "oauth":
				match = filterCredential(((OAuth)cred).getClientId(), ((OAuth)cred).getRedirectUrls(), app.getName());
				break;
			case "extclients":
				match = filterCredential(((ExtClients)cred).getClientId(), null, app.getName());
				break;
			case "apikeys":
				match = filterCredential(((APIKey)cred).getApiKey(), null, app.getName());
				break;
			}
			if(match) break;
		}
		return match;
	}
	
	private boolean filterCredential(String appCredential, String[] appRedirectUrls, String appName) {
		if(appCredential==null) {
			LOG.warn("Inconsistent application: '"+appName+"' found. API-Key/Client-ID is NULL for credential.");
			return false;
		}
		if(this.credential!=null) {
			Pattern pattern = Pattern.compile(this.credential.replace("*", ".*"));
			Matcher matcher = pattern.matcher(appCredential);
			if(!matcher.matches()) return false;
		}
		if(this.redirectUrl!=null) {
			if(appRedirectUrls==null || appRedirectUrls.length==0) return false;
			Pattern pattern = Pattern.compile(this.redirectUrl.replace("*", ".*"));
			boolean redirectUrlMatch = false;
			for(String appRedirectUrl : appRedirectUrls) {
				Matcher matcher = pattern.matcher(appRedirectUrl);
				if(matcher.matches()) {
					redirectUrlMatch = true;
					break;
				}
			}
			if(!redirectUrlMatch) return false;
		}
		return true;
	}


	/**
	 * Build an applicationAdapter based on the given configuration
	 */
	public static class Builder {
		
		private boolean includeQuota;
		
		private boolean includeCredentials;
		
		private boolean includeImage;
		
		private boolean includeAPIAccess;
		
		private Organization organization;
		
		/** The name of the application */
		private String applicationName;
		
		private String applicationId;
		
		private String state;
		
		private String credential;
		
		private String redirectUrl;
		
		private boolean includeOauthResources;
		
		private List<String> customProperties;
		
		public Builder() {
			super();
		}

		/**
		 * Creates a ClientAppAdapter based on the provided configuration using all registered Adapters
		 * @return ClientAppFilter used to filder for Client-Apps
		 */
		public ClientAppFilter build() {
			ClientAppFilter filter = new ClientAppFilter();
			filter.setApplicationId(this.applicationId);
			filter.setApplicationName(this.applicationName);
			filter.setOrganization(this.organization);
			filter.setState(this.state);
			filter.setIncludeQuota(this.includeQuota);
			filter.setIncludeCredentials(this.includeCredentials);
			filter.setIncludeImage(this.includeImage);
			filter.setIncludeAPIAccess(this.includeAPIAccess);
			filter.setCredential(this.credential);
			filter.setRedirectUrl(this.redirectUrl);
			filter.setIncludeOauthResources(this.includeOauthResources);
			filter.setCustomProperties(this.customProperties);
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
		
		public Builder hasCredential(String credential) {
			this.credential = credential;
			return this;
		}
		
		public Builder hasRedirectUrl(String redirectUrl) {
			this.redirectUrl = redirectUrl;
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
		
		public Builder includeOauthResources(boolean includeOauthResources) {
			this.includeOauthResources = includeOauthResources;
			return this;
		}
		
		public Builder includeCustomProperties(List<String> customProperties) {
			this.customProperties = customProperties;
			return this;
		}
	}
}
