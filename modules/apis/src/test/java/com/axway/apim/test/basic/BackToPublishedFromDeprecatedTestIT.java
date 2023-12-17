package com.axway.apim.test.basic;

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
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.DelegatingPayloadVariableExtractor.Builder.fromBody;


@ContextConfiguration(classes = {EndpointConfig.class})
public class BackToPublishedFromDeprecatedTestIT extends TestNGCitrusSpringSupport {

    @Autowired
    HttpClient apiManager;

	@CitrusTest
	@Test @Parameters("context")
	public void run() throws IOException {
        ImportTestAction swaggerImport = new ImportTestAction();
		description("Once an API in status deprecated, it must be possible to go back to published, basically remove the deperation status");
		variable("useApiAdmin", "true");
		variable("apiNumber", RandomNumberFunction.getRandomNumber(4, true));
		variable("apiPath", "/my-test-api-${apiNumber}");
		variable("apiName", "My-Test-API-${apiNumber}");

		$(echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable("state", "published");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
        variable("expectedReturnCode", "0");
        variable("version", "1.0.0");
        $(action(swaggerImport));

		$(echo("####### Validate API: '${apiName}' on path: '${apiPath}' with Status Published #######"));
        $(http().client(apiManager).send().get("/proxies"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.expression("$.[?(@.path=='${apiPath}')].state", "${state}"))
            .extract(fromBody()
			.expression("$.[?(@.path=='${apiPath}')].id", "apiId")));

        $(echo("####### Setting API: '${apiName}' on path: '${apiPath}' to deprecated #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable("state", "deprecated");
        variable("enforce", "true"); // Must be enforced, as it's a breaking change
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has Status Deprecated #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.expression("$.[?(@.id=='${apiId}')].state", "published")
			.expression("$.[?(@.id=='${apiId}')].deprecated", "true")));

		Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Z")));
		cal.add(Calendar.DAY_OF_YEAR, 30);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.add(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Z")));
		String retirementDate = format.format(cal.getTime());

        $(echo("####### Setting API: '${apiName}' on path: '${apiPath}' to deprecated including a retirement date: "+retirementDate+" #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable("state", "deprecated");
        variable("retirementDate", retirementDate);
        variable("enforce", "true"); // Must be enforced, as it's a breaking change
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/5_flexible-status-retirementDate.json");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has Status Deprecated and the correct RetirementDate! #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.expression("$.[?(@.id=='${apiId}')].state", "published")
			.expression("$.[?(@.id=='${apiId}')].retirementDate", cal.getTimeInMillis())
			.expression("$.[?(@.id=='${apiId}')].deprecated", "true")));

        $(echo("####### Perform No-Change test on API: '${apiName}' on path: '${apiPath}' including same retirement date #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable("state", "deprecated");
        variable("retirementDate", retirementDate);
        variable("enforce", "true"); // Must be enforced, as it's a breaking change
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/5_flexible-status-retirementDate.json");
        variable("expectedReturnCode", "10");
        $(action(swaggerImport));

		// Change the Retiremendate
		cal.add(Calendar.DAY_OF_YEAR, 60);
		format = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Z")));
		retirementDate = format.format(cal.getTime());

        $(echo("####### Change the retirementDate for API: '${apiName}' on path: '${apiPath}' #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable("state", "deprecated");
        variable("retirementDate", retirementDate);
        variable("enforce", "true"); // Must be enforced, as it's a breaking change
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/5_flexible-status-retirementDate.json");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' has Status Deprecated and the correct RetirementDate! #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
            .expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.expression("$.[?(@.id=='${apiId}')].state", "published")
			.expression("$.[?(@.id=='${apiId}')].retirementDate", cal.getTimeInMillis())
			.expression("$.[?(@.id=='${apiId}')].deprecated", "true")));

        $(echo("####### Going back to status Published  for API: '${apiName}' on path: '${apiPath}' #######"));
        variable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
        variable("state", "published");
        variable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
        variable("expectedReturnCode", "0");
        $(action(swaggerImport));

        $(echo("####### Validate API: '${apiName}' on path: '${apiPath}' hasn't anymore deprecation #######"));
        $(http().client(apiManager).send().get("/proxies/${apiId}"));
        $(http().client(apiManager).receive().response(HttpStatus.OK).message().type(MessageType.JSON).validate(jsonPath()
			.expression("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.expression("$.[?(@.id=='${apiId}')].state", "${state}")
			.expression("$.[?(@.id=='${apiId}')].deprecated", "false")));
	}
}
