package com.axway.apim.api.export.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

public class APIExportGetCLIOptions extends APIExportCLIOptions {

	CommandLine cmd;

	public APIExportGetCLIOptions(String[] args) throws ParseException {
		super(args);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to get APIs with different output formats");
		System.out.println("Get all APIs on console using environment properties: env.api-env.properties:");
		System.out.println(getBinaryName()+" api get -s api-env");
		System.out.println("Same as before, but with output format JSON:");
		System.out.println(getBinaryName()+" api get -s api-env -o json");
		System.out.println("Result as CSV-File with all possible information:");
		System.out.println(getBinaryName()+" api get -s api-env -o json -ultra");
		System.out.println();
		System.out.println();
		System.out.println("How to filter the list of selected APIs:");
		System.out.println(getBinaryName()+" api get -s api-env -n \"*API*\" -o csv");
		System.out.println(getBinaryName()+" api get -s api-env -id f6106454-1651-430e-8a2f-e3514afad8ee");
		System.out.println(getBinaryName()+" api get -s api-env -policy \"*Policy ABC*\" -o json");
		System.out.println(getBinaryName()+" api get -s api-env -name \"*API*\" -policy \"*Policy ABC*\"");
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Application-Export";
	}
}
