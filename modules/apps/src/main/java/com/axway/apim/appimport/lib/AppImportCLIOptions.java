package com.axway.apim.appimport.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.errorHandling.AppException;

public class AppImportCLIOptions extends CoreCLIOptions {

	CommandLine cmd;

	public AppImportCLIOptions(String[] args) throws ParseException {
		super(args);
		// Define command line options required for Application export
		Option option = new Option("c", "config", true, "This is the JSON-Formatted Application-Config file containing the application. You may get that config file using apim app get with output set to JSON.");
		option.setRequired(true);
		option.setArgName("app_config.json");
		options.addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);		
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to imports applications using JSON the JSON-Config format");
		System.out.println("Import an application using enviornment properties: env.api-env.properties:");
		System.out.println(getBinaryName()+" app import -c myApps/great-app.json -s api-env");
		System.out.println(getBinaryName()+" app import -c myApps/another-great-app.json -h localhost -u apiadmin -p changeme");
		System.out.println();
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Application-Export";
	}

	
	public AppImportParams getAppImportParams() throws AppException {
		AppImportParams params = new AppImportParams();
		super.addCoreParameters(params);
		params.setConfig(getValue("c"));
		return params;
	}
}
