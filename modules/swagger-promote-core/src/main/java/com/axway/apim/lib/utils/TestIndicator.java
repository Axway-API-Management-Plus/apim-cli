package com.axway.apim.lib.utils;

public class TestIndicator {
	
	private static TestIndicator instance = null;
	
	private boolean isTestRunning = false;

	private TestIndicator() {
		super();
	}
	
	public static TestIndicator getInstance() {
		if(instance == null) {
			instance = new TestIndicator();
		}
		return instance;
	}

	public boolean isTestRunning() {
		return isTestRunning;
	}

	public void setTestRunning(boolean isTestRunning) {
		this.isTestRunning = isTestRunning;
	}
}
