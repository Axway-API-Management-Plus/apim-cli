package com.axway.apim.lib;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.APIMHttpClient;

public class EnvironmentProperties {
	
	private static Logger LOG = LoggerFactory.getLogger(EnvironmentProperties.class);
	
	private Properties mainProperties = new Properties();
	private Properties stageProperties = new Properties();;

	public EnvironmentProperties(String stage) throws AppException {
		super();
		initProperties(stage);
	}
	
	private void initProperties(String stage) throws AppException {
		try {
			LOG.info("Loading environment properties from file: env.properties");
			mainProperties.load(APIMHttpClient.class.getClassLoader().getResourceAsStream("env.properties"));
		} catch (Exception e) {
			LOG.debug("Can't read base environment file.");
		}
		try {
			if(stage!=null && !stage.equals("NOT_SET")) {
				LOG.info("Loading environment properties from file: env."+stage+".properties");
				stageProperties.load(APIMHttpClient.class.getClassLoader().getResourceAsStream("env."+stage+".properties"));
			}
		} catch (Exception e) {
			LOG.debug("Can't read environment file.");
		}
	}
	
	public String get(String key) {
		if(stageProperties!=null && stageProperties.containsKey(key)) {
			return stageProperties.getProperty(key);
		} else if(this.mainProperties!=null && mainProperties.containsKey(key)) {
			return mainProperties.getProperty(key);
		} else {
			LOG.debug("Property: '" + key + "' not found.");
			return null;
		}
	}
	
	public boolean containsKey(String key) {
		return (this.mainProperties.containsKey(key) || this.stageProperties.containsKey(key));
	}
}
