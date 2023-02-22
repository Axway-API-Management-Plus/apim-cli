package com.axway.apim.setup.lib;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.StandardImportCLIOptions;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.Option;

public class APIManagerSetupImportCLIOptions extends CLIOptions {

	private APIManagerSetupImportCLIOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) throws AppException {
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
		Console.println("----------------------------------------------------------------------------------------");
		Console.println("How to import API-Manager configuration");
		Console.println("Import the API-Manager configuration:");
		Console.println(getBinaryName()+" config import -c manager-config.json -s api-env");
		Console.println();
		Console.println();
		Console.println("For more information and advanced examples please visit:");
		Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "API-Manager Config-Import";
	}
	
	@Override
	public StandardImportParams getParams() {
		StandardImportParams params = new StandardImportParams();
		params.setConfig(getValue("config"));
		return params;
	}


}
