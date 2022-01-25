package com.axway.apim.appexport.lib;

import org.apache.commons.cli.Option;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.errorHandling.AppException;

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

		option = new  Option("orgName", true, "Filter applications to this organization");
		option.setRequired(false);
		option.setArgName("*Partners*");
		addOption(option);
		
		option = new  Option("createdBy", true, "Filter applications created by this user");
		option.setRequired(false);
		option.setArgName("Tom");
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
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to get/export applications with different output formats");
		System.out.println("Get all applications on console using environment properties: env.api-env.properties:");
		System.out.println(getBinaryName()+" app get -s api-env");
		System.out.println("Same as before, but with output format JSON - As it is used to import applications");
		System.out.println(getBinaryName()+" api get -s api-env -o json");
		System.out.println();
		System.out.println();
		System.out.println("How to filter the list of selected applications:");
		System.out.println(getBinaryName()+" api get -s api-env -n \"Client App\" -o json");
		System.out.println(getBinaryName()+" app get -s api-env -n \"Client App\" -t /tmp/exported_apps -o json -deleteTarget ");
		System.out.println(getBinaryName()+" app get -s api-env -n \"App 123\" -t /tmp/exported_apps -o json -deleteTarget");
		System.out.println(getBinaryName()+" app get -s api-env -redirectUrl \"localhost\"");
		System.out.println(getBinaryName()+" app get -s api-env -credential 16378192");
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
