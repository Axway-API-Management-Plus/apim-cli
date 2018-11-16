package com.axway.apim.swagger.api.properties.corsprofiles;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CorsProfiles {
	
	protected static Logger LOG = LoggerFactory.getLogger(CorsProfiles.class);
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	protected List<CorsProfile> corsProfiles;

	public List<CorsProfile> getCorsProfile() {
		return corsProfiles;
	}

	public void setCorsProfiles(List<CorsProfile> corsProfiles) {
		this.corsProfiles = corsProfiles;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof CorsProfiles) {
			CorsProfiles otherCorsProfiles = (CorsProfiles)other;
			if(otherCorsProfiles.getCorsProfile()==null) return true; // <<< No update, as the property hasn't provided 
			if(otherCorsProfiles.getCorsProfile().size()!=this.getCorsProfile().size()) return false;
			if(otherCorsProfiles.getCorsProfile().size()==0 && this.getCorsProfile().size()==0) return true;
			if(!otherCorsProfiles.getCorsProfile().equals(this.getCorsProfile())) return false;
		} else {
			return false;
		}
		return true;
	}
}
