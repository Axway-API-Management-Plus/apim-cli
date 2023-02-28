package com.axway.apim.setup.remotehosts.lib;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.Option;

public class RemoteHostsExportCLIOptions extends CLIOptions {

	private RemoteHostsExportCLIOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) throws AppException {
		CLIOptions cliOptions = new RemoteHostsExportCLIOptions(args);
		cliOptions = new StandardExportCLIOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public void addOptions() {
		Option option = new  Option("n", "name", true, "Filter remote hosts based on the name. Wildcards are supported.");
		option.setRequired(false);
		option.setArgName("*backendhost.com");
		addOption(option);
		
		option = new  Option("id", true, "Filter Remote-Host with that specific ID.");
		option.setRequired(false);
		option.setArgName("UUID-ID-OF-THE-REMOTE-HOST");
		addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		Console.println("----------------------------------------------------------------------------------------");
		Console.println("How to get/export API-Manager remote hosts with different output formats");
		Console.println("Get the configuration on console using environment properties: env.api-env.properties:");
		Console.println(getBinaryName()+" remotehosts get -s api-env");
		Console.println("Same as before, but with output format JSON - As it is used to import configuration");
		Console.println(getBinaryName()+" remotehosts get -s api-env -o json");
		Console.println();
		Console.println();
		Console.println("For more information please visit:");
		Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Remote-Hosts Export";
	}
	
	public RemoteHostsExportParams getParams() {
		RemoteHostsExportParams params = new RemoteHostsExportParams();
		params.setName(getValue("name"));
		params.setId(getValue("id"));
		return params;
	}
}
