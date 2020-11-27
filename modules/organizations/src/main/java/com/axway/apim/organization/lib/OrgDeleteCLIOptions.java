package com.axway.apim.organization.lib;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.organization.lib.cli.CLIOrgFilterOptions;

public class OrgDeleteCLIOptions extends CLIOptions {

	private OrgDeleteCLIOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) {
		CLIOptions cliOptions = new OrgDeleteCLIOptions(args);
		cliOptions = new CLIOrgFilterOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to delete organizations using different filter options:");
		System.out.println(getBinaryName()+" org delete -s api-env");
		System.out.println(getBinaryName()+" org delete -s api-env -n \"*Org ABC*\"");
		System.out.println(getBinaryName()+" org delete -s api-env -id f6106454-1651-430e-8a2f-e3514afad8ee");
		System.out.println();
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Organization-Export";
	}

	@Override
	public Parameters getParams() throws AppException {
		return new OrgExportParams();
	}

	@Override
	public void addOptions() {
		// No additional options
		return;
	}
}
