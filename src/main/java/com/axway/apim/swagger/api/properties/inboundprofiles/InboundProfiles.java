package com.axway.apim.swagger.api.properties.inboundprofiles;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.swagger.api.properties.outboundprofiles.OutboundProfiles;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InboundProfiles {
	
	protected static Logger LOG = LoggerFactory.getLogger(InboundProfiles.class);
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	protected Map<String, InboundProfile> inboundProfiles;

	public Map<String, InboundProfile> getInboundProfiles() {
		return inboundProfiles;
	}

	public void setInboundProfiles(Map<String, InboundProfile> inboundProfiles) {
		this.inboundProfiles = inboundProfiles;
	}

	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof InboundProfiles) {
			InboundProfiles otherInboundProfiles = (InboundProfiles)other;
			if(otherInboundProfiles.getInboundProfiles()==null) return true; // <<< No update, as the property hasn't provided
			if(otherInboundProfiles.getInboundProfiles().size()!=this.getInboundProfiles().size()) return false;
			if(otherInboundProfiles.getInboundProfiles().size()==0 && this.getInboundProfiles().size()==0) return true;
			if(!otherInboundProfiles.getInboundProfiles().equals(this.getInboundProfiles())) return false;
		} else {
			return false;
		}
		return true;
	}
}
