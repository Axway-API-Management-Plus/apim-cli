package com.axway.apim.api.model.apps;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientApplication {
	private String id;
	private String name;
	private String description;
	private String email;
	private String phone;
	private boolean enabled;
	private String state;
	private String image;
	private String oauthClientId;
	private String extClientId;
	private String apiKey;
	
	private List<APIAccess> apiAccess;
	
	private List<ClientAppCredential> credentials = new ArrayList<ClientAppCredential>(); 
	
	private APIQuota appQuota;
	
	private String organizationId;
	
	private String organization;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getOauthClientId() {
		return oauthClientId;
	}
	public void setOauthClientId(String oauthClientId) {
		this.oauthClientId = oauthClientId;
	}
	public String getExtClientId() {
		return extClientId;
	}
	public void setExtClientId(String extClientId) {
		this.extClientId = extClientId;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.credentials.add(new APIKey());
		this.apiKey = apiKey;
	}
	
	public String getOrganizationId() throws AppException {
		return organizationId;
	}
	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}
	
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public APIQuota getAppQuota() {
		return appQuota;
	}
	public void setAppQuota(APIQuota appQuota) {
		this.appQuota = appQuota;
	}
	
	public List<APIAccess> getApiAccess() {
		return apiAccess;
	}
	public void setApiAccess(List<APIAccess> apiAccess) {
		this.apiAccess = apiAccess;
	}
	
	public List<ClientAppCredential> getCredentials() {
		return credentials;
	}
	public void setCredentials(List<ClientAppCredential> credentials) {
		this.credentials = credentials;
	}
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof ClientApplication) {
			return StringUtils.equals(((ClientApplication)other).getName(), this.getName());
		}
		return false;
	}
	@Override
	public String toString() {
		return "[" + name + "]";
	}
}
