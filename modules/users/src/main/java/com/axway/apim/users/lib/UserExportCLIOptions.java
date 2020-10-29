package com.axway.apim.users.lib;

import org.apache.commons.cli.Option;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.errorHandling.AppException;

public class UserExportCLIOptions extends CLIOptions {

	private UserExportCLIOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) {
		CLIOptions cliOptions = new UserExportCLIOptions(args);
		cliOptions = new StandardExportCLIOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
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
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to get/export user with different output formats");
		System.out.println("Get all users on console using environment properties: env.api-env.properties:");
		System.out.println(getBinaryName()+" user get -s api-env");
		System.out.println("Same as before, but with output format JSON - As it is used to import applications");
		System.out.println(getBinaryName()+" user get -s api-env -o json");
		System.out.println();
		System.out.println();
		System.out.println("How to filter the list of selected users:");
		System.out.println(getBinaryName()+" user get -s api-env -n \"Mark*\" -o json");
		System.out.println(getBinaryName()+" user get -s api-env -n \"Mike*\" -role admin -o json");
		System.out.println(getBinaryName()+" user get -s api-env -n \"*Marcel*\" -t /tmp/exported_apps -o json -deleteTarget ");
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Application-Export";
	}
	
	@Override
	public Parameters getParams() throws AppException {
		UserExportParams params = new UserExportParams();

		params.setId(getValue("id"));
		params.setLoginName(getValue("loginName"));
		params.setName(getValue("name"));
		params.setEmail(getValue("email"));
		params.setType(getValue("type"));
		params.setOrg(getValue("org"));
		params.setRole(getValue("role"));
		params.setState(getValue("state"));
		if(getValue("enabled")!=null) params.setEnabled(Boolean.parseBoolean(getValue("enabled")));
		return params;
	}
}
