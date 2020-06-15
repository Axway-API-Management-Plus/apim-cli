package com.axway.apim.api.export.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

public class APIListCLIOptions extends APIExportCLIOptions {

	CommandLine cmd;

	public APIListCLIOptions(String[] args) throws ParseException {
		super(args);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);		
		System.out.println("You may run one of the following examples:");
		System.out.println(getBinaryName()+" api get -s api-env");
		System.out.println(getBinaryName()+" api get -s api-env -n \"*API*\"");
		System.out.println(getBinaryName()+" api get -s api-env -policy \"*Policy ABC*\"");
		System.out.println();
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Application-Export";
	}


}
