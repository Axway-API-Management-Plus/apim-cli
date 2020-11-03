package com.axway.apim.api.export.lib.cli;

import org.apache.commons.cli.Option;

import com.axway.apim.api.export.lib.params.APIFilterParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.errorHandling.AppException;

public class CLIAPIFilterOptions extends CLIOptions {
	
	private CLIOptions cliOptions;

	public CLIAPIFilterOptions(CLIOptions cliOptions) {
		super();
		this.cliOptions = cliOptions;
	}

	@Override
	public Parameters getParams() throws AppException {
		APIFilterParams params = (APIFilterParams)cliOptions.getParams();
		params.setApiPath(getValue("a"));
		params.setName(getValue("n"));
		params.setOrganization(getValue("org"));
		params.setId(getValue("id"));
		params.setPolicy(getValue("policy"));
		params.setVhost(getValue("vhost"));
		params.setState(getValue("state"));
		params.setBackend(getValue("backend"));
		params.setTag(getValue("tag"));
		params.setInboundSecurity(getValue("inboundsecurity"));
		params.setOutboundAuthentication(getValue("outboundauthn"));
		
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
		Option option = new Option("a", "api-path", true, "Filter APIs to be exported, based on the exposure path.\n"
				+ "You can use wildcards to export multiple APIs:\n"
				+ "-a /api/v1/my/great/api     : Export a specific API\n"
				+ "-a *                        : Export all APIs\n"
				+ "-a /api/v1/any*             : Export all APIs with this prefix\n"
				+ "-a */some/other/api         : Export APIs end with the same path\n");
		option.setRequired(false);
		option.setArgName("/api/v1/my/great/api");
		cliOptions.addOption(option);
		
		option = new Option("n", "name", true, "Filter APIs with the given name. Wildcards at the beginning/end are supported.");
		option.setRequired(false);
		option.setArgName("*MyName*");
		cliOptions.addOption(option);
		
		option = new Option("org", true, "Filter APIs with the given organization. Wildcards at the beginning/end are supported.");
		option.setRequired(false);
		option.setArgName("*MyOrg*");
		cliOptions.addOption(option);
		
		option = new  Option("id", true, "Filter the API with that specific ID.");
		option.setRequired(false);
		option.setArgName("UUID-ID-OF-THE-API");
		cliOptions.addOption(option);
		
		option = new Option("policy", true, "Filter APIs with the given policy name. This is includes all policy types.");
		option.setRequired(false);
		option.setArgName("*Policy1*");
		cliOptions.addOption(option);

		option = new Option("vhost", true, "Filter APIs with that specific virtual host.");
		option.setRequired(false);
		option.setArgName("vhost.customer.com");
		cliOptions.addOption(option);
		
		option = new  Option("state", true, "Filter APIs with specific state: unpublished | pending | published");
		option.setRequired(false);
		option.setArgName("published");
		cliOptions.addOption(option);
		
		option = new  Option("backend", true, "Filter APIs with specific backendBasepath. Wildcards are supported.");
		option.setRequired(false);
		option.setArgName("*mybackhost.com*");
		cliOptions.addOption(option);
		
		option = new  Option("inboundsecurity", true, "Filter APIs with specific Inbound-Security. Wildcards are supported when filtering for APIs using a custom security policy.");
		option.setRequired(false);
		option.setArgName("oauth-ext|api-key|*my-security-pol*|...");
		cliOptions.addOption(option);
		
		option = new  Option("outboundauthn", true, "Filter APIs with specific Outbound-Authentication. Wildcards are supported when filtering for an OAuth Provider profile.");
		option.setRequired(false);
		option.setArgName("oauth|api-key|My provider profile*|...");
		cliOptions.addOption(option);
		
		option = new  Option("tag", true, "Filter APIs with a specific tag. Use either \"*myTagValueOrGroup*\" or \"tagGroup=*myTagValue*\"");
		option.setRequired(false);
		option.setArgName("tagGroup=*myTagValue*");
		cliOptions.addOption(option);
	}
}
