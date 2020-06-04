package com.axway.apim.api.model.apps;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.axway.apim.adapter.apis.jackson.APIAccessSerializer;
import com.axway.apim.adapter.apis.jackson.JSONViews;
import com.axway.apim.adapter.apis.jackson.OrganizationDeserializer;
import com.axway.apim.adapter.apis.jackson.OrganizationSerializer;
import com.axway.apim.api.model.APIAccess;
import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.Image;
import com.axway.apim.api.model.Organization;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientApplication {
	@JsonView(JSONViews.ApplicationBase.class)
	private String id;
	@JsonView(JSONViews.ApplicationBase.class)
	private String name;
	@JsonView(JSONViews.ApplicationBase.class)
	private String description;
	@JsonView(JSONViews.ApplicationBase.class)
	private String email;
	@JsonView(JSONViews.ApplicationBase.class)
	private String phone;
	@JsonView(JSONViews.ApplicationBase.class)
	private boolean enabled;

	private String state;

	@JsonProperty("image")
	private String imageUrl;
	
	@JsonIgnore
	private Image image;
	
	private String oauthClientId;
	private String extClientId;
	private String apiKey;
	
	@JsonView(JSONViews.ApplicationAPIs.class)
	@JsonSerialize (using = APIAccessSerializer.class)
	@JsonProperty("apis")
	private List<APIAccess> apiAccess = new ArrayList<APIAccess>();
	
	private List<ClientAppCredential> credentials = new ArrayList<ClientAppCredential>(); 
	
	private APIQuota appQuota;
	
	@JsonDeserialize( using = OrganizationDeserializer.class)
	@JsonSerialize (using = OrganizationSerializer.class)
	@JsonProperty(value = "organizationId")
	@JsonAlias({ "organization" })
	@JsonView(JSONViews.ApplicationBase.class)
	private Organization organization;
	
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
		if(StringUtils.isEmpty(description)) return null;
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
			return 
					StringUtils.equals(otherApp.getName(), this.getName()) &&
					StringUtils.equals(otherApp.getEmail(), this.getEmail()) && 
					StringUtils.equals(otherApp.getDescription(), this.getDescription()) &&
					StringUtils.equals(otherApp.getPhone(), this.getPhone()) &&
					StringUtils.equals(otherApp.getState(), this.getState()) &&
					(otherApp.getCredentials()==null || otherApp.getCredentials().equals(this.getCredentials())) &&
					(otherApp.getImage()==null || otherApp.getImage().equals(this.getImage()))
					;
		}
		return false;
	}
	@Override
	public String toString() {
		return "[" + name + "]";
	}
}
