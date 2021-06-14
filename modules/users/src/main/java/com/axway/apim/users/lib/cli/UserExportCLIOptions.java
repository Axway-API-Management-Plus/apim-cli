package com.axway.apim.users.lib.cli;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.users.lib.params.UserExportParams;

public class UserExportCLIOptions extends CLIOptions {

	private UserExportCLIOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) {
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
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to get/export user(s) with different output formats");
		System.out.println("Get all users on console using environment properties: env.api-env.properties:");
		System.out.println(getBinaryName()+" user get -s api-env");
		System.out.println("Same as before, but with output format JSON - As it is used to import applications");
		System.out.println(getBinaryName()+" user get -s api-env -o json");
		System.out.println();
		System.out.println();
		System.out.println("How to filter the list of selected users:");
		System.out.println(getBinaryName()+" user get -s api-env -n \"Mark*\" -o json");
		System.out.println(getBinaryName()+" user get -s api-env -n \"Mike*\" -role admin -o json");
		System.out.println(getBinaryName()+" user get -s api-env -n \"*Marcel*\" -t /tmp/exported_apps -o json -deleteTarget ");
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "User-Management";
	}
	
	@Override
	public Parameters getParams() throws AppException {
		return new UserExportParams();
	}
}
