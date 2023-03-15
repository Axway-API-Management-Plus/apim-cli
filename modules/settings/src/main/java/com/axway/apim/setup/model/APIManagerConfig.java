package com.axway.apim.setup.model;

import com.axway.apim.api.model.Alerts;
import com.axway.apim.api.model.Config;
import com.axway.apim.api.model.RemoteHost;
import com.fasterxml.jackson.annotation.JsonFilter;

import java.util.Map;

@JsonFilter("APIManagerConfigFilter")
public class APIManagerConfig {
	
	private Config config;
	
	private Map<String, RemoteHost> remoteHosts;
	
	private Alerts alerts;

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}

	public Map<String, RemoteHost> getRemoteHosts() {
		return remoteHosts;
	}

	public void setRemoteHosts(Map<String, RemoteHost> remoteHosts) {
		this.remoteHosts = remoteHosts;
	}

	public Alerts getAlerts() {
		return alerts;
	}

	public void setAlerts(Alerts alerts) {
		this.alerts = alerts;
	}
}
