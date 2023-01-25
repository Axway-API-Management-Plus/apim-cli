package com.axway.apim.api.export.lib.cli;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.Option;

import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardExportCLIOptions;

public class CLIAPIExportOptions extends CLIOptions {
	
	private CLIAPIExportOptions(String[] args) {
		super(args);
	}
	
	public static CLIOptions create(String[] args) throws AppException {
		CLIOptions cliOptions = new CLIAPIExportOptions(args);
		cliOptions = new CLIAPIFilterOptions(cliOptions);
		cliOptions = new StandardExportCLIOptions(cliOptions);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}
	
	@Override
	public Parameters getParams() {
		APIExportParams params = new APIExportParams();
		params.setUseFEAPIDefinition(hasOption("useFEAPIDefinition"));
		params.setDatPassword(getValue("datPassword"));
		params.setExportMethods(hasOption("exportMethods"));
		return params;
	}

	@Override
	public void addOptions() {
		Option option = new Option("useFEAPIDefinition", "If this flag is set, the exported API contains the API-Specification (e.g. Swagger-File) "
				+ "from the FE-API instead of the original imported API. But the specification contains the host, basePath and scheme from the backend.");
		option.setRequired(false);
		addOption(option);
		
		option = new Option("datPassword", true, "Password used when exporting APIs in a DAT-Format.");
		option.setRequired(false);
		option.setArgName("myExportPassword");
		addOption(option);

		option = new Option("exportMethods", "If this flag is set, the exported API contains API Methods from frontend API");
		option.setRequired(false);
		addOption(option);
	}
	
	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		Console.println("----------------------------------------------------------------------------------------");
		Console.println("How to get APIs with different output formats");
		Console.println("Get all APIs on console using environment properties: env.api-env.properties:");
		Console.println(getBinaryName()+" api get -s api-env");
		Console.println("Same as before, but with output format JSON:");
		Console.println(getBinaryName()+" api get -s api-env -o json");
		Console.println("Result as CSV-File with all possible information:");
		Console.println(getBinaryName()+" api get -s api-env -o json -ultra");
		Console.println();
		Console.println();
		Console.println("How to filter the list of selected APIs:");
		Console.println(getBinaryName()+" api get -s api-env -n \"*API*\" -o csv");
		Console.println(getBinaryName()+" api get -s api-env -id f6106454-1651-430e-8a2f-e3514afad8ee");
		Console.println(getBinaryName()+" api get -s api-env -policy \"*Policy ABC*\" -o json");
		Console.println(getBinaryName()+" api get -s api-env -name \"*API*\" -policy \"*Policy ABC*\"");
		Console.println();
		Console.println("For more information and advanced examples please visit:");
		Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Application-Export";
	}
}
