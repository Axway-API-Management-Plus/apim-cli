package com.axway.apim.users.lib.cli;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import com.axway.apim.users.lib.params.UserExportParams;

public class UserDeleteCLIOptions extends CLIOptions {

    private UserDeleteCLIOptions(String[] args) {
        super(args);
    }

    public static CLIOptions create(String[] args) throws AppException {
        CLIOptions cliOptions = new UserDeleteCLIOptions(args);
        cliOptions = new CLIUserFilterOptions(cliOptions);
        cliOptions = new CoreCLIOptions(cliOptions);
        cliOptions.addOptions();
        cliOptions.parse();
        return cliOptions;
    }

    @Override
    public void printUsage(String message, String[] args) {
        super.printUsage(message, args);
        Console.println("----------------------------------------------------------------------------------------");
        Console.println("How to delete users using different filter options:");
        Console.println(getBinaryName() + " user delete -s api-env");
        Console.println(getBinaryName() + " user delete -s api-env -n \"*Name of user*\" -loginName \"*loginNameOfUser*\"");
        Console.println(getBinaryName() + " user delete -s api-env -id f6106454-1651-430e-8a2f-e3514afad8ee");
        Console.println();
        Console.println();
        Console.println("For more information and advanced examples please visit:");
        Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
    }

    @Override
    protected String getAppName() {
        return "User-Management";
    }

    @Override
    public Parameters getParams() {
        return new UserExportParams();
    }

    @Override
    public void addOptions() {
    }
}
