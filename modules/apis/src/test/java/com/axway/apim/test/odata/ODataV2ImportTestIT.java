package com.axway.apim.test.odata;

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


@ContextConfiguration(classes = {EndpointConfig.class})
public class ODataV2ImportTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

    @CitrusTest
    @Test
    public void run() throws IOException {
        ImportTestAction importAction = new ImportTestAction();
        description("Import an OData V2 specification that must be converted into an OpenAPI V3 specification.");
        variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
        variable("apiPath", "/odata-v2-api-${apiNumber}");
        variable("apiName", "OData-V2-API-${apiNumber}");
        variable("backendBasepath", "https://services.odata.org/V2/Northwind/Northwind.svc/");
        variable("state", "unpublished");
        $(echo("####### Importing OData V2 API: '${apiName}' on path: '${apiPath}' #######"));
        variable(ImportTestAction.API_DEFINITION, "/api_definition_1/ODataV2NorthWindMetadata.xml");
        variable(ImportTestAction.API_CONFIG, "/com/axway/apim/test/files/basic/minimal-config-with-backendBasepath.json");
        variable("expectedReturnCode", "0");
        $(action(importAction));
        $(echo("####### Validate OData V2 API: '${apiName}' on path: '${apiPath}' has been imported #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")));
        $(echo("####### RE-Importing same API: '${apiName}' on path: '${apiPath}' without changes. Expecting No-Change. #######"));
        variable("expectedReturnCode", "10");
        $(action(importAction));
    }
}
