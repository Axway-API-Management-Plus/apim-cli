package com.axway.apim.plugins.extenstions;

import org.pf4j.ExtensionPoint;

import com.axway.apim.plugins.PluginException;
import com.axway.apim.swagger.APIChangeState;

public interface PostProcessPluginExtension extends ExtensionPoint {
	
	/**
	 * Is called when the Actual API has been loaded from the API-Manager.
	 * @param actualAPI represents the API in API-Manager.
	 * @return the actualAPI or null, if you haven't changed anything
	 */
	void postProcess(APIChangeState changeState) throws PluginException;
}
