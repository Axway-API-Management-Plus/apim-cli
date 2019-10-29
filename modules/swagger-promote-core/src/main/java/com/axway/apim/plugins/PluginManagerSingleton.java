package com.axway.apim.plugins;

import java.nio.file.Path;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginManagerSingleton {
	
	private static Logger LOG = LoggerFactory.getLogger(PluginManagerSingleton.class);
	
	private static org.pf4j.PluginManager instance = null;
	
	public static PluginManager getInstance() {
		return getInstance(null);
	}
	
	public static PluginManager getInstance(Path pluginPath) {
		if(instance==null) {
			if(pluginPath==null) {
				instance = new DefaultPluginManager();
			} else {
				instance = new DefaultPluginManager(pluginPath);
			}
			instance.loadPlugins();
		}
		return instance;
	}
}
