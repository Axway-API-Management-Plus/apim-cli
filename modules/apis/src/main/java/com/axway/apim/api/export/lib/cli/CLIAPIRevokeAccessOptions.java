package com.axway.apim.api.export.lib.cli;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;

public class CLIAPIRevokeAccessOptions extends CLIAPIGrantAccessOptions{

    public CLIAPIRevokeAccessOptions(String[] args) {
        super(args);
    }


    public static CLIOptions create(String[] args) throws AppException {
        CLIOptions cliOptions = new CLIAPIRevokeAccessOptions(args);
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
        Console.println("Revoke access for selected organizations to one or more APIs.");
        Console.println("You can use all known API filters to select the desired APIs. However, only APIs that are in the Published status are considered.");
        Console.println(getBinaryName()+" api revoke-access -s api-env -orgId <UUID-ID-OF-THE-ORG> -id <UUID-ID-OF-THE-API>");
        Console.println(getBinaryName()+" api revoke-access -s api-env -orgName *MyOrg* -n *NameOfAPI*");
        Console.println();
        Console.println();
        Console.println("For more information and advanced examples please visit:");
        Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
    }

    @Override
    protected String getAppName() {
        return "Revoke access";
    }
}
