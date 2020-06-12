package com.axway.apim.apiimport.actions;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;

public class VHostManager {
	static Logger LOG = LoggerFactory.getLogger(VHostManager.class);
	
	boolean updateVhost = false;
	
	public void handleVHost(API desiredAPI, API actualAPI) throws AppException {
		handleVHost(desiredAPI, actualAPI, false);
	}
	
	public void handleVHost(API desiredAPI, API actualAPI, boolean forceUpdate) throws AppException {
		if(updateVhost || forceUpdate) {
			if(!APIManagerAdapter.hasAPIManagerVersion("7.6.2 SP3") && actualAPI.getState().equals(API.STATE_UNPUBLISHED)) {
				ErrorState.getInstance().setError("Can't update V-Host to: "+desiredAPI.getVhost()+" on unpublished API!", ErrorCode.CANT_SETUP_VHOST, false);
				throw new AppException("Can't update V-Host to: "+desiredAPI.getVhost()+" on unpublished API!", 
						ErrorCode.CANT_SETUP_VHOST);
			} else {
				LOG.info("Updating V-Host for published API to: " + desiredAPI.getVhost());
				List<String> vhostChange = new Vector<String>() {{ add ("vhost"); }};
				APIManagerAdapter.getInstance().apiAdapter.updateAPIProxy(desiredAPI);//.execute(vhostChange);
			}
		}
	}
}
