package com.axway.apim.users.lib.cli;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import com.axway.apim.users.lib.params.UserChangePasswordParams;
import org.apache.commons.cli.Option;

public class UserChangePasswordCLIOptions extends CLIOptions {

    private UserChangePasswordCLIOptions(String[] args) {
        super(args);
    }

    public static CLIOptions create(String[] args) throws AppException {
        CLIOptions cliOptions = new UserChangePasswordCLIOptions(args);
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
        Console.println("How to change the password of one or multiple users using different filter options:");
        Console.println(getBinaryName() + " user changepassword -s api-env -newpassword \"newPassword4allUsers\"");
        Console.println(getBinaryName() + " user changepassword -s api-env -n \"*Name of user*\" -loginName \"*loginNameOfUser*\" -newpassword \"aNewPassword\"");
        Console.println(getBinaryName() + " user changepassword -s api-env -id f6106454-1651-430e-8a2f-e3514afad8ee -newpassword \"aNewPassword\"");
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
        UserChangePasswordParams params = new UserChangePasswordParams();
        params.setNewPassword(getValue("newpassword"));
        return params;
    }

    @Override
    public void addOptions() {
        Option option = new Option("newpassword", true, "The password you would like to set for the user(s).");
        option.setRequired(true);
        option.setArgName("mysecurepassword");
        addOption(option);
    }
}
