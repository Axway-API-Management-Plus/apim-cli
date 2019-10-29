package com.axway.apim.plugins;

import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

import com.axway.apim.plugins.extenstions.DesiredAPIPluginExtension;
import com.axway.apim.swagger.api.state.IAPI;

public class TestPlugin extends Plugin {
	
	public TestPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }
	
	@Extension
	public static class TestPluginImpl implements DesiredAPIPluginExtension {

		@Override
		public IAPI preProcessDesiredAPI(IAPI desiredAPI) {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
