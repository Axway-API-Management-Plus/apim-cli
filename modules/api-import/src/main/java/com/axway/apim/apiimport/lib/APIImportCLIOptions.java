package com.axway.apim.apiimport.lib;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import com.axway.apim.lib.APIMCoreCLIOptions;

public class APIImportCLIOptions extends APIMCoreCLIOptions {

	CommandLine cmd;

	public APIImportCLIOptions(String[] args) throws ParseException {
		super(args);
		// Define command line options required for Application export
		Option option = new Option("a", "apidefinition", true, "(Optional) The API Definition either as Swagger (JSON/YAML) or a WSDL for SOAP-Services:\n"
				+ "- in local filesystem using a relative or absolute path. Example: swagger_file.json\n"
				+ "  Please note: Local filesystem is not supported for WSDLs. Please use direct URL or a URL-Reference-File.\n"
				+ "- a URL providing the Swagger-File or WSDL-File. Examples:\n"
				+ "  [username/password@]https://any.host.com/my/path/to/swagger.json\n"
				+ "  [username/password@]http://www.dneonline.com/calculator.asmx?wsdl\n"
				+ "- a reference file called anyname-i-want.url which contains a line with the URL\n"
				+ "  (same format as above for Swagger or WSDL)."
				+ "  If not specified, the API Definition configuration is read directly from the API-Config file.");
		option.setRequired(false);
		option.setArgName("swagger_file.json");
		options.addOption(option);

		option = new Option("c", "config", true, "This is the JSON-Formatted API-Config containing information how to expose the API. You may get that config file using apim api get with output set to JSON.");
		option.setRequired(true);
		option.setArgName("api_config.json");
		options.addOption(option);
		
		option = new Option("ignoreQuotas", "Use this flag to ignore configured API quotas.");
		option.setRequired(false);
		options.addOption(option);
		
		option = new Option("useFEAPIDefinition", "If this flag is set, the Actual-API contains the API-Definition (e.g. Swagger) from the FE-API instead of the original imported API.");
		option.setRequired(false);
		options.addOption(option);
		
		option = new Option("clientOrgsMode", true, "Controls how configured Client-Organizations are treated. Defaults to add!");
		option.setArgName("ignore|replace|add");
		options.addOption(option);
		
		option = new Option("clientAppsMode", true, "Controls how configured Client-Applications are treated. Defaults to add!");
		option.setArgName("ignore|replace|add");
		options.addOption(option);
		
		option = new Option("quotaMode", true, "Controls how quotas are managed in API-Manager. Defaults to add!");
		option.setArgName("ignore|replace|add");
		options.addOption(option);
		
		option = new Option("allowOrgAdminsToPublish", true, "If set to false, OrgAdmins cannot replicate an API with desired state published. Defaults to true.");
		option.setRequired(false);
		option.setArgName("false");
		internalOptions.addOption(option);
		
		option = new Option("replaceHostInSwagger", true, "Controls if you want to replace the host in your Swagger-File ");
		option.setRequired(false);
		option.setArgName("true");
		internalOptions.addOption(option);
		
		option = new Option("changeOrganization", "Set this flag to allow to change the organization of an existing API.");
		option.setRequired(false);
		internalOptions.addOption(option);
		
		option = new Option("detailsExportFile", true, "Configure a filename, to get a Key=Value file containing information about the created API.");
		option.setRequired(false);
		option.setArgName("APIDetails.properties");
		options.addOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);		
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to import APIs");
		System.out.println("Import an API including the API-Definition using environment properties file: env.api-env.properties:");
		System.out.println(getBinaryName()+" api import -c samples/basic/minimal-config-api-definition.json -s api-env");
		System.out.println();
		System.out.println();
		System.out.println(getBinaryName()+" api import -c samples/basic/minimal-config.json -a ../petstore.json -h localhost -u apiadmin -p changeme");
		System.out.println(getBinaryName()+" api import -c samples/basic/minimal-config.json -a ../petstore.json -h localhost -u apiadmin -p changeme -s prod");
		System.out.println(getBinaryName()+" api import -c samples/complex/complete-config.json -a ../petstore.json -h localhost -u apiadmin -p changeme");
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/tree/develop/modules/api-import/assembly/samples");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "API-Import";
	}


}
