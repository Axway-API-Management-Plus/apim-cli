package com.axway.apim.api.export.lib.cli;

import com.axway.apim.api.export.lib.params.APIExportParams;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.error.AppException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CLIAPIDeleteOptionsTest {

    @Test
    public void cliApiDelete() throws AppException {
        String[] args = {"-h", "localhost", "-n", "petstore"};
        CLIOptions options = CLIAPIDeleteOptions.create(args);
        APIExportParams params = (APIExportParams) options.getParams();
        Assert.assertEquals(params.getName(), "petstore");
    }
}
