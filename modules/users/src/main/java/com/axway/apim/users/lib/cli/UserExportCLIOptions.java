package com.axway.apim.users.lib.cli;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import com.axway.apim.users.lib.params.UserExportParams;

public class UserExportCLIOptions extends CLIOptions {

	private UserExportCLIOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) throws AppException {
		CLIOptions cliOptions = new UserExportCLIOptions(args);
		cliOptions = new CLIUserFilterOptions(cliOptions);
		cliOptions = new StandardExportCLIOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public void addOptions() {

	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		Console.println("----------------------------------------------------------------------------------------");
		Console.println("How to get/export user(s) with different output formats");
		Console.println("Get all users on console using environment properties: env.api-env.properties:");
		Console.println(getBinaryName()+" user get -s api-env");
		Console.println("Same as before, but with output format JSON - As it is used to import applications");
		Console.println(getBinaryName()+" user get -s api-env -o json");
		Console.println();
		Console.println();
		Console.println("How to filter the list of selected users:");
		Console.println(getBinaryName()+" user get -s api-env -n \"Mark*\" -o json");
		Console.println(getBinaryName()+" user get -s api-env -n \"Mike*\" -role admin -o json");
		Console.println(getBinaryName()+" user get -s api-env -n \"*Marcel*\" -t /tmp/exported_apps -o json -deleteTarget ");
		Console.println();
		Console.println("For more information and advanced examples please visit:");
		Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "User-Management";
	}
	
	@Override
	public Parameters getParams() {
		return new UserExportParams();
	}
}
