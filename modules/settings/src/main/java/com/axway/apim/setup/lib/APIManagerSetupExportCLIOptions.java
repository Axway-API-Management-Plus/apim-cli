package com.axway.apim.setup.lib;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.Option;

public class APIManagerSetupExportCLIOptions extends CLIOptions {

	private APIManagerSetupExportCLIOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) throws AppException {
		CLIOptions cliOptions = new APIManagerSetupExportCLIOptions(args);
		cliOptions = new StandardExportCLIOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public void addOptions() {
		Option option = new Option("type", true, "Limit the configuration with a comma separated list. (config|alerts|remotehosts|policies|customProperties). If not given everything is exported. Policies and Custom-Properties configuration are printed on console only.");
		option.setRequired(false);
		option.setArgName("config,alerts,remotehosts,policies");
		addOption(option);
		
		option = new  Option("n", "name", true, "Filter based on the name. Wildcards are supported. Actually only remote hosts are using this filter.");
		option.setRequired(false);
		option.setArgName("*backendhost.com");
		addOption(option);
		
		option = new  Option("id", true, "Filter on given ID. Actually only remote hosts are using this filter.");
		option.setRequired(false);
		option.setArgName("UUID-ID-OF-THE-REMOTE-HOST");
		addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		Console.println("----------------------------------------------------------------------------------------");
		Console.println("How to get/export API-Manager configuration with different output formats");
		Console.println("Get the complete API-Manager on console using environment properties: env.api-env.properties:");
		Console.println(getBinaryName()+" setup get -s api-env");
		Console.println("Same as before, but with output format JSON - As it is used to import configuration");
		Console.println(getBinaryName()+" setup get -s api-env -o json");
		Console.println("Export configuration and alerts into JSON");
		Console.println(getBinaryName()+" setup get -s api-env -o json -type alerts,config");
		Console.println("Export remote hosts with specified name");
		Console.println(getBinaryName()+" setup get -s api-env -type remotehosts -name \"*.host.com*\"");
		Console.println();
		Console.println();
		Console.println("For more information please visit:");
		Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Configuration-Export";
	}
	
	@Override
	public APIManagerSetupExportParams getParams() {
		APIManagerSetupExportParams params = new APIManagerSetupExportParams();
		params.setConfigType(getValue("type"));
		params.setRemoteHostName(getValue("name"));
		params.setRemoteHostId(getValue("id"));
		return params;
	}
}
