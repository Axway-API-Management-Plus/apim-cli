package com.axway.apim.appimport;

import com.axway.apim.api.model.ClientApplication;

public class ClientAppImportManager {
	
	private ClientApplication desiredApp;
	
	private ClientApplication actualApp;

	public ClientAppImportManager() {
		// TODO Auto-generated constructor stub
	}
	
	public void replicate() {
		
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
