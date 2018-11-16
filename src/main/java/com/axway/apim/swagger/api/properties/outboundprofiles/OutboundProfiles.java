package com.axway.apim.swagger.api.properties.outboundprofiles;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OutboundProfiles {
	
	protected static Logger LOG = LoggerFactory.getLogger(OutboundProfiles.class);
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	
	protected Map<String, OutboundProfile> outboundProfiles;

	public Map<String, OutboundProfile> getOutboundProfiles() {
		return outboundProfiles;
	}


	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof OutboundProfiles) {
			OutboundProfiles otherOutboundProfiles = (OutboundProfiles)other;
			if(otherOutboundProfiles.getOutboundProfiles()==null) return true; // <<< No update, as the property hasn't provided
			if(otherOutboundProfiles.getOutboundProfiles().size()==0) return true; // <<< Nothing to overwrite, as nothing is provided
			if(otherOutboundProfiles.getOutboundProfiles().size()!=this.getOutboundProfiles().size()) return false;
			if(otherOutboundProfiles.getOutboundProfiles().size()==0 && this.getOutboundProfiles().size()==0) return true;
			if(!otherOutboundProfiles.getOutboundProfiles().equals(this.getOutboundProfiles())) return false;
		} else {
			return false;
		}
		return true;
	}
}
