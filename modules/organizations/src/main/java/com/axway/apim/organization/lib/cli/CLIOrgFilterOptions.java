package com.axway.apim.organization.lib.cli;

import org.apache.commons.cli.Option;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.organization.lib.OrgFilterParams;

public class CLIOrgFilterOptions extends CLIOptions {
	
	private CLIOptions cliOptions;

	public CLIOrgFilterOptions(CLIOptions cliOptions) {
		super();
		this.cliOptions = cliOptions;
	}

	@Override
	public Parameters getParams() throws AppException {
		OrgFilterParams params = (OrgFilterParams)cliOptions.getParams();
		params.setName(getValue("name"));
		params.setId(getValue("id"));
		params.setDev(getValue("dev"));
		
		return (Parameters) params;
	}

	@Override
	public void parse() {
		cliOptions.parse();
	}

	@Override
	public void addOption(Option option) {
		cliOptions.addOption(option);
	}

	@Override
	public void addInternalOption(Option option) {
		cliOptions.addInternalOption(option);
	}
	
	@Override
	public String getValue(String key) {
		return cliOptions.getValue(key);
	}

	@Override
	public boolean hasOption(String key) {
		return cliOptions.hasOption(key);
	}

	@Override
	public void printUsage(String message, String[] args) {
		cliOptions.printUsage(message, args);
	}

	@Override
	public void showReturnCodes() {
		cliOptions.showReturnCodes();
	}

	@Override
	public void addOptions() {
		cliOptions.addOptions();
		Option option = new  Option("n", "name", true, "Filter organizations with the specified name. You may use wildcards at the end or beginning.");
		option.setRequired(false);
		option.setArgName("*My organization*");
		cliOptions.addOption(option);
		
		option = new  Option("id", true, "Filter the export to an organization with that specific ID.");
		option.setRequired(false);
		option.setArgName("UUID-ID-OF-THE-ORG");
		cliOptions.addOption(option);
		
		option = new  Option("dev", true, "Filter organizations based on the development flag: true | false");
		option.setRequired(false);
		option.setArgName("true|false");
		cliOptions.addOption(option);
	}

	@Override
	public EnvironmentProperties getEnvProperties() {
		return this.cliOptions.getEnvProperties();
	}
}
