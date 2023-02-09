package com.axway.apim.api.export.lib.cli;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.Option;

import com.axway.apim.api.export.lib.params.APIChangeParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;

public class CLIChangeAPIOptions extends CLIOptions {

	private CLIChangeAPIOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) throws AppException {
		CLIOptions cliOptions = new CLIChangeAPIOptions(args);
		cliOptions = new CLIAPIFilterOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
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
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		Console.println("----------------------------------------------------------------------------------------");
		Console.println("Changing APIs examples:");
		Console.println();
		Console.println("Changes the backend basepath of selected APIs from any to new");
		Console.println(getBinaryName()+" api change -s api-env <FILTER-APIs> -newBackend https://new.backend.host:6756/api");
		Console.println();
		Console.println("Changes the backend basepath of select APIs having the given oldBackend");
		Console.println(getBinaryName()+" api change -s api-env <FILTER-APIs> -newBackend https://new.backend.host:6756/api -oldBackend https://old.backend....");
		Console.println();
		Console.println("For more information and advanced examples please visit:");
		Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Change API";
	}
	
	@Override
	public Parameters getParams() {
		APIChangeParams params = new APIChangeParams();
		params.setNewBackend(getValue("newBackend"));
		params.setOldBackend(getValue("oldBackend"));
		return params;
	}
}
