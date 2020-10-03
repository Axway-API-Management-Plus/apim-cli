package com.axway.apim.api.export.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.errorHandling.AppException;

public class APIExportCLIOptions extends StandardExportCLIOptions {

	CommandLine cmd;

	public APIExportCLIOptions(String[] args) throws ParseException {
		super(args);
		Option option = new Option("useFEAPIDefinition", "If this flag is set, the export API contains the API-Definition (e.g. Swagger) from the FE-API instead of the original imported API.");
		option.setRequired(false);
		options.addOption(option);
		
		option = new Option("a", "api-path", true, "Filter APIs to be exported, based on the exposure path.\n"
				+ "You can use wildcards to export multiple APIs:\n"
				+ "-a /api/v1/my/great/api     : Export a specific API\n"
				+ "-a *                        : Export all APIs\n"
				+ "-a /api/v1/any*             : Export all APIs with this prefix\n"
				+ "-a */some/other/api         : Export APIs end with the same path\n");
		option.setRequired(false);
		option.setArgName("/api/v1/my/great/api");
		options.addOption(option);
		
		option = new Option("n", "name", true, "Filter APIs with the given name. Wildcards at the beginning/end are supported.");
		option.setRequired(false);
		option.setArgName("*MyName*");
		options.addOption(option);
		
		option = new  Option("id", true, "Filter the API with that specific ID.");
		option.setRequired(false);
		option.setArgName("UUID-ID-OF-THE-API");
		options.addOption(option);
		
		option = new Option("policy", true, "Filter APIs with the given policy name. This is includes all policy types.");
		option.setRequired(false);
		option.setArgName("*Policy1*");
		options.addOption(option);

		option = new Option("vhost", true, "Filter APIs with that specific virtual host.");
		option.setRequired(false);
		option.setArgName("vhost.customer.com");
		options.addOption(option);
		
		option = new  Option("state", true, "Filter APIs with specific state: unpublished | pending | published");
		option.setRequired(false);
		option.setArgName("published");
		options.addOption(option);
		
		option = new  Option("backend", true, "Filter APIs with specific backendBasepath. Wildcards are supported.");
		option.setRequired(false);
		option.setArgName("*mybackhost.com*");
		options.addOption(option);
		
		option = new  Option("security", true, "Filter APIs with specific Inbound-Security. Wildcards are supported when filtering for policies.");
		option.setRequired(false);
		option.setArgName("oauth-ext|api-key|http-basic|...");
		options.addOption(option);
		
		option = new  Option("auth", true, "Filter APIs with specific Outbound-Authentication. Wildcards are supported when filtering for OAuth-Client-App");
		option.setRequired(false);
		option.setArgName("oauth|api-key|http-basic|...");
		options.addOption(option);
		
		option = new  Option("tag", true, "Filter APIs with a specific tag. Use either \"*myTagValueOrGroup*\" or \"tagGroup=*myTagValue*\"");
		option.setRequired(false);
		option.setArgName("tagGroup=*myTagValue*");
		options.addOption(option);
	}

	@Override
	protected String getAppName() {
		return "Application-Export";
	}

	public APIExportParams getAPIExportParams() throws AppException {
		APIExportParams params = new APIExportParams();
		addAPIExportParams(params);
		return params;
	}
	
	protected void addAPIExportParams(APIExportParams params) throws AppException {
		super.addStandardExportParameters(params);
		params.setApiPath(getValue("a"));
		params.setName(getValue("n"));
		params.setId(getValue("id"));
		params.setPolicy(getValue("policy"));
		params.setVhost(getValue("vhost"));
		params.setState(getValue("state"));
		params.setBackend(getValue("backend"));
		params.setTag(getValue("tag"));
		params.setUseFEAPIDefinition(hasOption("useFEAPIDefinition"));
		return;
	}
}
