package com.axway.apim.api.export.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

public class ChangeAPICLIOptions extends APIExportCLIOptions {

	CommandLine cmd;

	public ChangeAPICLIOptions(String[] args) throws ParseException {
		super(args);
		Option option = new Option("newBackend", true, "The new backend you would like to change to.");
		option.setRequired(false);
		option.setArgName("https://new.server.com:8080/api");
		options.addOption(option);
		
		option = new Option("oldBackend", true, "If given, only APIs matching to this backend will be changed");
		option.setRequired(false);
		option.setArgName("https://old.server.com:8080/api");
		options.addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("Changing APIs examples:");
		System.out.println();
		System.out.println("Changes the backend basepath of selected APIs from any to new");
		System.out.println(getBinaryName()+" api change -s api-env <FILTER-APIs> -newBackend https://new.backend.host:6756/api");
		System.out.println();
		System.out.println("Changes the backend basepath of select APIs having the given oldBackend");
		System.out.println(getBinaryName()+" api change -s api-env <FILTER-APIs> -newBackend https://new.backend.host:6756/api -oldBackend https://old.backend....");
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Application-Export";
	}


}
