package com.axway.apim.organization.lib;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.organization.lib.cli.CLIOrgFilterOptions;

public class OrgExportCLIOptions extends CLIOptions {

	private OrgExportCLIOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) {
		CLIOptions cliOptions = new OrgExportCLIOptions(args);
		cliOptions = new CLIOrgFilterOptions(cliOptions);
		cliOptions = new StandardExportCLIOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to get/export organizations with different output formats");
		System.out.println("Get all organizations on console using environment properties: env.api-env.properties:");
		System.out.println(getBinaryName()+" org get -s api-env");
		System.out.println("Same as before, but with output format JSON - As it is used to import organizations");
		System.out.println(getBinaryName()+" org get -s api-env -o json");
		System.out.println();
		System.out.println();
		System.out.println("How to filter the list of selected organizations:");
		System.out.println(getBinaryName()+" org get -s api-env -n \"Partner *\" -o json");
		System.out.println(getBinaryName()+" org get -s api-env -n \"*Private*\" -t /tmp/exported_apps -o json -deleteTarget ");
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Application-Export";
	}
	
	@Override
	public Parameters getParams() throws AppException {
		OrgExportParams params = new OrgExportParams();

		params.setName(getValue("name"));
		params.setId(getValue("id"));
		params.setDev(getValue("dev"));
		return params;
	}
	
	@Override
	public void addOptions() {
		// No additional options
		return;
	}
}
