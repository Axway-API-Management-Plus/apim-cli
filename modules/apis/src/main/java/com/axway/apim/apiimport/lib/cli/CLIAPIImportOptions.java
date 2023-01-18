package com.axway.apim.apiimport.lib.cli;

import org.apache.commons.cli.Option;

import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.CoreParameters.Mode;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardImportCLIOptions;
import com.axway.apim.lib.errorHandling.AppException;

public class CLIAPIImportOptions extends CLIOptions {

	private CLIAPIImportOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) throws AppException {
		CLIOptions cliOptions = new CLIAPIImportOptions(args);
		cliOptions = new StandardImportCLIOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public void addOptions() {
		// Define command line options required for Application export
		Option option = new Option("a", "apidefinition", true, "(Optional) The API Specification either as OpenAPI (JSON/YAML) or a WSDL for SOAP-Services:\n"
				+ "- in local filesystem using a relative or absolute path. Example: swagger_file.json\n"
				+ "  Please note: Local filesystem is not supported for WSDLs. Please use direct URL or a URL-Reference-File.\n"
				+ "- a URL providing the Swagger-File or WSDL-File. Examples:\n"
				+ "  [username/password@]https://any.host.com/my/path/to/swagger.json\n"
				+ "  [username/password@]http://www.dneonline.com/calculator.asmx?wsdl\n"
				+ "- a reference file called anyname-i-want.url which contains a line with the URL\n"
				+ "  (same format as above for OpenAPI or WSDL)."
				+ "  If not specified, the API Specification configuration is read directly from the API-Config file.");
		option.setRequired(false);
		option.setArgName("swagger_file.json");
		addOption(option);

		option = new Option("c", "config", true, "This is the JSON-Formatted API-Config containing information how to expose the API. You may get that config file using apim api get with output set to JSON.");
		option.setRequired(true);
		option.setArgName("api_config.json");
		addOption(option);
		
		option = new Option("ignoreQuotas", "Use this flag to ignore configured API quotas.");
		option.setRequired(false);
		addOption(option);
		
		option = new Option("updateOnly", "If set, an existing actual API will be updated. If no actual API is found, the CLI stops.");
		option.setRequired(false);
		addOption(option);
		
		option = new Option("useFEAPIDefinition", "If this flag is set, the Actual-API contains the API-Definition (e.g. Swagger) from the FE-API instead of the original imported API.");
		option.setRequired(false);
		addOption(option);
		
		option = new Option("clientOrgsMode", true, "Controls how configured Client-Organizations are treated. Defaults to add!");
		option.setArgName("ignore|replace|add");
		addOption(option);
		
		option = new Option("clientAppsMode", true, "Controls how configured Client-Applications are treated. Defaults to add!");
		option.setArgName("ignore|replace|add");
		addOption(option);
		
		option = new Option("quotaMode", true, "Controls how quotas are managed in API-Manager. Defaults to add!");
		option.setArgName("ignore|replace|add");
		addOption(option);
		
		option = new Option("replaceHostInSwagger", true, "Controls if you want to replace the host in your Swagger-File ");
		option.setRequired(false);
		option.setArgName("true");
		addInternalOption(option);
		
		option = new Option("validateRemoteHost", true, "Disables the remote host validation which is turned on by default if a remote host is given");
		option.setRequired(false);
		option.setArgName("false");
		addInternalOption(option);
		
		option = new Option("changeOrganization", "Set this flag to allow to change the organization of an existing API.");
		option.setRequired(false);
		addInternalOption(option);
		
		option = new Option("detailsExportFile", true, "Configure a filename, to get a Key=Value file containing information about the created API.");
		option.setRequired(false);
		option.setArgName("APIDetails.properties");
		addOption(option);
		
		option = new Option("forceUpdate", "If set, the API is Re-Created even if the Desired- and Actual-State are equal.");
		option.setRequired(false);
		addOption(option);
		
		option = new Option("zeroDowntimeUpdate", "Always update a published APIs by creating a new API and switch clients to it. Defaults to false");
		option.setRequired(false);
		addInternalOption(option);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);		
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to import APIs");
		System.out.println("Import an API including the API-Specification using environment properties file: env.api-env.properties:");
		System.out.println(getBinaryName()+" api import -c samples/basic/minimal-config-api-specification.json -s api-env");
		System.out.println(getBinaryName()+" api import -c samples/basic/minimal-config-api-specification-filtered.json -s api-env");
		System.out.println(getBinaryName()+" api import -c samples/basic/odata-v2-northwind-api.json -s api-env");
		System.out.println();
		System.out.println();
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
	
	@Override
	public Parameters getParams() {
		APIImportParams params = new APIImportParams();
		params.setConfig(getValue("config"));
		params.setStageConfig(getValue("stagedConfig"));
		params.setApiDefintion(getValue("apidefinition"));
		params.setForceUpdate(hasOption("forceUpdate"));
		params.setChangeOrganization(hasOption("changeOrganization"));
		if(getValue("replaceHostInSwagger")!=null) params.setReplaceHostInSwagger(Boolean.parseBoolean(getValue("replaceHostInSwagger")));
		params.setUseFEAPIDefinition(hasOption("useFEAPIDefinition"));
		params.setIgnoreQuotas(hasOption("ignoreQuotas"));
		params.setUpdateOnly(hasOption("updateOnly"));
		params.setClientOrgsMode(Mode.valueOfDefault(getValue("clientOrgsMode")));
		params.setClientAppsMode(Mode.valueOfDefault(getValue("clientAppsMode")));
		params.setQuotaMode(Mode.valueOfDefault(getValue("quotaMode")));
		params.setDetailsExportFile(getValue("detailsExportFile"));
		params.setValidateRemoteHost(Boolean.parseBoolean(getValue("validateRemoteHost")));
		params.setZeroDowntimeUpdate(Boolean.parseBoolean(getValue("zeroDowntimeUpdate")));
		return params;
	}
}
