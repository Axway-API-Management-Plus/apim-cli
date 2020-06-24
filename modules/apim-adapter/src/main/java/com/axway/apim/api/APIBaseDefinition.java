package com.axway.apim.api;

/**
 * Not sure if this class is really needed.
 * 
 * @author cwiechmann@axway.com
 */
public class APIBaseDefinition extends API {

	@Override
	public String getState() {
		return this.state;
	}

	public void setStatus(String state) {
		this.state = state;
	}
}
