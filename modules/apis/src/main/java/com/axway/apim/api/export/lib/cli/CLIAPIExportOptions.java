package com.axway.apim.api.export.lib.cli;

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
	
	public static CLIOptions create(String[] args) {
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
		return params;
	}

	@Override
	public void addOptions() {
		Option option = new Option("useFEAPIDefinition", "If this flag is set, the exported API contains the API-Specification (e.g. Swagger-File) "
				+ "from the FE-API instead of the original imported API. But the specification contains the host, basePath and scheme from the backend.");
		option.setRequired(false);
		addOption(option);
	}
	
	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("How to get APIs with different output formats");
		System.out.println("Get all APIs on console using environment properties: env.api-env.properties:");
		System.out.println(getBinaryName()+" api get -s api-env");
		System.out.println("Same as before, but with output format JSON:");
		System.out.println(getBinaryName()+" api get -s api-env -o json");
		System.out.println("Result as CSV-File with all possible information:");
		System.out.println(getBinaryName()+" api get -s api-env -o json -ultra");
		System.out.println();
		System.out.println();
		System.out.println("How to filter the list of selected APIs:");
		System.out.println(getBinaryName()+" api get -s api-env -n \"*API*\" -o csv");
		System.out.println(getBinaryName()+" api get -s api-env -id f6106454-1651-430e-8a2f-e3514afad8ee");
		System.out.println(getBinaryName()+" api get -s api-env -policy \"*Policy ABC*\" -o json");
		System.out.println(getBinaryName()+" api get -s api-env -name \"*API*\" -policy \"*Policy ABC*\"");
		System.out.println();
		System.out.println("For more information and advanced examples please visit:");
		System.out.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "Application-Export";
	}
}
