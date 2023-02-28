package com.axway.apim.apiimport;

import com.axway.apim.api.API;
import com.axway.apim.lib.error.AppException;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Concrete class that is used to reflect the actual API as it's stored in the API-Manager. 
 * On the other hand, the APIImportDefintion reflects the desired state of the API.
 * <p>
 * Both classes extend the AbstractAPIDefinition which contains all the common API-Properties that 
 * are compared property by property in APIChangeState.
 * 
 * @see API
 * @see DesiredAPI
 * @author cwiechmann@axway.com
 */
public class ActualAPI extends API {

	/**
	 * The actual state must be stored as given by the API-Manager, as this state must be 
	 * send back during API-Proxy update!
	 */
	@JsonIgnore
	private String actualState;

	public ActualAPI() throws AppException {
		super();
	}

	public String getActualState() {
		return actualState;
	}

	public void setActualState(String actualState) {
		this.actualState = actualState;
	}
}
