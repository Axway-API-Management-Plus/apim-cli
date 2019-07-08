package com.axway.apim.lib;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.actions.rest.APIMHttpClient;

public class EnvironmentProperties implements Map<String, String> {
	
	private static Logger LOG = LoggerFactory.getLogger(EnvironmentProperties.class);
	
	private Properties mainProperties = new Properties();
	private Properties stageProperties = new Properties();
	private Properties systemProperties = System.getProperties();

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
	
	@Override
	public String get(Object key) {
		if(stageProperties!=null && stageProperties.containsKey(key)) {
			return stageProperties.getProperty((String)key);
		} else if(this.mainProperties!=null && mainProperties.containsKey(key)) {
			return mainProperties.getProperty((String)key);
		} else if(this.systemProperties!=null && systemProperties.containsKey(key)) {
			return systemProperties.getProperty((String)key);
		} else {
			LOG.debug("Property: '" + key + "' not found.");
			return null;
		}
	}
	
	@Override
	public boolean containsKey(Object key) {
		return (this.mainProperties.containsKey(key) || this.stageProperties.containsKey(key) || this.systemProperties.containsKey(key));
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}



	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String put(String key, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<String> values() {
		throw new UnsupportedOperationException();
	}
}
