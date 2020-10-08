package com.axway.apim.setup.config.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.StandardImportParams;
import com.axway.apim.lib.errorHandling.AppException;

public class ConfigImportCLIOptions extends CoreCLIOptions {

	CommandLine cmd;

	public ConfigImportCLIOptions(String[] args) throws ParseException {
		super(args);
		Option option = new Option("c", "config", true, "This is the JSON-Formatted API-Manager configuration. You may get that config file using apim config get with output set to JSON.");
		option.setRequired(true);
		option.setArgName("api-manager.json");
		options.addOption(option);
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
	
	public StandardImportParams getImportParams() throws AppException {
		StandardImportParams params = new StandardImportParams();
		super.addCoreParameters(params);
		params.setConfig(getValue("config"));
		return params;
	}
}
