package com.axway.apim.organization.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.errorHandling.AppException;

public class OrgExportCLIOptions extends StandardExportCLIOptions {

	CommandLine cmd;

	public OrgExportCLIOptions(String[] args) throws ParseException {
		super(args);
		Option option = new  Option("n", "name", true, "Filter organizations with the specified name. You may use wildcards at the end or beginning.");
		option.setRequired(false);
		option.setArgName("*My organization*");
		options.addOption(option);
		
		option = new  Option("id", true, "Filter the export to an organization with that specific ID.");
		option.setRequired(false);
		option.setArgName("UUID-ID-OF-THE-ORG");
		options.addOption(option);
		
		option = new  Option("dev", true, "Filter organizations based on the development flag: true | false");
		option.setRequired(false);
		option.setArgName("true|false");
		options.addOption(option);
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
	
	public OrgExportParams getOrgExportParams() throws AppException {
		OrgExportParams params = new OrgExportParams();
		super.addStandardExportParameters(params);
		params.setName(getValue("name"));
		params.setId(getValue("id"));
		params.setDev(getValue("dev"));
		return params;
	}
}
