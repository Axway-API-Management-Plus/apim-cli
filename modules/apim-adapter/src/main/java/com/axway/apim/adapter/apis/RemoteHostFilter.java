package com.axway.apim.adapter.apis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.model.Organization;
import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.lib.errorHandling.AppException;

public class RemoteHostFilter {

	private String alias;
	private String id;
	private String name;
	private Integer port;
	private Organization organization;

	private RemoteHostFilter() { }
	
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setId(String id) {
		if(id==null) return;
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public void setName(String name) {
		if(name==null) return;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		if(port == null) return;
		this.port = port;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if(obj instanceof RemoteHostFilter == false) return false;
		RemoteHostFilter other = (RemoteHostFilter)obj;
		return (
				StringUtils.equals(other.getId(), this.getId()) && 
				StringUtils.equals(other.getName(), this.getName())
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
	
	public boolean filter(RemoteHost remoteHost) {
		if(this.getName()==null && this.getPort()==null && this.getOrganization()==null && this.getId()==null && this.getAlias()==null) { // Nothing given to filter out.
			return false;
		}
		if(this.getName()!=null) {
			Pattern pattern = Pattern.compile(this.getName().replace("*", ".*"));
			Matcher matcher = pattern.matcher(remoteHost.getName());
			if(!matcher.matches()) return false;
		}
		if(this.getPort()!=null) {
			if(!this.getPort().equals(remoteHost.getPort())) return false;
		}
		if(this.getId()!=null) {
			if(!this.getId().equals(remoteHost.getId())) return false;
		}
		if(this.getOrganization()!=null) {
			Pattern pattern = Pattern.compile(this.getOrganization().getName().replace("*", ".*"));
			Matcher matcher = pattern.matcher(remoteHost.getOrganization().getName());
			if(!matcher.matches()) return false;
		}
		if(this.getAlias()!=null) {
			Pattern pattern = Pattern.compile(this.getAlias().replace("*", ".*"));
			Matcher matcher = pattern.matcher(remoteHost.getAlias());
			if(!matcher.matches()) return false;
		}
		return true;
	}

	public static class Builder {

		String alias;
		String id;
		String name;
		Integer port;
		Organization organization;

		public Builder() {
			super();
		}

		public RemoteHostFilter build() {
			RemoteHostFilter filter = new RemoteHostFilter();
			filter.setAlias(this.alias);
			filter.setId(this.id);
			filter.setName(this.name);
			filter.setPort(this.port);
			filter.setOrganization(this.organization);
			return filter;
		}
		
		public Builder hasAlias(String alias) {
			this.alias = alias;
			return this;
		}

		public Builder hasId(String id) {
			this.id = id;
			return this;
		}

		public Builder hasName(String name) {
			this.name = name;
			return this;
		}
		
		public Builder hasPort(Integer port) {
			this.port = port;
			return this;
		}
		
		public Builder hasOrganization(String organizationName) throws AppException {
			Organization org = APIManagerAdapter.getInstance().orgAdapter.getOrgForName(organizationName);
			this.organization = org;
			return this;
		}
	}

}