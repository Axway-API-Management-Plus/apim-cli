package com.axway.apim.appimport.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.APIMCoreCLIOptions;

public class AppImportCLIOptions extends APIMCoreCLIOptions {

	CommandLine cmd;

	public AppImportCLIOptions(String[] args) throws ParseException {
		super(args);
		// Define command line options required for Application export
		Option option = new Option("c", "config", true, "This is the JSON-Formatted Application-Config file containing the application");
		option.setRequired(true);
		option.setArgName("app_config.json");
		options.addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);		
		System.out.println("You may run one of the following examples:");
		System.out.println("Using parameters provided in properties file stored in conf-folder:");
		System.out.println(getBinaryName()+" app export -n \"Client App\" -s api-env");
		System.out.println(getBinaryName()+" app export -n \"Client App\" -l /tmp/exported_apps -s api-env -deleteFolder");
		System.out.println(getBinaryName()+" app export -n \"Client App\" -l /tmp/exported_apps -s api-env -deleteFolder -f json");
		System.out.println();
		System.out.println();
		System.out.println(getBinaryName()+" app export -n \"Client App\" -h localhost -u apiadmin -p changeme");
		System.out.println(getBinaryName()+" app export -n \"App 123\" -h localhost -u apiadmin -p changeme -s prod -deleteFolder");
		System.out.println(getBinaryName()+" app export -n \"App 123\" -l /tmp/exported_apps -deleteFolder -h localhost -u apiadmin -p changeme -s prod");

		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Application-Export";
	}


}
