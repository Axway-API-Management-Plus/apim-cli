package com.axway.apim.api.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Concrete class that is used to reflect the actual API as it's stored in the API-Manager. 
 * On the other hand, the APIImportDefintion reflects the desired state of the API.
 * 
 * Both classes extend the AbstractAPIDefinition which contains all the common API-Properties that 
 * are compared property by property in APIChangeState.
 * 
 * @see AbstractAPI
 * @see DesiredAPI
 * @author cwiechmann@axway.com
 */
public class ActualAPI extends AbstractAPI implements IAPI {
	
	static Logger LOG = LoggerFactory.getLogger(ActualAPI.class);

	JsonNode apiConfiguration;

	public ActualAPI() throws AppException {
		super();
	}

	public ActualAPI(JsonNode apiConfiguration) {
		this.apiConfiguration = apiConfiguration;
	}
	
	/**
	 * The tool handles deprecation as an additional state (might not be best choice), but  
	 * the API-Manager internally doesn't. In API-Manager deprecation is just a true/false toggle.
	 * To make Desired and Actual API comparable this method is encapsulating the difference. 
	 * @see com.axway.apim.api.state.AbstractAPI#getState()
	 */
	@Override
	public String getState() throws AppException {
		if(this.deprecated!=null 
				&& this.deprecated.equals("true")) return IAPI.STATE_DEPRECATED;
		return super.getState();
	}

	@Override
	public String getApiDefinitionImport() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public JsonNode getApiConfiguration() {
		return apiConfiguration;
	}

	public void setApiConfiguration(JsonNode apiConfiguration) {
		this.apiConfiguration = apiConfiguration;
	}
}
