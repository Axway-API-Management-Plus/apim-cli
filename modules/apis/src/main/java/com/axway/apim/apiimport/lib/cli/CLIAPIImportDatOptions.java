package com.axway.apim.apiimport.lib.cli;

import com.axway.apim.apiimport.lib.params.APIImportDatParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardImportCLIOptions;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.Option;

public class CLIAPIImportDatOptions extends CLIOptions {


	private CLIAPIImportDatOptions(String[] args) {
		super(args);
	}

	public static CLIOptions create(String[] args) throws AppException {
		CLIOptions cliOptions = new CLIAPIImportDatOptions(args);
		cliOptions = new CoreCLIOptions(cliOptions);
		cliOptions.addOptions();
		cliOptions.parse();
		return cliOptions;
	}

	@Override
	public void addOptions() {
		// Define command line options required for Application export
		Option option = new Option("a", "apidefinition", true, "api collection dat file");
		option.setRequired(true);
		option.setArgName("api-export.dat");
		addOption(option);

        option = new  Option("orgName", true, "Organization name to use when importing dat file");
        option.setRequired(true);
        option.setArgName("*My organization*");
        addOption(option);

        option = new Option("datPassword", true, "Password used when exporting APIs in a DAT-Format.");
        option.setRequired(false);
        option.setArgName("myExportPassword");
        addOption(option);

        new StandardImportCLIOptions().addOptions(this);
	}

	@Override
	public void printUsage(String message, String[] args) {
		super.printUsage(message, args);
		Console.println("----------------------------------------------------------------------------------------");
		Console.println("How to import APIs using dat file");
		Console.println(getBinaryName()+" api import-dat -a api-export.dat -datPassword change me -orgName Development -s api-env");
		Console.println();
		Console.println();
		Console.println("For more information and advanced examples please visit:");
		Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/tree/develop/modules/api-import/assembly/samples");
		Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
	}

	@Override
	protected String getAppName() {
		return "API-Import";
	}

	@Override
	public Parameters getParams() {
		APIImportDatParams params = new APIImportDatParams();
		params.setApiDefinition(getValue("apidefinition"));
        params.setDatPassword(getValue("datPassword"));
        params.setOrgName(getValue("orgName"));
        return params;
	}
}
