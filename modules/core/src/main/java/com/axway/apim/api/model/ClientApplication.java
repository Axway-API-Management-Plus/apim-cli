package com.axway.apim.api.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientApplication {
	private String id;
	private String name;
	private String oauthClientId;
	private String extClientId;
	private String apiKey;
	
	private List<APIAccess> apiAccess;
	
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
		this.apiKey = apiKey;
	}
	
	public String getOrganizationId() throws AppException {
		if(this.organizationId==null) this.organizationId = APIManagerAdapter.getInstance().getOrgId(this.organization);
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
