package com.axway.apim.test.quota;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;

@ContextConfiguration(classes = {EndpointConfig.class})

public class CreateAndUpdateQuotaWithNewMethodAndSpecIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;


    @CitrusTest
    @Test
    public void run() throws IOException, InterruptedException {

        $(echo("#######Get initial system quota #######"));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000000"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.restrictions.length()", "size")));

        $(echo("Number of system quota : ${size}"));

        ImportTestAction swaggerImport = new ImportTestAction();
        description("Import an API containing a quota definition");
        variable("useApiAdmin", "true");
        variable("apiName", "Teste api - Quotas");

        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/quota/upsert_quota_and_spec/step1/api.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/upsert_quota_and_spec/step1/api-config.json");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Check System-Quotas have been setup as configured #######"));
        $(echo("####### ############ Sleep 5 seconds ##################### #######"));
        $(sleep().seconds(5));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000000"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.restrictions.length()", "size2")));
        $(echo("Number of system quota After import : ${size2}"));
        $(testContext -> {
            Assert.assertEquals(Integer.parseInt(testContext.getVariable("size2")), Integer.parseInt(testContext.getVariable("size")) + 2);
        });


        $(echo("####### Executing a Quota-No-Change import #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/quota/upsert_quota_and_spec/step2/api.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/quota/upsert_quota_and_spec/step2/api-config.json");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Check System-Quotas have been setup as configured #######"));
        $(echo("####### ############ Sleep 2 seconds ##################### #######"));
        $(sleep().seconds(5));
        $(http().client(apiManager).send().get("/quotas/00000000-0000-0000-0000-000000000000"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.restrictions.length()", "size3")));

        $(echo("Number of system quota After import2 : ${size3}"));


        $(testContext -> {
            Assert.assertEquals(Integer.parseInt(testContext.getVariable("size3")), Integer.parseInt(testContext.getVariable("size")) + 3);
        });

    }

}
