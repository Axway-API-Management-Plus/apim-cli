package com.axway.apim.api.export.lib.cli;

import com.axway.apim.lib.*;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.rest.Console;
import org.apache.commons.cli.Option;

import com.axway.apim.api.export.lib.params.APICheckCertificatesParams;

public class CLICheckCertificatesOptions extends CLIOptions {

    private CLICheckCertificatesOptions(String[] args) {
        super(args);
    }

    public static CLIOptions create(String[] args) throws AppException {
        CLIOptions cliOptions = new CLICheckCertificatesOptions(args);
        cliOptions = new CLIAPIFilterOptions(cliOptions);
        cliOptions = new StandardExportCLIOptions(cliOptions);
        cliOptions = new CoreCLIOptions(cliOptions);
        cliOptions.addOptions();
        cliOptions.parse();
        return cliOptions;
    }

    @Override
    public void addOptions() {
        Option option = new Option("days", true, "The number of days for which you want to check if certificates expire.");
        option.setRequired(true);
        option.setArgName("30");
        addOption(option);
    }

    @Override
    public void printUsage(String message, String[] args) {
        super.printUsage(message, args);
        Console.println("----------------------------------------------------------------------------------------");
        Console.println(getAppName());
        Console.println("Check Certificate examples:");
        Console.println();
        Console.println("Certificate expires in next 90 days");
        Console.println(getBinaryName() + " api check-certs -days 90");
        Console.println();
        Console.println("For more information and advanced examples please visit:");
        Console.println("https://github.com/Axway-API-Management-Plus/apim-cli/wiki");
    }

    @Override
    public String getAppName() {
        return "API Check certificates";
    }

    @Override
    public Parameters getParams() {
        APICheckCertificatesParams params = new APICheckCertificatesParams();
        params.setNumberOfDays(Integer.parseInt(getValue("days")));
        return params;
    }
}
