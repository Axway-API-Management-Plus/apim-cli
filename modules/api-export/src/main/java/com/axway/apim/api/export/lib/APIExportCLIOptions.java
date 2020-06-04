package com.axway.apim.api.export.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.APIMCoreCLIOptions;

public class APIExportCLIOptions extends APIMCoreCLIOptions {

	CommandLine cmd;

	public APIExportCLIOptions(String[] args) throws ParseException {
		super(args);
		// Define command line options required for Application export
		Option option = new Option("a", "api-path", true, "Define the APIs to be exported, based on the exposure path.\n"
				+ "You can use wildcards to export multiple APIs:\n"
				+ "-a /api/v1/my/great/api     : Export a specific API\n"
				+ "-a *                        : Export all APIs\n"
				+ "-a /api/v1/any*             : Export all APIs with this prefix\n"
				+ "-a */some/other/api         : Export APIs end with the same path\n");
		option.setRequired(true);
		option.setArgName("/api/v1/my/great/api");
		options.addOption(option);
		
		option = new Option("s", "stage", true, "The API-Management stage (prod, preprod, qa, etc.)\n"
				+ "Is used to lookup the stage configuration file.");
		option.setArgName("preprod");
		options.addOption(option);

		option = new Option("v", "vhost", true, "Limit the export to that specific host.");
		option.setRequired(false);
		option.setArgName("vhost.customer.com");
		options.addOption(option);

		option = new Option("l", "localFolder", true, "Defines the location to store API-Definitions locally. Defaults to current folder.\n"
				+ "For each API a new folder is created automatically.");
		option.setRequired(false);
		option.setArgName("my/apis");
		options.addOption(option);

		option = new Option("df", "deleteFolder", true, "Controls if an existing local folder should be deleted. Defaults to false.");
		option.setArgName("true");
		options.addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);		
		System.out.println("You may run one of the following examples:");
		System.out.println(getBinaryName()+" api export -c samples/basic/minimal-config.json -a ../petstore.json -h localhost -u apiadmin -p changeme");
		System.out.println(getBinaryName()+" api export -c samples/basic/minimal-config.json -a ../petstore.json -h localhost -u apiadmin -p changeme -s prod");
		System.out.println(getBinaryName()+" api export -c samples/complex/complete-config.json -a ../petstore.json -h localhost -u apiadmin -p changeme");
		System.out.println();
		System.out.println();
		System.out.println("Using parameters provided in properties file stored in conf-folder:");
		System.out.println(getBinaryName()+" api export -c samples/basic/minimal-config-api-definition.json -s api-env");
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Application-Export";
	}


}
