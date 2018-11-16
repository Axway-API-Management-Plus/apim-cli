package com.axway.apim.swagger.api.properties.securityprofiles;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SecurityProfiles {
	
	protected static Logger LOG = LoggerFactory.getLogger(SecurityProfiles.class);
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	protected List<SecurityProfile> securityProfiles;

	public List<SecurityProfile> getSecurityProfiles() {
		return securityProfiles;
	}

	public void setSecurityProfiles(List<SecurityProfile> securityProfiles) {
		this.securityProfiles = securityProfiles;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof SecurityProfiles) {
			SecurityProfiles otherSecurityProfiles = (SecurityProfiles)other;
			if(otherSecurityProfiles.getSecurityProfiles()==null) return true; // <<< No update, as the property hasn't provided
			if(otherSecurityProfiles.getSecurityProfiles().size()!=this.getSecurityProfiles().size()) return false;
			if(otherSecurityProfiles.getSecurityProfiles().size()==0 && this.getSecurityProfiles().size()==0) return true;
			if(!otherSecurityProfiles.getSecurityProfiles().equals(this.getSecurityProfiles())) return false;
		} else {
			return false;
		}
		return true;
	}
}
