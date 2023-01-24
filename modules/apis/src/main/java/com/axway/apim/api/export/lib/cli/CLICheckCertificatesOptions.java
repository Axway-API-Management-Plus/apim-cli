package com.axway.apim.api.export.lib.cli;

import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.Option;

import com.axway.apim.api.export.lib.params.APICheckCertificatesParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;

public class CLICheckCertificatesOptions extends CLIOptions {

	private CLICheckCertificatesOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) {
		CLIOptions cliOptions = new CLICheckCertificatesOptions(args);
		cliOptions = new CLIAPIFilterOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public void addOptions() {
		Option option = new Option("days", true, "The number of days for which you want to check if certificates expire.");
		option.setRequired(true);
		option.setArgName("30");
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
		return "API Check certificates";
	}
	
	@Override
	public Parameters getParams() {
		APICheckCertificatesParams params = new APICheckCertificatesParams();
		params.setNumberOfDays(Integer.parseInt(getValue("days")));
		return params;
	}
}
