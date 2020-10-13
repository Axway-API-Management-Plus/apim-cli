package com.axway.apim.setup.remotehosts.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.errorHandling.AppException;

public class RemoteHostsExportCLIOptions extends StandardExportCLIOptions {

	CommandLine cmd;

	public RemoteHostsExportCLIOptions(String[] args) throws ParseException {
		super(args);
		Option option = new  Option("n", "name", true, "Filter remote hosts based on the name. Wildcards are supported.");
		option.setRequired(false);
		option.setArgName("*backendhost.com");
		options.addOption(option);
		
		option = new  Option("id", true, "Filter Remote-Host with that specific ID.");
		option.setRequired(false);
		option.setArgName("UUID-ID-OF-THE-REMOTE-HOST");
		options.addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to get/export API-Manager remote hosts with different output formats");
		System.out.println("Get the configuration on console using environment properties: env.api-env.properties:");
		System.out.println(getBinaryName()+" remotehosts get -s api-env");
		System.out.println("Same as before, but with output format JSON - As it is used to import configuration");
		System.out.println(getBinaryName()+" remotehosts get -s api-env -o json");
		System.out.println();
		System.out.println();
		System.out.println("For more information please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Remote-Hosts Export";
	}
	
	public RemoteHostsExportParams getConfigExportParams() throws AppException {
		RemoteHostsExportParams params = new RemoteHostsExportParams();
		super.addStandardExportParameters(params);
		params.setName(getValue("name"));
		params.setId(getValue("id"));
		return params;
	}
}
