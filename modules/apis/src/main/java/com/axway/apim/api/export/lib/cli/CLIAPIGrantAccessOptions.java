package com.axway.apim.api.export.lib.cli;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.Option;

public class CLIAPIGrantAccessOptions extends CLIOptions {

	public CLIAPIGrantAccessOptions(String[] args) {
		super(args);
	}

	public static CLIOptions create(String[] args) throws AppException {
		CLIOptions cliOptions = new CLIAPIGrantAccessOptions(args);
		return Helper.create(cliOptions);
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

        option = new  Option("appName", true, "Desired application name to be granted access to API");
        option.setRequired(false);
        option.setArgName("Application Name");
        addOption(option);

        option = new  Option("appId", true, "The ID of the application to which you give the rights for the selected APIs.");
        option.setRequired(false);
        option.setArgName("UUID-ID-OF-THE-APP");
        addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		Console.println("----------------------------------------------------------------------------------------");
		Console.println("Grant access for selected organizations to one or more APIs.");
		Console.println("You can use all known API filters to select the desired APIs. However, only APIs that are in the Published status are considered.");
        Console.println("----------------------------Grant API Access to Organization----------------------------");
		Console.println(getBinaryName()+" api grant-access -s api-env -orgId <UUID-ID-OF-THE-ORG> -id <UUID-ID-OF-THE-API>");
		Console.println(getBinaryName()+" api grant-access -s api-env -orgName *MyOrg* -n *NameOfAPI*");
        Console.println("----------------------------Grant API Access to Application-----------------------------");
        Console.println(getBinaryName()+" api grant-access -s api-env -orgId <UUID-ID-OF-THE-ORG> -id <UUID-ID-OF-THE-API> -appId <UUID-ID-OF-THE-APP");
        Console.println(getBinaryName()+" api grant-access -s api-env -orgName *MyOrg* -n *NameOfAPI* -appName applicationName");
		Console.println();
		Console.println();
		Console.println("For more information and advanced examples please visit:");
		Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Grant access";
	}

	@Override
	public Parameters getParams() throws AppException {
		return Helper.getParams(this);
	}
}
