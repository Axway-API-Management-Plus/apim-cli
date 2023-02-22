package com.axway.apim.organization.lib;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardExportCLIOptions;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import com.axway.apim.organization.lib.cli.CLIOrgFilterOptions;

public class OrgExportCLIOptions extends CLIOptions {

    private OrgExportCLIOptions(String[] args) {
        super(args);
    }

    public static CLIOptions create(String[] args) throws AppException {
        CLIOptions cliOptions = new OrgExportCLIOptions(args);
        cliOptions = new CLIOrgFilterOptions(cliOptions);
        cliOptions = new StandardExportCLIOptions(cliOptions);
        cliOptions = new CoreCLIOptions(cliOptions);
        cliOptions.addOptions();
        cliOptions.parse();
        return cliOptions;
    }

    @Override
    public void printUsage(String message, String[] args) {
        super.printUsage(message, args);
        Console.println("----------------------------------------------------------------------------------------");
        Console.println("How to get/export organizations with different output formats");
        Console.println("Get all organizations on console using environment properties: env.api-env.properties:");
        Console.println(getBinaryName() + " org get -s api-env");
        Console.println("Same as before, but with output format JSON - As it is used to import organizations");
        Console.println(getBinaryName() + " org get -s api-env -o json");
        Console.println();
        Console.println();
        Console.println("How to filter the list of selected organizations:");
        Console.println(getBinaryName() + " org get -s api-env -n \"Partner *\" -o json");
        Console.println(getBinaryName() + " org get -s api-env -n \"*Private*\" -t /tmp/exported_apps -o json -deleteTarget ");
        Console.println();
        Console.println("For more information and advanced examples please visit:");
        Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
    }

    @Override
    protected String getAppName() {
        return "Application-Export";
    }

    @Override
    public Parameters getParams() {
        OrgExportParams params = new OrgExportParams();
        params.setName(getValue("name"));
        params.setId(getValue("id"));
        params.setDev(getValue("dev"));
        return params;
    }

    @Override
    public void addOptions() {
    }
}
