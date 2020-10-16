package com.axway.apim.setup.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.errorHandling.AppException;

public class APIManagerSetupExportCLIOptions extends StandardExportCLIOptions {

	CommandLine cmd;

	public APIManagerSetupExportCLIOptions(String[] args) throws ParseException {
		super(args);
		Option option = new Option("type", true, "Limit the configuration with a comma separated list. (config|alerts|remotehosts|policies). If not given everything is exported. Policies are printed on console only.");
		option.setRequired(false);
		option.setArgName("config,alerts,remotehosts,policies");
		options.addOption(option);
		
		option = new  Option("n", "name", true, "Filter based on the name. Wildcards are supported. Actually only remote hosts are using this filter.");
		option.setRequired(false);
		option.setArgName("*backendhost.com");
		options.addOption(option);
		
		option = new  Option("id", true, "Filter on given ID. Actually only remote hosts are using this filter.");
		option.setRequired(false);
		option.setArgName("UUID-ID-OF-THE-REMOTE-HOST");
		options.addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to get/export API-Manager configuration with different output formats");
		System.out.println("Get the complete API-Manager on console using environment properties: env.api-env.properties:");
		System.out.println(getBinaryName()+" setup get -s api-env");
		System.out.println("Same as before, but with output format JSON - As it is used to import configuration");
		System.out.println(getBinaryName()+" setup get -s api-env -o json");
		System.out.println("Export configuration and alerts into JSON");
		System.out.println(getBinaryName()+" setup get -s api-env -o json -type alerts,config");
		System.out.println("Export remote hosts with specified name");
		System.out.println(getBinaryName()+" setup get -s api-env -type remotehosts -name \"*.host.com*\"");
		System.out.println();
		System.out.println();
		System.out.println("For more information please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Configuration-Export";
	}
	
	public APIManagerSetupExportParams getParams() throws AppException {
		APIManagerSetupExportParams params = new APIManagerSetupExportParams();
		super.addStandardExportParameters(params);
		params.setConfigType(getValue("type"));
		params.setRemoteHostName(getValue("name"));
		params.setRemoteHostId(getValue("id"));
		return params;
	}
}
