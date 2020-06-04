package com.axway.apim.lib;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.rest.APIMHttpClient;

public class EnvironmentProperties implements Map<String, String> {
	
	private static Logger LOG = LoggerFactory.getLogger(EnvironmentProperties.class);
	
	private String stage;
	private String swaggerPromoteHome;
	
	private Properties mainProperties = new Properties();
	private Properties stageProperties = new Properties();
	private Properties systemProperties = System.getProperties();

	public EnvironmentProperties(String stage) throws AppException {
		this(stage, null);
	}
	
	public EnvironmentProperties(String stage, String swaggerPromoteHome) throws AppException {
		super();
		this.stage = stage;
		this.swaggerPromoteHome = swaggerPromoteHome;
		if(swaggerPromoteHome==null) {
			// Try to use SWAGGER_PROMOTE_HOME if not given by a parameter
			this.swaggerPromoteHome = System.getenv(CommandParameters.APIM_CLI_HOME);
		}
		if(this.swaggerPromoteHome!=null) this.swaggerPromoteHome += "/conf";
		initProperties();
	}
	
	private void initProperties() throws AppException {
		mainProperties = loadProperties(null);

		if(stage!=null && !stage.equals("NOT_SET")) {
			stageProperties = loadProperties(stage);
		}
	}
	
	private Properties loadProperties(String stage) {
		/*
		 * We load properties in the following order:
		 * SwaggerPromote Home is used
		 * if ConfDir is not set
		 * if ConfDir is not set, the Classpath is used
		 */
		String pathToUse = null;
		InputStream is;
		Properties props = new Properties();
		try {
			if(swaggerPromoteHome!=null) {
				pathToUse = (stage==null) ? swaggerPromoteHome + "/env.properties" : swaggerPromoteHome + "/env."+stage+".properties";
				is = new FileInputStream(pathToUse);
			} else {
				pathToUse = (stage==null) ? "env.properties" : "env."+stage+".properties";
				is = APIMHttpClient.class.getClassLoader().getResourceAsStream(pathToUse);
			}
			props.load(is);
			LOG.info("Loaded environment properties from file: " + pathToUse);
		} catch (Exception e) {
			LOG.info("Trying to load environment properties from file: "+pathToUse+" ... not found.");
		}
		return props;
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
