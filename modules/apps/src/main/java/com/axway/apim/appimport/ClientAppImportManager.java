package com.axway.apim.appimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.clientApps.APIMgrAppsAdapter;
import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.api.model.apps.ApplicationPermission;
import com.axway.apim.api.model.apps.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public class ClientAppImportManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(ClientAppImportManager.class);

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
		} else { // Existing application
			// If an application is created by an OrgAdmin he automatically get permission to it's own application
			// This might not be part the desired state, hence it must be taken over
			copyAppCreatedByPermission(desiredApp, actualApp);
			if(appsAreEqual(desiredApp, actualApp)) {
				LOG.debug("No changes detected between Desired- and Actual-App. Exiting now...");
				throw new AppException("No changes detected between Desired- and Actual-App.", ErrorCode.NO_CHANGE);			
			} else {
				LOG.info("Update existing application: " + actualApp.getName() + " ("+actualApp.getId()+")");
				apiMgrAppAdapter.updateApplication(desiredApp, actualApp);
			}
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
			(desiredApp.getApiAccess()==null || desiredApp.getApiAccess().equals(actualApp.getApiAccess())) && 
			(desiredApp.getPermissions()==null || desiredApp.getPermissions().containsAll(actualApp.getPermissions()))
			;
	}
	
	private void copyAppCreatedByPermission(ClientApplication desiredApp, ClientApplication actualApp) {
		// Only required, if app has some permissions
		if(actualApp.getPermissions()==null) return;
		for(ApplicationPermission actualPerm : actualApp.getPermissions()) {
			// Check if the same userId created the application, also has a permission object
			// This user is considered as the "default" desired state, 
			if(actualPerm.getUserId().equals(actualApp.getCreatedBy())) {
				boolean found = false;
				// if not already part of the desired state
				for(ApplicationPermission desiredPerm : desiredApp.getPermissions()) {
					// Check each given desired permission
					if(desiredPerm.getUserId().equals(actualPerm.getUserId())) {
						found = true;
						break;
					}
				}
				if(!found) {
					LOG.info("Taking over permission of the user ("+actualPerm.getUserId()+") initially created the application.");
					desiredApp.getPermissions().add(actualPerm);
					return;
				}
			}
		}
	}
}
