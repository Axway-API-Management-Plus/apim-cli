package com.axway.apim.lib;

public class StandardImportParams extends CoreParameters {
	
	private String config;

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}
	
	public static synchronized StandardImportParams getInstance() {
		return (StandardImportParams)CoreParameters.getInstance();
	}

	@Override
	public String toString() {
		return "[" + super.toString() + ", config=" + config + "]";
	}
}