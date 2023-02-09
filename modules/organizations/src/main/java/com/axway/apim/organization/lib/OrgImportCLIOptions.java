package com.axway.apim.organization.lib;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardImportCLIOptions;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.Option;

public class OrgImportCLIOptions extends CLIOptions {

	private OrgImportCLIOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) throws AppException {
		CLIOptions cliOptions = new OrgImportCLIOptions(args);
		cliOptions = new StandardImportCLIOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public void addOptions() {
		// Define command line options required for Application export
		Option option = new Option("c", "config", true, "This is the JSON-Formatted Organization-Config file containing the organization. You may get that config file using apim org get with output set to JSON.");
		option.setRequired(true);
		option.setArgName("org_config.json");
		addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);		
		Console.println("----------------------------------------------------------------------------------------");
		Console.println("How to imports organizations using the JSON-Config format");
		Console.println("Import an organization using enviornment properties: env.api-env.properties:");
		Console.println(getBinaryName()+" org import -c myOrgs/partner-org.json -s api-env");
		Console.println(getBinaryName()+" org import -c myOrgs/development-org.json -h localhost -u apiadmin -p changeme");
		Console.println();
		Console.println();
		Console.println("For more information and advanced examples please visit:");
		Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Organization-Import";
	}
	
	@Override
	public Parameters getParams() {
		OrgImportParams params = new OrgImportParams();
		params.setConfig(getValue("config"));
		return params;
	}
}
