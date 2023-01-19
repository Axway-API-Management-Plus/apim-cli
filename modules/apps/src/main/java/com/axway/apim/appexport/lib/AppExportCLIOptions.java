package com.axway.apim.appexport.lib;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.Option;

public class AppExportCLIOptions extends CLIOptions {

	private AppExportCLIOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) {
		CLIOptions cliOptions = new AppExportCLIOptions(args);
		cliOptions = new StandardExportCLIOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public void addOptions() {
		// Define command line options required for Application export
		Option option = new  Option("n", "name", true, "Filter applications with the specified name. You may use wildcards at the end or beginning.");
		option.setRequired(false);
		option.setArgName("*My Great App*");
		addOption(option);
		
		option = new  Option("id", true, "Filter the export to an application with that specific ID.");
		option.setRequired(false);
		option.setArgName("UUID-ID-OF-THE-APP");
		addOption(option);

		option = new  Option("state", true, "Filter applications to the specificied state: pending | approved");
		option.setRequired(false);
		option.setArgName("pending");
		addOption(option);

		option = new  Option("orgName", true, "Filter for applications to this organization");
		option.setRequired(false);
		option.setArgName("*Partners*");
		addOption(option);
		
		option = new  Option("createdBy", true, "Filter for applications created by this user based on the login-name");
		option.setRequired(false);
		option.setArgName("tom");
		addOption(option);
		
		option = new  Option("api", true, "Filter applications having access to this API. You may use wildcards at the end or beginning.");
		option.setRequired(false);
		option.setArgName("*MyAPI*");
		addOption(option);
		
		option = new  Option("credential", true, "Filter applications having this credential information. Client-ID and API-Key is considered here.");
		option.setRequired(false);
		option.setArgName("*9877979779*");
		addOption(option);
		
		option = new  Option("redirectUrl", true, "Filter applications having this Redirect-URL. Only OAuth-Credentials are considered.");
		option.setRequired(false);
		option.setArgName("*localhost*");
		addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		Console.println("----------------------------------------------------------------------------------------");
		Console.println("How to get/export applications with different output formats");
		Console.println("Get all applications on console using environment properties: env.api-env.properties:");
		Console.println(getBinaryName()+" app get -s api-env");
		Console.println("Same as before, but with output format JSON - As it is used to import applications");
		Console.println(getBinaryName()+" api get -s api-env -o json");
		Console.println();
		Console.println();
		Console.println("How to filter the list of selected applications:");
		Console.println(getBinaryName()+" api get -s api-env -n \"Client App\" -o json");
		Console.println(getBinaryName()+" app get -s api-env -n \"Client App\" -t /tmp/exported_apps -o json -deleteTarget ");
		Console.println(getBinaryName()+" app get -s api-env -n \"App 123\" -t /tmp/exported_apps -o json -deleteTarget");
		Console.println(getBinaryName()+" app get -s api-env -redirectUrl \"localhost\"");
		Console.println(getBinaryName()+" app get -s api-env -credential 16378192");
		Console.println();
		Console.println("For more information and advanced examples please visit:");
		Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Application-Export";
	}
	
	@Override
	public Parameters getParams() {
		AppExportParams params = new AppExportParams();
		params.setName(getValue("name"));
		params.setId(getValue("id"));
		params.setState(getValue("state"));
		params.setOrgName(getValue("orgName"));
		params.setCreatedBy(getValue("createdBy"));
		params.setCredential(getValue("credential"));
		params.setRedirectUrl(getValue("redirectUrl"));
		params.setApiName(getValue("api"));
		return params;
	}
}
