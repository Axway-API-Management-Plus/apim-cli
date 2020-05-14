package com.axway.apim.adapter.apis;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class OrgFilter {
	
	String id;
	String apiId;
	String description;
	String email;
	boolean enabled;
	String name;
	String phone;
	
	List<NameValuePair> filters = new ArrayList<NameValuePair>();

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
		filters.add(new BasicNameValuePair("op", "eq"));
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
		this.enabled = enabled;
		filters.add(new BasicNameValuePair("field", "email"));
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
	
	public void setPhone(String phone) {
		if(phone==null) return;
		this.phone = phone;
		filters.add(new BasicNameValuePair("field", "phone"));
		filters.add(new BasicNameValuePair("op", "eq"));
		filters.add(new BasicNameValuePair("value", phone));
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
		
		List<NameValuePair> filters = new ArrayList<NameValuePair>();
		
		/**
		 * @param config the config that is used what kind of adapter should be used
		 */
		public Builder() {
			super();
		}
		
		public OrgFilter build() {
			OrgFilter filter = new OrgFilter();
			filter.id = this.id;
			filter.apiId = this.apiId;
			filter.description = this.description;
			filter.email = this.email;
			filter.enabled = this.enabled;
			filter.name = this.name;
			filter.phone = this.phone;
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
		
		public Builder inEnabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}
	}

}