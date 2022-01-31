package com.axway.apim.users.lib.cli;

import org.apache.commons.cli.Option;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.users.lib.params.UserFilterParams;

public class CLIUserFilterOptions extends CLIOptions {
	
	private CLIOptions cliOptions;

	public CLIUserFilterOptions(CLIOptions cliOptions) {
		super();
		this.cliOptions = cliOptions;
	}

	@Override
	public Parameters getParams() throws AppException {
		UserFilterParams params = (UserFilterParams)cliOptions.getParams();
		params.setName(getValue("name"));
		params.setLoginName(getValue("loginName"));
		params.setEmail(getValue("email"));
		params.setType(getValue("type"));
		params.setOrg(getValue("org"));
		params.setRole(getValue("role"));
		params.setState(getValue("state"));
		if(getValue("enabled")!=null) {
			params.setEnabled(Boolean.parseBoolean(getValue("enabled")));
		}
		params.setId(getValue("id"));
		
		return (Parameters) params;
	}

	@Override
	public void parse() {
		cliOptions.parse();
	}

	@Override
	public void addOption(Option option) {
		cliOptions.addOption(option);
	}

	@Override
	public void addInternalOption(Option option) {
		cliOptions.addInternalOption(option);
	}
	
	@Override
	public String getValue(String key) {
		return cliOptions.getValue(key);
	}

	@Override
	public boolean hasOption(String key) {
		return cliOptions.hasOption(key);
	}

	@Override
	public void printUsage(String message, String[] args) {
		cliOptions.printUsage(message, args);
	}

	@Override
	public void showReturnCodes() {
		cliOptions.showReturnCodes();
	}

	@Override
	public void addOptions() {
		Option option = new  Option("loginName", true, "Filter users with the specified login-name. You may use wildcards at the end or beginning.");
		option.setRequired(false);
		option.setArgName("*mark24*");
		addOption(option);
		
		option = new  Option("n", "name", true, "Filter users with the specified name. You may use wildcards at the end or beginning.");
		option.setRequired(false);
		option.setArgName("*Mark*");
		addOption(option);
		
		option = new  Option("email", true, "Filter users with the specified Email-Address. You may use wildcards at the end or beginning.");
		option.setRequired(false);
		option.setArgName("*@axway.com*");
		addOption(option);
		
		option = new  Option("type", true, "Filter users with specific type. External users are managed in external system such as LDAP");
		option.setRequired(false);
		option.setArgName("internal|external");
		addOption(option);
		
		option = new  Option("org", true, "Filter users belonging to specified organization. You may use wildcards at the end or beginning.");
		option.setRequired(false);
		option.setArgName("*Partner*");
		addOption(option);
		
		option = new  Option("role", true, "Filter users with the given role. ");
		option.setRequired(false);
		option.setArgName("user|oadmin|admin");
		addOption(option);
		
		option = new  Option("state", true, "Filter users with the given state. ");
		option.setRequired(false);
		option.setArgName("approved|pending");
		addOption(option);
		
		option = new  Option("enabled", true, "Filter users based on the enablement flag. By default enabled users are include by default.");
		option.setRequired(false);
		option.setArgName("true|false");
		addOption(option);
		
		option = new  Option("id", true, "Filter users with that specific ID.");
		option.setRequired(false);
		option.setArgName("UUID-ID-OF-THE-USER");
		addOption(option);
		
		cliOptions.addOptions();
	}

	@Override
	public EnvironmentProperties getEnvProperties() {
		return this.cliOptions.getEnvProperties();
	}
}
