package com.axway.apim.appimport.lib;

import org.apache.commons.cli.Option;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.errorHandling.AppException;

public class AppImportCLIOptions extends CLIOptions {

	private AppImportCLIOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) {
		CLIOptions cliOptions = new AppImportCLIOptions(args);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public void addOptions() {
		// Define command line options required for Application export
		Option option = new Option("c", "config", true, "This is the JSON-Formatted Application-Config file containing the application. You may get that config file using apim app get with output set to JSON.");
		option.setRequired(true);
		option.setArgName("app_config.json");
		addOption(option);
		
		option = new Option("stageConfig", true, "Manually provide the name of the stage configuration file to use instead of derived from the given stage.");
		option.setArgName("my-staged-app-config.json");
		addOption(option);
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

	
	@Override
	public Parameters getParams() throws AppException {
		AppImportParams params = new AppImportParams();
		
		params.setConfig(getValue("c"));
		params.setStageConfig(getValue("stageConfig"));
		return params;
	}
}
