package com.axway.apim.plugins.application;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

import com.axway.apim.plugins.PluginException;
import com.axway.apim.plugins.extenstions.ChangestatePluginExtension;
import com.axway.apim.swagger.APIChangeState;

public class ApplicationPlugin extends Plugin {

	public ApplicationPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}
	
	public static class ApplicationExtension implements ChangestatePluginExtension {

		@Override
		public APIChangeState beforeApplyChanges(APIChangeState changeState) throws PluginException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
