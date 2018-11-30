package com.axway.apim.swagger.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.api.properties.APIImage;
import com.axway.apim.swagger.api.properties.APISwaggerDefinion;
import com.axway.apim.swagger.api.properties.corsprofiles.CorsProfile;
import com.axway.apim.swagger.api.properties.inboundprofiles.InboundProfile;
import com.axway.apim.swagger.api.properties.outboundprofiles.OutboundProfile;
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
	
	/**
	 * Compare the actual API-Definition with the given in Def
	 * 
	 * A list of differences is returned, representing the actual differences. 
	 * The returned list contains APIProperty which make knows, if the property is
	 * - changeable on an existing API
	 * - leads to a breaking change
	 * - is non breaking, but would require a new API-Import (ZDD)
	 * 
	 * This List will be used as a working queue in the API-Manager Adapter to 
	 * bring the API-Manager API into the desired state.
	 * @param def
	 * @return
	 */
//	public APIChangeState getChanges(APIDefinition def);
	
	//....
}
