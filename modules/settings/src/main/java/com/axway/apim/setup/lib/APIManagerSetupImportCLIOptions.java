package com.axway.apim.setup.lib;

import org.apache.commons.cli.Option;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.StandardImportCLIOptions;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.errorHandling.AppException;

public class APIManagerSetupImportCLIOptions extends CLIOptions {

	private APIManagerSetupImportCLIOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) {
		CLIOptions cliOptions = new APIManagerSetupImportCLIOptions(args);
		cliOptions = new StandardImportCLIOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public void addOptions() {
		Option option = new Option("c", "config", true, "This is the JSON-Formatted API-Manager configuration. You may get that config file using apim config get with output set to JSON.");
		option.setRequired(true);
		option.setArgName("api-manager.json");
		addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);		
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to import API-Manager configuration");
		System.out.println("Import the API-Manager configuration:");
		System.out.println(getBinaryName()+" config import -c manager-config.json -s api-env");
		System.out.println();
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "API-Manager Config-Import";
	}
	
	@Override
	public StandardImportParams getParams() throws AppException {
		StandardImportParams params = new StandardImportParams();
		params.setConfig(getValue("config"));
		return params;
	}


}
