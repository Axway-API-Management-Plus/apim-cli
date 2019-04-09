package com.axway.apim.lib;

import org.apache.commons.cli.CommandLine;

public class CommandParameters {
	
	public static String MODE_REPLACE	= "replace";
	public static String MODE_IGNORE	= "ignore";
	public static String MODE_ADD		= "add";
	
	private static CommandParameters instance;
	
	int port = 8075;
	
	private CommandLine cmd;
	
	private CommandLine internalCmd;
	
	private EnvironmentProperties envProperties;
	
	private CommandParameters () {}
	
	public static synchronized CommandParameters getInstance () {
		if (CommandParameters.instance == null) {
			CommandParameters.instance = new CommandParameters ();
		}
		return CommandParameters.instance;
	}
	
	public void setCmd(CommandLine cmd) {
		this.cmd = cmd;
	}
	
	public void setInternalCmd(CommandLine internalCmd) {
		this.internalCmd = internalCmd;
	}

	public void setEnvironment(EnvironmentProperties environment) {
		this.envProperties = environment;
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
