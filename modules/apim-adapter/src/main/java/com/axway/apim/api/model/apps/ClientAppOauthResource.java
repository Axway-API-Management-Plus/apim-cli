package com.axway.apim.api.model.apps;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFilter("ClientAppOauthResourceFilter")
public class ClientAppOauthResource {

	private String id;
	
	private String applicationId;
	
	private String uriprefix;
	
	private List<String> scopes;
	
	private String scope;
	
	private boolean enabled;

	
	@JsonProperty("isDefault")
	private boolean defaultScope;

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	
	public boolean isDefaultScope() {
		return defaultScope;
	}

	public void setDefaultScope(boolean defaultScope) {
		this.defaultScope = defaultScope;
	}

	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getUriprefix() {
		return uriprefix;
	}

	public void setUriprefix(String uriprefix) {
		this.uriprefix = uriprefix;
	}

	public List<String> getScopes() {
		return scopes;
	}

	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof ClientAppOauthResource) {
			ClientAppOauthResource otherAppCredential = (ClientAppOauthResource)other;
			return 
					(StringUtils.equals(otherAppCredential.getScope(), this.getScope() ) && 
					otherAppCredential.isDefaultScope()==this.isDefaultScope() );
		}
		return false;
	}	
}
