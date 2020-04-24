package com.axway.apim.api.state;

/**
 * Not sure if this class is really needed.
 * 
 * @author cwiechmann@axway.com
 */
public class APIBaseDefinition extends AbstractAPI implements IAPI {
	
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

	@Override
	public String getApiDefinitionImport() {
		// TODO Auto-generated method stub
		return null;
	}
}
