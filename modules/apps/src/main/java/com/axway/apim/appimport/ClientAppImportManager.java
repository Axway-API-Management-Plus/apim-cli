package com.axway.apim.appimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.clientApps.APIMgrAppsAdapter;
import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public class ClientAppImportManager {
	
	private static Logger LOG = LoggerFactory.getLogger(ClientAppImportManager.class);
	
	@SuppressWarnings("unused")
	private ClientAppAdapter sourceAppAdapter;
	
	private APIMgrAppsAdapter apiMgrAppAdapter;
	
	private ClientApplication desiredApp;
	
	private ClientApplication actualApp;
	
	public ClientAppImportManager(ClientAppAdapter sourceAppAdapter) throws AppException {
		super();
		this.sourceAppAdapter = sourceAppAdapter;
		this.apiMgrAppAdapter = APIManagerAdapter.getInstance().appAdapter;
	}

	public void replicate() throws AppException {
		if(actualApp==null) {
			apiMgrAppAdapter.createApplication(desiredApp);
		} else if(appsAreEqual(desiredApp, actualApp)) {
			LOG.debug("No changes detected between Desired- and Actual-App. Exiting now...");
			throw new AppException("No changes detected between Desired- and Actual-App.", ErrorCode.NO_CHANGE);			
		} else {
			LOG.debug("Update existing application");
			apiMgrAppAdapter.updateApplication(desiredApp, actualApp);
		}
		return;
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
			(desiredApp.getApiAccess()==null || desiredApp.getApiAccess().equals(actualApp.getApiAccess()));
	}
}
