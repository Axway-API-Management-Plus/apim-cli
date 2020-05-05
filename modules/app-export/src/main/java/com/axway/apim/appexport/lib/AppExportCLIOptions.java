package com.axway.apim.appexport.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.APIMCoreCLIOptions;

public class AppExportCLIOptions extends APIMCoreCLIOptions {

	CommandLine cmd;

	public AppExportCLIOptions(String[] args) throws ParseException {
		super(args);
		// Define command line options required for Application export
		Option option = new  Option("n", "name", true, "Application name");
		option.setRequired(false);
		options.addOption(option);

		option = new  Option("state", false, "Export application with specific state: pending | approved");
		option.setRequired(false);
		options.addOption(option);

		option = new  Option("orgName", false, "Limit applications to this organization");
		option.setRequired(false);
		options.addOption(option);

		option = new Option("t", "targetFolder", true, "Defines the location to store the application locally. Defaults to current folder.\n"
				+ "For each application a new folder is created automatically.");
		option.setRequired(false);
		option.setArgName("my/apps");
		options.addOption(option);
		
		option = new Option("df", "deleteFolder", true, "Controls if an existing local folder should be deleted. Defaults to false.");
		option.setArgName("true");
		options.addOption(option);
	}

	public void printUsage(Options options, String message, String[] args) {
		super.printUsage(options, message, args);		
		System.out.println("You may run one of the following examples:");
		System.out.println(getBinaryName()+" -c samples/basic/minimal-config.json -a ../petstore.json -h localhost -u apiadmin -p changeme");
		System.out.println(getBinaryName()+" -c samples/basic/minimal-config.json -a ../petstore.json -h localhost -u apiadmin -p changeme -s prod");
		System.out.println(getBinaryName()+" -c samples/complex/complete-config.json -a ../petstore.json -h localhost -u apiadmin -p changeme");
		System.out.println();
		System.out.println();
		System.out.println("Using parameters provided in properties file stored in conf-folder:");
		System.out.println(getBinaryName()+" -c samples/basic/minimal-config-api-definition.json -s api-env");
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/tree/develop/modules/swagger-promote-core/src/main/assembly/samples");
		System.out.println("https://github.com/Axway-API-Management-Plus/apimanager-swagger-promote/wiki");
	}

	@Override
	protected String getAppName() {
		return "Application-Export";
	}


}
