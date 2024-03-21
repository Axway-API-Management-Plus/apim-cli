package com.axway.apim.api.export.lib.cli;

import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.Option;

import com.axway.apim.api.export.lib.params.APIApproveParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;

public class CLIAPIApproveOptions extends CLIOptions {

	private CLIAPIApproveOptions(String[] args) {
		super(args);
	}

	public static CLIOptions create(String[] args) throws AppException {
		CLIOptions cliOptions = new CLIAPIApproveOptions(args);
		cliOptions = new CLIAPIFilterOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		Console.println("----------------------------------------------------------------------------------------");
		Console.println("Approve APIs using different filter options:");
		Console.println("** APIs must be in pending state to be considered for approval ** ");
		Console.println(getAppName() + " -s api-env");
		Console.println(getAppName() + " -s api-env -n \"*API*\"");
		Console.println(getAppName() + " -s api-env -id f6106454-1651-430e-8a2f-e3514afad8ee");
		Console.println(getAppName() + " -s api-env -policy \"*Policy ABC*\"");
		Console.println(getAppName() + " -s api-env -name \"*API*\" -policy \"*Policy ABC*\"");
		Console.println();
		Console.println();
		Console.println("For more information and advanced examples please visit:");
		Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return getBinaryName()+" api approve";
	}

	@Override
	public Parameters getParams() {
		APIApproveParams params = new APIApproveParams();
		params.setPublishVhost(getValue("publishVHost"));
		return params;
	}

	@Override
	public void addOptions() {
		Option option = new Option("publishVHost", true, "Use this V-Host to publish pending API.");
		option.setRequired(false);
		option.setArgName("qa-api.customer.com:8443");
		addOption(option);
	}
}
