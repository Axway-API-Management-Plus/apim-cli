package com.axway.apim.actions.tasks.props;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.tasks.UpdateAPIProxy;
import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.swagger.api.IAPIDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class VhostPropertyHandler implements PropertyHandler {
	
	static Logger LOG = LoggerFactory.getLogger(VhostPropertyHandler.class);
	
	boolean updateVhost = false;
	
	public VhostPropertyHandler() {
		super();
	}

	public VhostPropertyHandler(List<String> changedProps) {
		if(changedProps.contains("vhost")) {
			updateVhost = true;
			// Make sure, Vhost isn't updated with all the other properties
			changedProps.remove("vhost");
		}
	}
	
	public void handleVHost(IAPIDefinition desiredAPI, IAPIDefinition actualAPI) throws AppException {
		if(updateVhost) {
			if(actualAPI.getStatus().equals(IAPIDefinition.STATE_UNPUBLISHED)) {
				throw new AppException("Can't update V-Host to: "+desiredAPI.getVhost()+" on unpublished API!", 
						ErrorCode.CANT_SETUP_VHOST, false);
			} else {
				LOG.info("Updating V-Host for published API to: " + desiredAPI.getVhost());
				List<String> vhostChange = new Vector<String>() {{ add ("vhost"); }};
				new UpdateAPIProxy(desiredAPI, actualAPI).execute(vhostChange);
			}
		}
	}

	@Override
	public JsonNode handleProperty(IAPIDefinition desired, JsonNode response) throws AppException {
		((ObjectNode) response).put("vhost", desired.getVhost());
		return response;
	}
}
