package com.axway.apim.test.plugins;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.plugins.PluginManagerSingleton;
import com.axway.apim.plugins.TestPlugin.TestPluginImpl;
import com.axway.apim.plugins.extenstions.DesiredAPIPluginExtension;

public class PluginManagerTest {

	@Test
	public void testTestPlugin() throws AppException, IOException, URISyntaxException {
		Path testPlugins = new File(Thread.currentThread().getContextClassLoader().getResource("plugins").toURI()).toPath();
		PluginManager manager = PluginManagerSingleton.getInstance(testPlugins);
		List<PluginWrapper> plugins =  manager.getPlugins();
		assertNotNull(plugins, "Plugins should not be null");
		assertTrue(plugins.size()!=0, "No Plugins loaded");

		List<DesiredAPIPluginExtension> extensions = manager.getExtensions(DesiredAPIPluginExtension.class);
		assertNotNull(plugins, "Plugins should not be null");
		assertTrue(plugins.size()!=0, "No Plugins loaded");
		assertTrue(isExtensionLoaded(extensions, TestPluginImpl.class), "TestPluginImpl has not been loaded");
	}

	private <T, C> boolean isExtensionLoaded(List<T> extensions, Class<C> xyz) {
		for(T extension : extensions) {
			if(xyz.isInstance(extension)) {
				return true;
			}
		}
		return false;
	}
}
