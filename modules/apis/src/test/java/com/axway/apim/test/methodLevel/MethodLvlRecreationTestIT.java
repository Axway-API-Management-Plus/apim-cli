package com.axway.apim.test.methodLevel;

import com.axway.apim.EndpointConfig;
import com.axway.apim.test.ImportTestAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;


@ContextConfiguration(classes = {EndpointConfig.class})
public class MethodLvlRecreationTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
        description("Make sure, Method-Level config is working on API-Recreation. See issue: #119");

        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/method-lvl-recreation-${apiNumber}");
        variable("apiName", "Method-Level-Recreation-API-${apiNumber}");

        $(echo("####### Try to replicate an API having Method-Level settings declared #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/methodLevel/method-level-inbound-api-key.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        variable("securityProfileName", "APIKeyBased${apiNumber}");
        $(action(swaggerImport));

        $(echo("####### Validate the FE-API has been configured with API-Key on method level #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "${state}")
                .expression("$.[?(@.path=='${apiPath}')].securityProfiles.[?(@.name=='${securityProfileName}')].devices[0].type", "apiKey"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(http().client(apiManager).send().get("/proxies/${apiId}/operations"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.[?(@.name=='findPetsByStatus')].id", "apiMethodId")));

        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].inboundProfiles.${apiMethodId}.securityProfile", "${securityProfileName}")));

        $(echo("####### Force a Re-Creation of the API with the same Method-Level-Settings #######"));
        variable(ImportTestAction.API_DEFINITION, "/com/axway/apim/test/files/basic/petstore2.json");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/methodLevel/method-level-inbound-api-key.json");
        variable("state", "unpublished");
        variable("expectedReturnCode", "0");
        variable("securityProfileName", "APIKeyBased${apiNumber}");
        $(action(swaggerImport));

        $(echo("####### Make sure, the Method-Level settings are still in place #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
                .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
                .expression("$.[?(@.path=='${apiPath}')].state", "${state}")
                .expression("$.[?(@.path=='${apiPath}')].securityProfiles.[?(@.name=='${securityProfileName}')].devices[0].type", "apiKey"))
            .extract(fromBody()
                .expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(http().client(apiManager).send().get("/proxies/${apiId}/operations"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).extract(fromBody()
            .expression("$.[?(@.name=='findPetsByStatus')].id", "apiMethodId")));

        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].inboundProfiles.${apiMethodId}.securityProfile", "${securityProfileName}")));
		/*

		echo("####### Execute a No-Change test #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/methodLevel/method-level-inbound-api-key-and-cors.json");
		createVariable("state", "published");
		createVariable("expectedReturnCode", "0");
		createVariable("securityProfileName", "APIKeyBased${apiNumber}");
		swaggerImport.doExecute(context);

		echo("####### Validate the FE-API has been configured with API-Key on method level #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));
		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "${state}")
			.validate("$.[?(@.id=='${apiId}')].securityProfiles.[?(@.name=='${securityProfileName}')].devices[0].type", "apiKey")
			.validate("$.[?(@.id=='${apiId}')].corsProfiles.[0].name", "New CORS Profile")
			.validate("$.[?(@.id=='${apiId}')].inboundProfiles.${apiMethodId}.securityProfile", "${securityProfileName}")
			.validate("$.[?(@.id=='${apiId}')].inboundProfiles.${apiMethodId}.corsProfile", "New CORS Profile"));*/
    }
}
