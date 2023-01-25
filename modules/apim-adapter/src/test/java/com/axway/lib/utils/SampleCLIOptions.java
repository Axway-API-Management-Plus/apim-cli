package com.axway.lib.utils;

import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.errorHandling.AppException;

public class SampleCLIOptions extends CLIOptions {

    private SampleCLIOptions(String[] args) {
        super(args);
    }

    public static CLIOptions create(String[] args) throws AppException {
        CLIOptions cliOptions = new SampleCLIOptions(args);
        cliOptions = new CoreCLIOptions(cliOptions);
        cliOptions.addOptions();
        cliOptions.parse();
        return cliOptions;
    }

    @Override
    public Parameters getParams() throws AppException {
        return new CoreParameters();
    }

    @Override
    public void addOptions() {
    }

}
