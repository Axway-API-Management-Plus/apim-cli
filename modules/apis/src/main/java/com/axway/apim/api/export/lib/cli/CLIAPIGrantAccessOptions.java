package com.axway.apim.api.export.lib.cli;

import org.apache.commons.cli.Option;

import com.axway.apim.api.export.lib.params.APIGrantAccessParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.errorHandling.AppException;

public class CLIAPIGrantAccessOptions extends CLIOptions {
	
	private CLIAPIGrantAccessOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) {
		CLIOptions cliOptions = new CLIAPIGrantAccessOptions(args);
		cliOptions = new CLIAPIFilterOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}
	
	@Override
	public void addOptions() {
		
		Option option = new  Option("orgName", true, "Filter the desired organizations based on the name to give them the rights to the selected APIs. You may use wildcards at the end or beginning.");
		option.setRequired(false);
		option.setArgName("*My organization*");
		addOption(option);
		
		option = new  Option("orgId", true, "The ID of the organization to which you want to give the rights for the selected APIs.");
		option.setRequired(false);
		option.setArgName("UUID-ID-OF-THE-ORG");
		addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);		
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("Grant access for selected organizations to one or more APIs.");
		System.out.println("You can use all known API filters to select the desired APIs. However, only APIs that are in the Published status are considered.");
		System.out.println(getBinaryName()+" api grant-access -s api-env -orgId <UUID-ID-OF-THE-ORG> -id <UUID-ID-OF-THE-API>");
		System.out.println(getBinaryName()+" api grant-access -s api-env -orgName *MyOrg* -n *NameOfAPI*");
		System.out.println();
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return getBinaryName()+" api approve";
	}

	@Override
	public Parameters getParams() throws AppException {
		APIGrantAccessParams params = new APIGrantAccessParams();
		params.setOrgId(getValue("orgId"));
		params.setOrgName(getValue("orgName"));
		return params;
	}
}
