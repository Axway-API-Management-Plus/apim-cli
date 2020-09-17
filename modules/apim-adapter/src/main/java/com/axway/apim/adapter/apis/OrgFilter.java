package com.axway.apim.adapter.apis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.axway.apim.api.model.Organization;

public class OrgFilter {

	private String id;
	String apiId;
	String description;
	String email;
	boolean enabled;
	String name;
	String phone;
	String development;
	boolean includeImage;

	private List<NameValuePair> filters = new ArrayList<NameValuePair>();

	private OrgFilter() { }

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
		if(name.equals("*")) return;
		this.name = name;
		String op = "eq";
		if(name.startsWith("*") || name.endsWith("*")) {
			op = "like";
			name = name.replace("*", "");
		}
		filters.add(new BasicNameValuePair("field", "name"));
		filters.add(new BasicNameValuePair("op", op));
		filters.add(new BasicNameValuePair("value", name));
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
	
	public void setDevelopment(String development) {
		this.development = development;
	}
	
	public String getDevelopment() {
		return development;
	}
	
	public boolean isIncludeImage() {
		return includeImage;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if(obj instanceof OrgFilter == false) return false;
		OrgFilter other = (OrgFilter)obj;
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
		return "OrgFilter [name=" + name + ", id=" + id + "]";
	}
	
	public boolean filter(Organization org) {
		if(this.development!=null && Boolean.parseBoolean(this.development)!=org.isDevelopment()) { 
			return true;
		}
		return false;
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
		String phone;
		String development;
		
		boolean includeImage;

		List<NameValuePair> filters = new ArrayList<NameValuePair>();

		public Builder() {
			super();
		}

		public OrgFilter build() {
			OrgFilter filter = new OrgFilter();
			filter.setId(this.id);
			filter.setApiId(this.apiId);
			filter.setDescription(this.description);
			filter.setEmail(this.email);
			filter.setEnabled(this.enabled);
			filter.setName(this.name);
			filter.setPhone(this.phone);
			filter.includeImage = this.includeImage;
			filter.setDevelopment(this.development);
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

		public Builder hasPhone(String phone) {
			this.phone = phone;
			return this;
		}

		public Builder isEnabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}
		
		public Builder hasDevelopment(String development) {
			this.development = development;
			return this;
		}
		
		public Builder includeImage(boolean includeImage) {
			this.includeImage = includeImage;
			return this;
		}
	}

}