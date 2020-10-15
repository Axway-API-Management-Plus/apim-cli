package com.axway.apim.api.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFilter;

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
