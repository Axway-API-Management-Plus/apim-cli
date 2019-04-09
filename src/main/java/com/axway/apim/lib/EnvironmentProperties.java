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
			mainProperties.load(APIMHttpClient.class.getClassLoader().getResourceAsStream("env.properties"));
			LOG.info("Loaded environment properties from file: env.properties.");
		} catch (Exception e) {
			LOG.info("Trying to load environment properties from file: env.properties ... not found.");
		}
		try {
			if(stage!=null && !stage.equals("NOT_SET")) {
				stageProperties.load(APIMHttpClient.class.getClassLoader().getResourceAsStream("env."+stage+".properties"));
				LOG.info("Loaded environment properties from file: env."+stage+".properties.");
			}
		} catch (Exception e) {
			LOG.info("Trying to load environment properties from file: env."+stage+".properties ... not found.");
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
