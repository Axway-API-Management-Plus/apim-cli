package com.axway.apim.appimport.it.basic;

import java.io.IOException;

import com.axway.apim.test.actions.TestParams;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.appimport.it.ImportAppTestAction;
import com.axway.apim.lib.error.AppException;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;

@Test
public class ImportAppInvalidAPITestIT extends TestNGCitrusTestRunner {

    private static String PACKAGE = "/com/axway/apim/appimport/apps/basic/";

    @CitrusTest
    @Test
    @Parameters("context")
    public void importApplicationBasicTest(@Optional @CitrusResource TestContext context) throws IOException, AppException {
        description("Trying to import an application that requests access to an unknown API");
        ImportAppTestAction importApp = new ImportAppTestAction(context);

        variable("appName", "Complete-App-" + importApp.getRandomNum());
        variable("apiName1", "This-API-is-unkown");

        echo("####### Import application: '${appName}' with access to ONE UNKNOWN API #######");
        createVariable(TestParams.PARAM_CONFIGFILE, PACKAGE + "AppWithAPIAccess.json");
        createVariable(TestParams.PARAM_EXPECTED_RC, "56");
        importApp.doExecute(context);
    }
}
