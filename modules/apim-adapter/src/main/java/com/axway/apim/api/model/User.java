package com.axway.apim.api.model;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.jackson.OrganizationDeserializer;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("UserFilter")
public class User {
	
	private Boolean intialized = false;
	
	private static Logger LOG = LoggerFactory.getLogger(User.class);
	
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
		if(id==null) initUser();
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Organization getOrganization() {
		if(organization==null) initUser();
		return organization;
	}
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
	public String getOrganizationId() {
		if(organization==null) initUser();
		return this.organization.getId();
	}
	public String getName() {
		if(name==null) initUser();
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		if(description==null) initUser();
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLoginName() {
		if(loginName==null) initUser();
		return loginName;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	public String getEmail() {
		if(email==null) initUser();
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRole() {
		if(role==null) initUser();
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public Boolean isEnabled() {
		if(enabled==null) initUser();
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	public Long getCreatedOn() {
		if(createdOn==null) initUser();
		return createdOn;
	}
	public void setCreatedOn(Long createdOn) {
		this.createdOn = createdOn;
	}
	public String getState() {
		if(state==null) initUser();
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getType() {
		if(type==null) initUser();
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDn() {
		if(dn==null) initUser();
		return dn;
	}
	public void setDn(String dn) {
		this.dn = dn;
	}
	public Image getImage() {
		if(image==null) initUser();
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	
	public String getImageUrl() {
		if(imageUrl==null) initUser();
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getPhone() {
		if(phone==null) initUser();
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getMobile() {
		if(mobile==null) initUser();
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
	
	void setIntialized(Boolean intialized) {
		this.intialized = intialized;
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
	
	private void initUser() {
		if(intialized) return;
		User user;
		try {
			if(this.id!=null) {
				user = APIManagerAdapter.getInstance().userAdapter.getUserForId(this.id);
			} else {
				user = APIManagerAdapter.getInstance().userAdapter.getUserForLoginName(this.loginName);
			}
			if(user==null) {
				throw new AppException("Error initializing user. ", ErrorCode.UNXPECTED_ERROR);
			}
			user.setIntialized(true);
			this.id = user.getId();
			this.dn = user.getDn();
			this.authNUserAttributes = user.getAuthNUserAttributes();
			this.image = user.getImage();
			this.imageUrl = user.getImageUrl();
			this.name = user.getName();
			this.description = user.getDescription();
			this.loginName = user.getLoginName();
			this.email = user.getEmail();
			this.role = user.getRole();
			this.enabled = user.isEnabled();
			this.createdOn = user.getCreatedOn();
			this.state = user.getState();
			this.type = user.getType();
			this.phone = user.getPhone();
			this.mobile = user.getMobile();
		} catch (AppException e) {
			ErrorState.getInstance().setError("Error initializing user. " + e.getMessage(), ErrorCode.UNXPECTED_ERROR);
			LOG.error("Error initializing user. ", e);
		} finally {
			this.intialized = true;
		}
	}
}
