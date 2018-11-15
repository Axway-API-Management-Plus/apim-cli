package com.axway.apim.swagger.api.properties;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutboundProfiles {
	
	protected static Logger LOG = LoggerFactory.getLogger(OutboundProfiles.class);
	
	protected Map<String, Object> outboundProfiles;

	public Map<String, Object> getOutboundProfiles() {
		return outboundProfiles;
	}


	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof OutboundProfiles) {
			OutboundProfiles otherOutboundProfiles = (OutboundProfiles)other;
			if(otherOutboundProfiles.getOutboundProfiles().size()!=this.getOutboundProfiles().size()) return false;
			if(otherOutboundProfiles.getOutboundProfiles().size()==0 && this.getOutboundProfiles().size()==0) return true;
			if(otherOutboundProfiles.getOutboundProfiles().equals(this.getOutboundProfiles())) return false;
		} else {
			return false;
		}
		return true;
	}
}
