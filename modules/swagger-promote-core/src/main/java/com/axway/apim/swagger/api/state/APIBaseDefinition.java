package com.axway.apim.swagger.api.state;

/**
 * Not sure if this class is really needed.
 * 
 * @author cwiechmann@axway.com
 */
public class APIBaseDefinition extends AbstractAPI implements IAPI {

	@Override
	public String getState() {
		return this.state;
	}

	public void setStatus(String state) {
		this.state = state;
	}
	
	@Override
	public String getApiDefinitionImport() {
		// TODO Auto-generated method stub
		return null;
	}
}
