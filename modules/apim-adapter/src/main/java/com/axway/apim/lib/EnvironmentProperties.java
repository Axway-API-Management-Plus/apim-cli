package com.axway.apim.lib;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
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
	
	public EnvironmentProperties(String stage, String swaggerPromoteHome) {
		super();
		this.stage = stage;
		this.swaggerPromoteHome = swaggerPromoteHome;
		if(swaggerPromoteHome==null) {
			// Try to use SWAGGER_PROMOTE_HOME if not given by a parameter
			this.swaggerPromoteHome = System.getenv(CoreParameters.APIM_CLI_HOME);
		}
		if(this.swaggerPromoteHome!=null) this.swaggerPromoteHome += "/conf";
		initProperties();
	}
	
	private void initProperties() {
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
			props.load(new StringReader(IOUtils.toString(is, "UTF-8").replace("\\", "\\\\")));
			LOG.debug("Loaded environment properties from file: " + pathToUse);
		} catch (Exception e) {
			LOG.debug("Trying to load environment properties from file: "+pathToUse+" ... not found.");
		}
		return props;
	}
	
	@Override
	public String get(Object key) {
		if(stageProperties!=null && stageProperties.containsKey(key)) {
			return resolveValueWithEnvVars(stageProperties.getProperty((String)key));
		} else if(this.mainProperties!=null && mainProperties.containsKey(key)) {
			return resolveValueWithEnvVars(mainProperties.getProperty((String)key));
		} else if(this.systemProperties!=null && systemProperties.containsKey(key)) {
			return resolveValueWithEnvVars(systemProperties.getProperty((String)key));
		} else {
			return null;
		}
	}
	
	private static String resolveValueWithEnvVars(String value) {
		if (null == value) {
			return null;
		}
		if(value.indexOf("${")==-1) {
			return value;
		}

		Pattern p = Pattern.compile("\\$\\{(\\w+)\\}|\\$(\\w+)");
		Matcher m = p.matcher(value);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String envVarName = null == m.group(1) ? m.group(2) : m.group(1);
			String envVarValue = System.getenv(envVarName);
			m.appendReplacement(sb, null == envVarValue ? "" : Matcher.quoteReplacement(envVarValue));
		}
		m.appendTail(sb);
		return sb.toString();
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
		this.mainProperties.put(key, value);
		return key;
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
