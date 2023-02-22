package com.axway.apim.appimport.lib;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.StandardImportCLIOptions;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.Option;

public class AppImportCLIOptions extends CLIOptions {

    private AppImportCLIOptions(String[] args) {
        super(args);
    }

    public static CLIOptions create(String[] args) throws AppException {
        CLIOptions cliOptions = new AppImportCLIOptions(args);
        cliOptions = new StandardImportCLIOptions(cliOptions);
        cliOptions = new CoreCLIOptions(cliOptions);
        cliOptions.addOptions();
        cliOptions.parse();
        return cliOptions;
    }

    @Override
    public void addOptions() {
        // Define command line options required for Application export
        Option option = new Option("c", "config", true, "This is the JSON-Formatted Application-Config file containing the application. You may get that config file using apim app get with output set to JSON.");
        option.setRequired(true);
        option.setArgName("app_config.json");
        addOption(option);
    }

    @Override
    public void printUsage(String message, String[] args) {
        super.printUsage(message, args);
        Console.println("----------------------------------------------------------------------------------------");
        Console.println("How to imports applications using JSON the JSON-Config format");
        Console.println("Import an application using enviornment properties: env.api-env.properties:");
        Console.println(getBinaryName() + " app import -c myApps/great-app.json -s api-env");
        Console.println(getBinaryName() + " app import -c myApps/another-great-app.json -h localhost -u apiadmin -p changeme");
        Console.println();
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
        AppImportParams params = new AppImportParams();
        params.setConfig(getValue("c"));
        return params;
    }
}
