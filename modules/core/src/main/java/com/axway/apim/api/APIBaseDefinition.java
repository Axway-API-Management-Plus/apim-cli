package com.axway.apim.api;

/**
 * Not sure if this class is really needed.
 * 
 * @author cwiechmann@axway.com
 */
public class APIBaseDefinition extends API {
	
	private String createdOn;

	@Override
	public String getState() {
		return this.state;
	}

	public void setStatus(String state) {
		this.state = state;
	}
	
	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}
}
