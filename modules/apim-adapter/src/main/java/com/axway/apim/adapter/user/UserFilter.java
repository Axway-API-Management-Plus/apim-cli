package com.axway.apim.adapter.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class UserFilter {

	private String id;
	String apiId;
	String appId;
	String description;
	String email;
	boolean enabled;
	String name;
	String loginName;
	String phone;

	private List<NameValuePair> filters = new ArrayList<NameValuePair>();

	private UserFilter() { }

	public void setApiId(String apiId) {
		if(apiId==null) return;
		this.apiId = apiId;
		filters.add(new BasicNameValuePair("field", "apiid"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", apiId));
	}

	public void setDescription(String description) {
		if(description==null) return;
		this.description = description;
		filters.add(new BasicNameValuePair("field", "description"));
		filters.add(new BasicNameValuePair("op", "like"));
		filters.add(new BasicNameValuePair("value", description));
	}

	public void setEmail(String email) {
		if(email==null) return;
		this.email = email;
		filters.add(new BasicNameValuePair("field", "email"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", email));
	}

	public void setEnabled(boolean enabled) {
		if(this.enabled==enabled) return;
		this.enabled = enabled;
		filters.add(new BasicNameValuePair("field", "enabled"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", (enabled) ? "enabled" : "disabled"));
	}

	public void setName(String name) {
		if(name==null) return;
		this.name = name;
		filters.add(new BasicNameValuePair("field", "name"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", name));
	}
	
	public void setLoginName(String loginName) {
		if(loginName==null) return;
		this.loginName = loginName;
		filters.add(new BasicNameValuePair("field", "loginName"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", loginName));
	}

	public void setPhone(String phone) {
		if(phone==null) return;
		this.phone = phone;
		filters.add(new BasicNameValuePair("field", "phone"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", phone));
	}

	public List<NameValuePair> getFilters() {
		return filters;
	}

	public String getName() {
		return name;
	}

	public String getLoginName() {
		return loginName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getApiId() {
		return apiId;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if(obj instanceof UserFilter == false) return false;
		UserFilter other = (UserFilter)obj;
		return (
				StringUtils.equals(other.getId(), this.getId()) && 
				StringUtils.equals(other.getName(), this.getName()) &&
				other.isEnabled() == this.isEnabled() &&
				StringUtils.equals(other.getApiId(), this.getApiId())
				);
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		hashCode += (this.id!=null) ? this.id.hashCode() : 0;
		hashCode += (this.name!=null) ? this.name.hashCode() : 0;
		return hashCode;
	}

	@Override
	public String toString() {
		return "UserFilter [name=" + name + ", id=" + id + "]";
	}


	/**
	 * Build an applicationAdapter based on the given configuration
	 */
	public static class Builder {

		String id;
		String apiId;
		String description;
		String email;
		boolean enabled;
		String name;
		String loginName;
		String phone;

		List<NameValuePair> filters = new ArrayList<NameValuePair>();

		public Builder() {
			super();
		}

		public UserFilter build() {
			UserFilter filter = new UserFilter();
			filter.setId(this.id);
			filter.setApiId(this.apiId);
			filter.setDescription(this.description);
			filter.setEmail(this.email);
			filter.setEnabled(this.enabled);
			filter.setName(this.name);
			filter.setLoginName(this.loginName);
			filter.setPhone(this.phone);
			return filter;
		}

		public Builder hasId(String id) {
			this.id = id;
			return this;
		}

		public Builder hasApiId(String apiId) {
			this.apiId = apiId;
			return this;
		}

		public Builder hasDescription(String description) {
			this.description = description;
			return this;
		}

		public Builder hasEmail(String email) {
			this.email = email;
			return this;
		}

		public Builder hasName(String name) {
			this.name = name;
			return this;
		}
		
		public Builder hasLoginName(String loginName) {
			this.loginName = loginName;
			return this;
		}

		public Builder hasPhone(String phone) {
			this.phone = phone;
			return this;
		}

		public Builder inEnabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}
	}

}