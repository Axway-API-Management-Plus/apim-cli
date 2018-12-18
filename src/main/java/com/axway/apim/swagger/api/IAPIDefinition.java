package com.axway.apim.swagger.api;

import java.util.List;
import java.util.Map;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.api.properties.APIImage;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
import com.axway.apim.swagger.api.properties.cacerts.CaCert;
import com.axway.apim.swagger.api.properties.corsprofiles.CorsProfile;
import com.axway.apim.swagger.api.properties.inboundprofiles.InboundProfile;
import com.axway.apim.swagger.api.properties.outboundprofiles.OutboundProfile;
import com.axway.apim.swagger.api.properties.quota.APIQuota;
import com.axway.apim.swagger.api.properties.securityprofiles.SecurityProfile;

public interface IAPIDefinition {
	
	public final static String STATE_PUBLISHED = "published";
	public final static String STATE_UNPUBLISHED = "unpublished";
	public final static String STATE_DEPRECATED = "deprecated";
	public final static String STATE_DELETED = "deleted";
	
	public String getVersion();
	
	public String getPath() throws AppException;
	
	public String getState() throws AppException;
	
	public void setState(String state) throws AppException;
	
	public Map<String, OutboundProfile> getOutboundProfiles();
	public Map<String, InboundProfile> getInboundProfiles();
	public List<SecurityProfile> getSecurityProfiles();
	public List<CorsProfile> getCorsProfiles();
	
	public void setInboundProfiles(Map<String, InboundProfile> inboundProfiles);
	
	public void setSecurityProfiles(List<SecurityProfile> securityProfiles);
	
	public Map<String, String> getCustomProperties();
	
	
	public boolean isValid();
	
	public void setValid(boolean valid);
	
	public String getOrgId() throws AppException;
	
	public String getName();
	
	public String getSummary();
	
	public String getVhost();
	
	public String getId() throws AppException;
	
	public String getApiId() throws AppException;
	
	public APIImage getImage();
	
	public Map<String, String[]> getTags();
	
	public APISwaggerDefinion getSwaggerDefinition();
	
	public void setSwaggerDefinition(APISwaggerDefinion swaggerDefinition);
	
	public String getDescriptionType();
	
	public String getDescriptionManual();
	
	public String getDescriptionMarkdown();
	
	public String getDescriptionUrl();
	
	public List<CaCert> getCaCerts();
	
	public APIQuota getSystemQuota();
	
	public APIQuota getApplicationQuota();
}
