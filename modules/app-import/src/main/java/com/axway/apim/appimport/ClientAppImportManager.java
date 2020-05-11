package com.axway.apim.appimport;

import com.axway.apim.adapter.clientApps.ClientAppAdapter;
import com.axway.apim.api.model.ClientApplication;
import com.axway.apim.lib.errorHandling.AppException;

public class ClientAppImportManager {
	
	private ClientAppAdapter desiredAppAdapter;
	
	private ClientAppAdapter actualAppAdapter;
	
	private ClientApplication desiredApp;
	
	private ClientApplication actualApp;
	
	public ClientAppImportManager(ClientAppAdapter desiredAppAdapter, ClientAppAdapter actualAppAdapter) {
		super();
		this.desiredAppAdapter = desiredAppAdapter;
		this.actualAppAdapter = actualAppAdapter;
	}

	public void replicate() throws AppException {
		actualAppAdapter.createApplication(desiredApp);
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
}
