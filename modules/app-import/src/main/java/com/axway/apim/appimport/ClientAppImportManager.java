package com.axway.apim.appimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorState;

public class ClientAppImportManager {
	
	private static Logger LOG = LoggerFactory.getLogger(ClientAppImportManager.class);
	
	@SuppressWarnings("unused")
	private ClientAppAdapter sourceAppAdapter;
	
	private ClientAppAdapter targetAppAdapter;
	
	private ClientApplication desiredApp;
	
	private ClientApplication actualApp;
	
	public ClientAppImportManager(ClientAppAdapter sourceAppAdapter, ClientAppAdapter targetAppAdapter) {
		super();
		this.sourceAppAdapter = sourceAppAdapter;
		this.targetAppAdapter = targetAppAdapter;
	}

	public void replicate() throws AppException {
		if(actualApp==null) {
			targetAppAdapter.createApplication(desiredApp);
		} else if(appsAreEqual(desiredApp, actualApp)) {
			LOG.debug("No changes detected between Desired- and Actual-App. Exiting now...");
			ErrorState.getInstance().setWarning("No changes detected between Desired- and Actual-App.", ErrorCode.NO_CHANGE, false);
			throw new AppException("No changes detected between Desired- and Actual-App.", ErrorCode.NO_CHANGE);			
		} else {
			LOG.debug("Update existing application");
			targetAppAdapter.updateApplication(desiredApp, actualApp);
		}
	}

	public ClientApplication getDesiredApp() {
		return desiredApp;
	}

	public void setDesiredApp(ClientApplication desiredApp) {
		this.desiredApp = desiredApp;
	}

	public ClientApplication getActualApp() {
		return actualApp;
	}

	public void setActualApp(ClientApplication actualApp) {
		this.actualApp = actualApp;
	}
	
	private static boolean appsAreEqual(ClientApplication desiredApp, ClientApplication actualApp) {
		return 
			desiredApp.equals(actualApp) && 
			(desiredApp.getApiAccess()==null || desiredApp.getApiAccess().equals(actualApp.getCredentials()));
	}
}
