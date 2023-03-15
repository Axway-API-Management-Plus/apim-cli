package com.axway.apim.api.export.lib.cli;


import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;

public class CLIAPIUnpublishOptions extends CLIOptions {

    private CLIAPIUnpublishOptions(String[] args) {
        super(args);
    }

    public static CLIOptions create(String[] args) throws AppException {
        CLIOptions cliOptions = new CLIAPIUnpublishOptions(args);
        cliOptions = new CLIAPIFilterOptions(cliOptions);
        cliOptions = new CoreCLIOptions(cliOptions);
        cliOptions.addOptions();
        cliOptions.parse();
        return cliOptions;
    }

    @Override
    public void printUsage(String message, String[] args) {
        super.printUsage(message, args);
        Console.println("----------------------------------------------------------------------------------------");
        Console.println("How to unpublish APIs using different filter options:");
        Console.println(getBinaryName() + " api unpublish -s api-env");
        Console.println(getBinaryName() + " api unpublish -s api-env -n \"*API*\"");
        Console.println(getBinaryName() + " api unpublish -s api-env -id f6106454-1651-430e-8a2f-e3514afad8ee");
        Console.println(getBinaryName() + " api unpublish -s api-env -policy \"*Policy ABC*\"");
        Console.println(getBinaryName() + " api unpublish -s api-env -name \"*API*\" -policy \"*Policy ABC*\"");
        Console.println();
        Console.println();
        Console.println("For more information and advanced examples please visit:");
        Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
    }

    @Override
    protected String getAppName() {
        return "Unpublish APIs";
    }

    @Override
    public Parameters getParams() {
        return new APIExportParams();
    }

    @Override
    public void addOptions() {
        //empty
    }
}
