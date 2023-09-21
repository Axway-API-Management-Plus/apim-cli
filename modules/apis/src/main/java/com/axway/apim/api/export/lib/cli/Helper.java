package com.axway.apim.api.export.lib.cli;

import com.axway.apim.api.export.lib.params.APIGrantAccessParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.CoreCLIOptions;
import com.axway.apim.lib.Parameters;
import com.axway.apim.lib.error.AppException;

public final class Helper {

    private Helper(){
        throw new IllegalStateException("Access blocked");
    }

    public static Parameters getParams(CLIOptions cliOptions)  {
        APIGrantAccessParams params = new APIGrantAccessParams();
        params.setOrgId(cliOptions.getValue("orgId"));
        params.setOrgName(cliOptions.getValue("orgName"));
        params.setAppId(cliOptions.getValue("appId"));
        params.setAppName(cliOptions.getValue("appName"));
        return params;
    }

    public static CLIOptions create( CLIOptions cliOptions) throws AppException {
        cliOptions = new CLIAPIFilterOptions(cliOptions);
        cliOptions = new CoreCLIOptions(cliOptions);
        cliOptions.addOptions();
        cliOptions.parse();
        return cliOptions;
    }
}
