package com.axway.apim.swagger.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.AppException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Concrete class that is used to reflect the actual API as it's stored in the API-Manager. 
 * On the other hand, the APIImportDefintion reflects the desired state of the API.</br>
 * </br>
 * Both classes extend the AbstractAPIDefinition which contains all the common API-Properties that 
 * are compared property by property in APIChangeState.
 * 
 * @see AbstractAPIDefinition
 * @see APIImportDefinition
 * @author cwiechmann@axway.com
 */
public class APIManagerAPI extends AbstractAPIDefinition implements IAPIDefinition {
	
	static Logger LOG = LoggerFactory.getLogger(APIManagerAPI.class);

	JsonNode apiConfiguration;

	public APIManagerAPI() throws AppException {
		super();
	}

	public APIManagerAPI(JsonNode apiConfiguration) {
		this.apiConfiguration = apiConfiguration;
	}
	
	/**
	 * The tool handles deprecation as an additional state (might not be best choice), but  
	 * the API-Manager internally doesn't. In API-Manager deprecation is just a true/false toggle.</br>
	 * To make Desired & Actual API comparable this method is encapsulating the difference. 
	 * @see com.axway.apim.swagger.api.AbstractAPIDefinition#getState()
	 */
	@Override
	public String getState() throws AppException {
		if(this.deprecated!=null 
				&& this.deprecated.equals("true")) return IAPIDefinition.STATE_DEPRECATED;
		return super.getState();
	}

	@Override
	public void setWsdlURL(String wsdlURL) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getWsdlURL() {
		// TODO Auto-generated method stub
		return null;
	}
}
