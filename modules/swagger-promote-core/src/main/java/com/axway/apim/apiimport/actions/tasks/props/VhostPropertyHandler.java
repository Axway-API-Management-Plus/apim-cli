package com.axway.apim.apiimport.actions.tasks.props;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.IAPI;
import com.axway.apim.apiimport.actions.tasks.UpdateAPIProxy;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;
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
	
	public void handleVHost(IAPI desiredAPI, IAPI actualAPI) throws AppException {
		handleVHost(desiredAPI, actualAPI, false);
	}
	
	public void handleVHost(IAPI desiredAPI, IAPI actualAPI, boolean forceUpdate) throws AppException {
		if(updateVhost || forceUpdate) {
			if(!APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP3") && actualAPI.getState().equals(IAPI.STATE_UNPUBLISHED)) {
				ErrorState.getInstance().setError("Can't update V-Host to: "+desiredAPI.getVhost()+" on unpublished API!", ErrorCode.CANT_SETUP_VHOST, false);
				throw new AppException("Can't update V-Host to: "+desiredAPI.getVhost()+" on unpublished API!", 
						ErrorCode.CANT_SETUP_VHOST);
			} else {
				LOG.info("Updating V-Host for published API to: " + desiredAPI.getVhost());
				List<String> vhostChange = new Vector<String>() {{ add ("vhost"); }};
				new UpdateAPIProxy(desiredAPI, actualAPI).execute(vhostChange);
			}
		}
	}

	@Override
	public JsonNode handleProperty(IAPI desired, IAPI actual, JsonNode response) throws AppException {
		((ObjectNode) response).put("vhost", desired.getVhost());
		return response;
	}
}