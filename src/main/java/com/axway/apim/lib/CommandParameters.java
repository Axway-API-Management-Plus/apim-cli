package com.axway.apim.lib;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.test.lib.TestIndicator;

public class CommandParameters {
	
	private static Logger LOG = LoggerFactory.getLogger(CommandParameters.class);
	
	public static String MODE_REPLACE	= "replace";
	public static String MODE_IGNORE	= "ignore";
	public static String MODE_ADD		= "add";
	
	private static CommandParameters instance;
	
	int port = 8075;
	
	private CommandLine cmd;
	
	private CommandLine internalCmd;
	
	private EnvironmentProperties envProperties;
	
	public CommandParameters (CommandLine cmd, CommandLine internalCmd, EnvironmentProperties environment) throws AppException {
		this.cmd = cmd;
		this.internalCmd = internalCmd;
		this.envProperties = environment;
		validateRequiredParameters();
		CommandParameters.instance = this;
	}
	
	public static synchronized CommandParameters getInstance() {
		if(TestIndicator.getInstance().isTestRunning()) return null; // Skip this, if executed as a test
		if (CommandParameters.instance == null) {
			LOG.error("CommandParameters has not been initialized.");
			throw new RuntimeException("CommandParameters has not been initialized.");
		}
		return CommandParameters.instance;
	}

	public String getOptionValue(String option) {
		return getValue(option);
	}

	public String getUsername() {
		return getValue("username");
	}

	public String getPassword() {
		return getValue("password");
	}
	
	public String getAdminUsername() {
		return getValue("admin_username");
	}

	public String getAdminPassword() {
		return getValue("admin_password");
	}

	public String getHostname() {
		return getValue("host");
	}

	public int getPort() {
		if(getValue("port")==null) return port;
		return Integer.parseInt(getValue("port"));
	}

	public boolean isEnforceBreakingChange() {
		if(getValue("force")==null) return false;
		return Boolean.parseBoolean(getValue("force"));
	}
	
	public boolean isIgnoreQuotas() {
		if(getValue("ignoreQuotas")==null) return false;
		return Boolean.parseBoolean(getValue("ignoreQuotas"));
	}
	
	public boolean isIgnoreClientApps() {
		if(getClientAppsMode().equals(MODE_IGNORE)) return true;
		return false;
	}
	
	public String getClientAppsMode() {
		if(getValue("clientAppsMode")==null) return MODE_REPLACE;
		return getValue("clientAppsMode").toLowerCase();
	}
	
	public boolean isIgnoreClientOrgs() {
		if(getClientOrgsMode().equals(MODE_IGNORE)) return true;
		return false;
	}
	
	public String getClientOrgsMode() {
		if(getValue("clientOrgsMode")==null) return MODE_REPLACE;
		return getValue("clientOrgsMode").toLowerCase();
	}
	
	public String getAPIManagerURL() {
		return "https://"+this.getHostname()+":"+this.getPort();
	}
	
	public boolean ignoreAdminAccount() {
		if(getValue("ignoreAdminAccount")==null) return false;
		return Boolean.parseBoolean(getValue("ignoreAdminAccount"));
	}
	
	public void validateRequiredParameters() throws AppException {
		ErrorState errors  = ErrorState.getInstance();
		if(getValue("username")==null) errors.setError("Required parameter: 'username' is missing.", ErrorCode.MISSING_PARAMETER, false);
		if(getValue("password")==null) errors.setError("Required parameter: 'password' is missing.", ErrorCode.MISSING_PARAMETER, false);
		if(getValue("host")==null) errors.setError("Required parameter: 'host' is missing.", ErrorCode.MISSING_PARAMETER, false);
		if(errors.hasError) {
			LOG.error("Provide parameters either using Command-Line-Options or in Environment.Properties");
			throw new AppException("Missing required parameters.", ErrorCode.MISSING_PARAMETER);
		}
	}
	
	private String getValue(String key) {
		if(this.cmd.getOptionValue(key)!=null) {
			return this.cmd.getOptionValue(key);
		} else if(this.internalCmd.getOptionValue(key)!=null) {
			return this.internalCmd.getOptionValue(key);
		} else if(this.envProperties.containsKey(key)) {
			return this.envProperties.get(key);
		} else {
			return null;
		}
	}
}
