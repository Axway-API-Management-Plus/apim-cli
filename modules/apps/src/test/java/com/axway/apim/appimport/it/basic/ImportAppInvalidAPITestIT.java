package com.axway.apim.appimport.it.basic;

import com.axway.apim.TestUtils;
import com.axway.apim.appimport.ClientApplicationImportApp;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;

@Test
public class ImportAppInvalidAPITestIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    @Test
    public void importApplicationBasicTest(@Optional @CitrusResource TestContext context) {
        description("Trying to import an application that requests access to an unknown API");
        variable("appName", "Complete-App-" + RandomNumberFunction.getRandomNumber(4, true));
        variable("apiName1", "This-API-is-unkown");
        $(echo("####### Import application: '${appName}' with access to ONE UNKNOWN API #######"));
        String updatedConfigFile = TestUtils.createTestConfig("/com/axway/apim/appimport/apps/basic/AppWithAPIAccess.json",
            context, "apps", true);
        $(testContext -> {
            String[] args = {"app", "import", "-c", updatedConfigFile, "-h", testContext.replaceDynamicContentInString("${apiManagerHost}"), "-u", testContext.replaceDynamicContentInString("${apiManagerUser}"), "-p", testContext.replaceDynamicContentInString("${apiManagerPass}")};
            int returnCode = ClientApplicationImportApp.importApp(args);
            if (returnCode != 56)
                throw new ValidationException("Expected RC was: 56 but got: " + returnCode);
        });
    }
}
