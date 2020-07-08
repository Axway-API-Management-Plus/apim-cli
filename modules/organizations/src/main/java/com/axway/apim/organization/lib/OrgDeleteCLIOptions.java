package com.axway.apim.organization.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

public class OrgDeleteCLIOptions extends OrgExportCLIOptions {

	CommandLine cmd;

	public OrgDeleteCLIOptions(String[] args) throws ParseException {
		super(args);
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


}
