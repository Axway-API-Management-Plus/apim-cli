package com.axway.apim.users.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.StandardExportCLIOptions;

public class UserExportCLIOptions extends StandardExportCLIOptions {

	CommandLine cmd;

	public UserExportCLIOptions(String[] args) throws ParseException {
		super(args);
		Option option = new  Option("loginName", true, "Filter users with the specified login-name. You may use wildcards at the end or beginning.");
		option.setRequired(false);
		option.setArgName("*mark24*");
		options.addOption(option);
		
		option = new  Option("name", true, "Filter users with the specified name. You may use wildcards at the end or beginning.");
		option.setRequired(false);
		option.setArgName("*Mark*");
		options.addOption(option);
		
		option = new  Option("type", true, "Filter users with specific type. External users are managed in external system such as LDAP");
		option.setRequired(false);
		option.setArgName("internal|external");
		options.addOption(option);
		
		option = new  Option("org", true, "Filter users belonging to specified organization. You may use wildcards at the end or beginning.");
		option.setRequired(false);
		option.setArgName("*Partner*");
		options.addOption(option);
		
		option = new  Option("role", true, "Filter users with the given role. ");
		option.setRequired(false);
		option.setArgName("user|oadmin|admin");
		options.addOption(option);
		
		option = new  Option("state", true, "Filter users with the given state. ");
		option.setRequired(false);
		option.setArgName("approved|pending");
		options.addOption(option);
		
		option = new  Option("enabled", true, "Filter users based on the enablement flag. By default enabled users are include by default.");
		option.setRequired(false);
		option.setArgName("true|false");
		options.addOption(option);
		
		option = new  Option("id", true, "Filter users with that specific ID.");
		option.setRequired(false);
		option.setArgName("UUID-ID-OF-THE-USER");
		options.addOption(option);
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
}
