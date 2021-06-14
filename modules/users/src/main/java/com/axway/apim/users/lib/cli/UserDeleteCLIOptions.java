package com.axway.apim.users.lib.cli;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.users.lib.params.UserExportParams;

public class UserDeleteCLIOptions extends CLIOptions {

	private UserDeleteCLIOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) {
		CLIOptions cliOptions = new UserDeleteCLIOptions(args);
		cliOptions = new CLIUserFilterOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to delete users using different filter options:");
		System.out.println(getBinaryName()+" user delete -s api-env");
		System.out.println(getBinaryName()+" user delete -s api-env -n \"*Name of user*\" -loginName \"*loginNameOfUser*\"");
		System.out.println(getBinaryName()+" user delete -s api-env -id f6106454-1651-430e-8a2f-e3514afad8ee");
		System.out.println();
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

	@Override
	public void addOptions() {
		// No additional options to add
		return;
	}
}
