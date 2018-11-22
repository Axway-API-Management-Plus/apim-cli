package com.axway.apim.swagger.api;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.GETRequest;
import com.axway.apim.actions.rest.RestAPICall;
import com.axway.apim.lib.APIPropertyAnnotation;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.properties.outboundprofiles.OutboundProfile;
import com.fasterxml.jackson.databind.JsonNode;

public class APIManagerAPI extends AbstractAPIDefinition implements IAPIDefinition {
	
	static Logger LOG = LoggerFactory.getLogger(APIManagerAPI.class);

	JsonNode apiConfiguration;
	
	@APIPropertyAnnotation(isBreaking = true, 
			writableStates = {IAPIDefinition.STATE_UNPUBLISHED})
	protected Map<String, OutboundProfile> outboundProfiles = null;

	public APIManagerAPI() throws AppException {
		super();
	}

	public APIManagerAPI(JsonNode apiConfiguration) {
		this.apiConfiguration = apiConfiguration;
	}
	
	@Override
	public String getState() throws AppException {
		if(this.deprecated!=null 
				&& this.deprecated.equals("true")) return IAPIDefinition.STATE_DEPRECATED;
		return super.getState();
	}
	public Map<String, OutboundProfile> getOutboundProfiles() {
		return outboundProfiles;
	}
	public void setOutboundProfiles(Map<String, OutboundProfile> outboundProfiles) {
		this.outboundProfiles = outboundProfiles;
	}
}
