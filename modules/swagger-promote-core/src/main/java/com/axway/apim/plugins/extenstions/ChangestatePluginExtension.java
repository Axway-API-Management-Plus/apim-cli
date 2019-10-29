package com.axway.apim.plugins.extenstions;

import org.pf4j.ExtensionPoint;

import com.axway.apim.plugins.PluginException;
import com.axway.apim.swagger.APIChangeState;

public interface ChangestatePluginExtension extends ExtensionPoint {
	
	/**
	 * Is called when the Actual- & Desired API has been loaded from the API-Manager, but before 
	 * changes are replicated. 
	 * @param changeState contains the Desired-, Actual-API plus what changes are required 
	 * @return the change state of null if nothing has changed
	 */
	APIChangeState beforeApplyChanges(APIChangeState changeState) throws PluginException;;
}
