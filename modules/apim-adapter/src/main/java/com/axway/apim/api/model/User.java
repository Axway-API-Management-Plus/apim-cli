package com.axway.apim.api.model;

import org.apache.commons.lang3.StringUtils;

import com.axway.apim.adapter.jackson.OrganizationDeserializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("UserFilter")
public class User {
	String id;
	
	@JsonDeserialize( using = OrganizationDeserializer.class)
	@JsonAlias({"organizationId", "organization"}) // Alias to read Organization based on the id as given by the API-Manager
	Organization organization;
	String name;
	String description;
	String loginName;
	String email;
	String role;
	Boolean enabled;
	Long createdOn;
	String state;
	String type;
	String phone;
	String mobile;
	
	AuthenticatedUserAttributes authNUserAttributes;
	
	String dn;
	
	@JsonProperty("image")
	private String imageUrl;
	
	@JsonIgnore
	private Image image;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Organization getOrganization() {
		return organization;
	}
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
	public String getOrganizationId() {
		if(this.organization==null) return null; 
		return this.organization.getId();
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
	public String getLoginName() {
		return loginName;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public Boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	public Long getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Long createdOn) {
		this.createdOn = createdOn;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDn() {
		return dn;
	}
	public void setDn(String dn) {
		this.dn = dn;
	}
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public AuthenticatedUserAttributes getAuthNUserAttributes() {
		return authNUserAttributes;
	}
	public void setAuthNUserAttributes(AuthenticatedUserAttributes authNUserAttributes) {
		this.authNUserAttributes = authNUserAttributes;
	}
	
	public boolean deepEquals(Object other) {
		if(other == null) return false;
		if(other instanceof User) {
			User otherUser = (User)other;
			return 
					StringUtils.equals(otherUser.getName(), this.getName()) &&
					StringUtils.equals(otherUser.getLoginName(), this.getLoginName()) &&
					StringUtils.equals(otherUser.getMobile(), this.getMobile()) &&
					otherUser.getOrganization().equals(this.getOrganization()) &&
					StringUtils.equals(otherUser.getPhone(), this.getPhone()) &&
					StringUtils.equals(otherUser.getType(), this.getType()) &&
					StringUtils.equals(otherUser.getEmail().toLowerCase(), this.getEmail().toLowerCase()) &&
					(otherUser.isEnabled()==this.isEnabled()) && 
					StringUtils.equals(otherUser.getDescription(), this.getDescription()) &&
					(this.getImage()==null || this.getImage().equals(otherUser.getImage()))
					;
		}
		return false;
	}
}