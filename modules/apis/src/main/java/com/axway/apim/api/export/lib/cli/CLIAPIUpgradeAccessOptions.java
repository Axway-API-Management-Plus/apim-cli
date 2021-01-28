package com.axway.apim.api.export.lib.cli;

import org.apache.commons.cli.Option;

import com.axway.apim.api.export.lib.params.APIUpgradeAccessParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.errorHandling.AppException;

public class CLIAPIUpgradeAccessOptions extends CLIOptions {
	
	private CLIAPIUpgradeAccessOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) {
		CLIOptions cliOptions = new CLIAPIUpgradeAccessOptions(args);
		cliOptions = new CLIAPIFilterOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}
	
	@Override
	public void addOptions() {
		Option option = new Option("refAPIId", true, "Filter the reference API based on the ID.");
		option.setRequired(false);
		option.setArgName("UUID-ID-OF-THE-REF-API");
		addOption(option);
		
		option = new Option("refAPIName", true, "Filter the reference API based on the name. Wildcards are supported.");
		option.setRequired(false);
		option.setArgName("*My-Old-API*");
		addOption(option);
		
		option = new Option("refAPIVersion", true, "Filter the reference API based on the version.");
		option.setRequired(false);
		option.setArgName("1.0.0");
		addOption(option);
		
		option = new Option("refAPIOrg", true, "Filter the reference API based on the organization. Wildcards are supported.");
		option.setRequired(false);
		option.setArgName("*Org A*");
		addOption(option);
		
		option = new Option("refAPIDeprecate", true, "If set the old/reference API will be flagged as deprecated. Defaults to false.");
		option.setRequired(false);
		option.setArgName("true");
		addOption(option);
		
		option = new Option("refAPIRetire", true, "If set the old/reference API will be retired. Default to false.");
		option.setRequired(false);
		option.setArgName("true");
		addOption(option);
		
		option = new Option("refAPIRetireDate", true, "Sets the retirement date of the old API. Supported formats: \"dd.MM.yyyy\", \"dd/MM/yyyy\", \"yyyy-MM-dd\", \"dd-MM-yyyy\"");
		option.setRequired(false);
		option.setArgName("2021/06/30");
		addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);		
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("Upgrade one or more APIs based on the given reference API.");
		System.out.println("App-Subscriptions and Granted orgs are taken over to all selected APIs based on the reference API.");
		System.out.println("The reference API must be unique. APIs must be published to be considered.");
		System.out.println(getBinaryName()+" api upgrade -s api-env -refAPIId <UUID-ID-OF-THE-REF-API> -id <UUID-ID-OF-THE-API>");
		System.out.println(getBinaryName()+" api upgrade -s api-env -n \"*APIs-to-be-upgraded*\" -refAPIName \"*Name of Ref-API*\"");
		System.out.println(getBinaryName()+" api upgrade -s api-env -n \"*APIs-to-be-upgraded*\" -refAPIName \"*Name of Ref-API*\" -refAPIDeprecate true");
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
		APIUpgradeAccessParams params = new APIUpgradeAccessParams();
		params.setReferenceAPIId(getValue("refAPIId"));
		params.setReferenceAPIName(getValue("refAPIName"));
		params.setReferenceAPIVersion(getValue("refAPIVersion"));
		params.setReferenceAPIOrganization(getValue("refAPIOrg"));
		params.setReferenceAPIDeprecate(Boolean.parseBoolean(getValue("refAPIDeprecate")));
		params.setReferenceAPIRetire(Boolean.parseBoolean(getValue("refAPIRetire")));
		params.setReferenceAPIRetirementDate(getValue("refAPIRetireDate"));
		return params;
	}
}
