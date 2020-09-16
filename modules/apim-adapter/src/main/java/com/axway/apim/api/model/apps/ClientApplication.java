package com.axway.apim.api.model.apps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.clientApps.APIMgrAppsAdapter;
import com.axway.apim.adapter.jackson.APIAccessSerializer;
import com.axway.apim.adapter.jackson.OrganizationDeserializer;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("ApplicationFilter")
public class ClientApplication extends AbstractEntity {

	
	private String email;
	private String phone;
	private boolean enabled;

	private String state;

	@JsonProperty("image")
	private String imageUrl;
	
	@JsonIgnore
	private Image image;
	
	private String oauthClientId;
	private String extClientId;
	private String apiKey;
	
	@JsonSerialize (using = APIAccessSerializer.class)
	@JsonProperty("apis")
	private List<APIAccess> apiAccess = new ArrayList<APIAccess>();
	
	private List<ClientAppCredential> credentials = new ArrayList<ClientAppCredential>(); 
	
	private APIQuota appQuota;
	
	@JsonDeserialize( using = OrganizationDeserializer.class)
	@JsonAlias({ "organization", "organizationId" })	
	private Organization organization;
	
	public String getOrganizationId() {
		if(this.organization == null) return null;
		return this.organization.getId();
	}
	
	public String getEmail() {
		if(StringUtils.isEmpty(email)) return null;
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		if(StringUtils.isEmpty(phone)) return null;
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
		if(this.state==null) return "approved";
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
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
	
	public Organization getOrganization() {
		return organization;
	}
	public void setOrganization(Organization organization) {
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
			ClientApplication otherApp = (ClientApplication)other;
			Comparator c = Comparator.comparing(ClientAppCredential::getCredentialType).thenComparing(ClientAppCredential::getId);
			return 
					StringUtils.equals(otherApp.getName(), this.getName()) &&
					StringUtils.equals(otherApp.getEmail(), this.getEmail()) && 
					StringUtils.equals(otherApp.getDescription(), this.getDescription()) &&
					StringUtils.equals(otherApp.getPhone(), this.getPhone()) &&
					StringUtils.equals(otherApp.getState(), this.getState()) &&
					(otherApp.getCredentials()==null || otherApp.getCredentials().stream().sorted(c).collect(Collectors.toList()).equals(this.getCredentials().stream().sorted(c).collect(Collectors.toList()))) &&
					(otherApp.getImage()==null || otherApp.getImage().equals(this.getImage()))
					;
		}
		return false;
	}
	@Override
	public String toString() {
		return "[" + getName() + "]";
	}

}
