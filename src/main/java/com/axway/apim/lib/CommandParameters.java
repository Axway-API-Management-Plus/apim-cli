package com.axway.apim.lib;

import org.apache.commons.cli.CommandLine;

public class CommandParameters {
	
	public static String MODE_REPLACE	= "replace";
	public static String MODE_IGNORE	= "ignore";
	public static String MODE_ADD		= "add";
	
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
		if(!this.cmd.hasOption("port")) return port;
		return Integer.parseInt(this.cmd.getOptionValue("port"));
	}

	public boolean isEnforceBreakingChange() {
		if(!this.cmd.hasOption("force")) return false;
		return Boolean.parseBoolean(this.cmd.getOptionValue("force"));
	}
	
	public boolean isIgnoreQuotas() {
		if(!this.cmd.hasOption("ignoreQuotas")) return false;
		return Boolean.parseBoolean(this.cmd.getOptionValue("ignoreQuotas"));
	}
	
	public boolean isIgnoreClientApps() {
		if(getClientAppsMode().equals(MODE_IGNORE)) return true;
		return false;
	}
	
	public String getClientAppsMode() {
		if(!this.cmd.hasOption("clientAppsMode")) return MODE_REPLACE;
		return this.cmd.getOptionValue("clientAppsMode").toLowerCase();
	}
	
	public boolean isIgnoreClientOrgs() {
		if(getClientOrgsMode().equals(MODE_IGNORE)) return true;
		return false;
	}
	
	public String getClientOrgsMode() {
		if(!this.cmd.hasOption("clientOrgsMode")) return MODE_REPLACE;
		return this.cmd.getOptionValue("clientOrgsMode").toLowerCase();
	}
	
	public String getAPIManagerURL() {
		return "https://"+this.getHostname()+":"+this.getPort();
	}
}
