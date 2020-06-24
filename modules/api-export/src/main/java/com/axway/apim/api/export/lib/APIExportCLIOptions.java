package com.axway.apim.api.export.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.StandardExportCLIOptions;

public abstract class APIExportCLIOptions extends StandardExportCLIOptions {

	CommandLine cmd;

	public APIExportCLIOptions(String[] args) throws ParseException {
		super(args);
		// Define command line options required for Application export
		Option option = new Option("a", "api-path", true, "Define the APIs to be exported, based on the exposure path.\n"
				+ "You can use wildcards to export multiple APIs:\n"
				+ "-a /api/v1/my/great/api     : Export a specific API\n"
				+ "-a *                        : Export all APIs\n"
				+ "-a /api/v1/any*             : Export all APIs with this prefix\n"
				+ "-a */some/other/api         : Export APIs end with the same path\n");
		option.setRequired(false);
		option.setArgName("/api/v1/my/great/api");
		options.addOption(option);
		
		option = new Option("useFEAPIDefinition", "If this flag is set, the export API contains the API-Definition (e.g. Swagger) from the FE-API instead of the original imported API.");
		option.setRequired(false);
		options.addOption(option);
		
		option = new Option("n", "name", true, "The name of the API. Wildcards at the beginning/end are supported. Use '*' to export all APIs.");
		option.setRequired(false);
		option.setArgName("*MyName*");
		options.addOption(option);
		
		option = new  Option("id", true, "The ID of the API.");
		option.setRequired(false);
		options.addOption(option);
		
		option = new Option("policy", true, "Get APIs with the given policy name. This is includes all policy types.");
		option.setRequired(false);
		option.setArgName("*Policy1*");
		options.addOption(option);

		option = new Option("vhost", true, "Limit the export to that specific host.");
		option.setRequired(false);
		option.setArgName("vhost.customer.com");
		options.addOption(option);
		
		option = new  Option("state", true, "Select APIs with specific state: unpublished | pending | published");
		option.setRequired(false);
		options.addOption(option);
		
		option = new  Option("backend", true, "Filter APIs with specific backendBasepath. Wildcards are supported.");
		option.setRequired(false);
		options.addOption(option);

		option = new Option("t", "target", true, "Defines the target location for get operation creating files or directories. Defaults to current folder.\n"
				+ "Required output files or directories are created automatically.");
		option.setRequired(false);
		option.setArgName("my/apis");
		options.addOption(option);
	}

	@Override
	protected String getAppName() {
		return "Application-Export";
	}


}
