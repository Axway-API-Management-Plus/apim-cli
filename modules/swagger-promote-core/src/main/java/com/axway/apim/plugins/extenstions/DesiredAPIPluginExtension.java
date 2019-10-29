package com.axway.apim.plugins.extenstions;

import org.pf4j.ExtensionPoint;

import com.axway.apim.plugins.PluginException;
import com.axway.apim.swagger.api.state.IAPI;

public interface DesiredAPIPluginExtension extends ExtensionPoint {
	
	/**
	 * Is called right at the beginning when the DesiredAPI has been loaded/created
	 * @param desiredAPI is the API how it has been created/loaded
	 * @return the desiredAPI or null, if you haven't changed anything
	 */
	IAPI preProcessDesiredAPI(IAPI desiredAPI) throws PluginException;;
}
