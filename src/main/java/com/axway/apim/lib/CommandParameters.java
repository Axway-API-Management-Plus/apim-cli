package com.axway.apim.lib;

import org.apache.commons.cli.CommandLine;

public class CommandParameters {
	
	private static CommandParameters instance;
	
	int port = 8075;
	
	private CommandLine cmd;
	
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

	public String getOptionValue(String option) {
		return this.cmd.getOptionValue(option);
	}

	public String getUsername() {
		return this.cmd.getOptionValue("username");
	}

	public String getPassword() {
		return this.cmd.getOptionValue("password");
	}

	public String getHostname() {
		return this.cmd.getOptionValue("host");
	}

	public int getPort() {
		return port;
	}

	public boolean isEnforceBreakingChange() {
		if(!this.cmd.hasOption("force")) return false;
		return Boolean.parseBoolean(this.cmd.getOptionValue("force"));
	}
	
	public boolean isIgnoreQuotas() {
		if(!this.cmd.hasOption("ignoreQuotas")) return false;
		return Boolean.parseBoolean(this.cmd.getOptionValue("ignoreQuotas"));
	}
	
	public boolean isIgnoreClientOrgs() {
		if(!this.cmd.hasOption("ignoreClientOrgs")) return false;
		return Boolean.parseBoolean(this.cmd.getOptionValue("ignoreClientOrgs"));
	}	
	
	public String getAPIManagerURL() {
		return "https://"+this.getHostname()+":"+this.getPort();
	}
}
