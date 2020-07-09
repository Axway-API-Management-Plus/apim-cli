package com.axway.apim.users.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.APIMCoreCLIOptions;

public class UserImportCLIOptions extends APIMCoreCLIOptions {

	CommandLine cmd;

	public UserImportCLIOptions(String[] args) throws ParseException {
		super(args);
		// Define command line options required for Application export
		Option option = new Option("c", "config", true, "This is the JSON-Formatted Organization-Config file containing the organization. You may get that config file using apim org get with output set to JSON.");
		option.setRequired(true);
		option.setArgName("org_config.json");
		options.addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);		
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to imports organizations using the JSON-Config format");
		System.out.println("Import an organization using enviornment properties: env.api-env.properties:");
		System.out.println(getBinaryName()+" org import -c myOrgs/partner-org.json -s api-env");
		System.out.println(getBinaryName()+" org import -c myOrgs/development-org.json -h localhost -u apiadmin -p changeme");
		System.out.println();
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Organization-Import";
	}


}
