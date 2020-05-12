package com.axway.apim.appimport.lib;

import com.axway.apim.api.model.ClientApplication;

public class ClientAppBuilder {

	private ClientAppBuilder() {
	}
	
	public static class Builder {
		
		String appName;
		
		public ClientApplication build() {
			ClientApplication app = new ClientApplication();
			app.setName(this.appName);
			return app;
		}
		
		public Builder hasName(String name) {
			this.appName = name;
			return this;
		}
	}
}
