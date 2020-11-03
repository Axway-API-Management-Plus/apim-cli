package com.axway.apim.api.export.lib.cli;

import org.apache.commons.cli.Option;

import com.axway.apim.api.export.lib.params.APIChangeParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;

public class CLINewChangeOptions extends CLIOptions {

	private CLINewChangeOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) {
		CLIOptions cliOptions = new CLINewChangeOptions(args);
		cliOptions = new CLIAPIFilterOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public Parameters getParams() {
		APIChangeParams params = new APIChangeParams();
		params.setNewBackend(getValue("newBackend"));
		params.setOldBackend(getValue("oldBackend"));
		return params;
	}

	@Override
	public void addOptions() {
		Option option = new Option("newBackend", true, "The new backend you would like to change to.");
		option.setRequired(false);
		option.setArgName("https://new.server.com:8080/api");
		addOption(option);
		
		option = new Option("oldBackend", true, "If given, only APIs matching to this backend will be changed");
		option.setRequired(false);
		option.setArgName("https://old.server.com:8080/api");
		addOption(option);
	}

	@Override
	protected String getAppName() {
		return "api change";
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

}
