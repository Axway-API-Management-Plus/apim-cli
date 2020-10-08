package com.axway.apim.setup.config.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.errorHandling.AppException;

public class ConfigExportCLIOptions extends StandardExportCLIOptions {

	CommandLine cmd;

	public ConfigExportCLIOptions(String[] args) throws ParseException {
		super(args);
		Option option = new  Option("n", "fieldName", true, "Limit export the the given fieldName. Wildcards are supported.");
		option.setRequired(false);
		option.setArgName("*password*");
		options.addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to get/export API-Manager configuration with different output formats");
		System.out.println("Get the configuration on console using environment properties: env.api-env.properties:");
		System.out.println(getBinaryName()+" config get -s api-env");
		System.out.println("Same as before, but with output format JSON - As it is used to import configuration");
		System.out.println(getBinaryName()+" config get -s api-env -o json");
		System.out.println();
		System.out.println();
		System.out.println("For more information please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Configuration-Export";
	}
	
	public ConfigExportParams getConfigExportParams() throws AppException {
		ConfigExportParams params = new ConfigExportParams();
		super.addStandardExportParameters(params);
		params.setFieldName(getValue("fieldName"));
		return params;
	}
}
