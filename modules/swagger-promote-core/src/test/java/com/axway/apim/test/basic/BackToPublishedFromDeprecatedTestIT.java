package com.axway.apim.test.basic;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test
public class BackToPublishedFromDeprecatedTestIT extends TestNGCitrusTestRunner {

	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException, AppException {
		swaggerImport = new ImportTestAction();
		description("Once an API in status deprecated, it must be possible to go back to published, basically remove the deperation status");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/my-test-api-${apiNumber}");
		variable("apiName", "My-Test-API-${apiNumber}");

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("state", "published");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' with Status Published #######");
		http(builder -> builder.client("apiManager").send().get("/proxies").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "${state}")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId"));
		
		echo("####### Setting API: '${apiName}' on path: '${apiPath}' to deprecated #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("state", "deprecated");
		createVariable("enforce", "true"); // Must be enforced, as it's a breaking change
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has Status Deprecated #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "published")
			.validate("$.[?(@.id=='${apiId}')].deprecated", "true"));
		
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
		
		echo("####### Setting API: '${apiName}' on path: '${apiPath}' to deprecated including a retirement date: "+retirementDate+" #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("state", "deprecated");
		createVariable("retirementDate", retirementDate);
		createVariable("enforce", "true"); // Must be enforced, as it's a breaking change
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/5_flexible-status-retirementDate.json");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has Status Deprecated and the correct RetirementDate! #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "published")
			.validate("$.[?(@.id=='${apiId}')].retirementDate", cal.getTimeInMillis())
			.validate("$.[?(@.id=='${apiId}')].deprecated", "true"));
		
		echo("####### Perform No-Change test on API: '${apiName}' on path: '${apiPath}' including same retirement date #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("state", "deprecated");
		createVariable("retirementDate", retirementDate);
		createVariable("enforce", "true"); // Must be enforced, as it's a breaking change
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/5_flexible-status-retirementDate.json");
		createVariable("expectedReturnCode", "10");
		swaggerImport.doExecute(context);
		
		// Change the Retiremendate
		cal.add(Calendar.DAY_OF_YEAR, 60);
		format = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
		format.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Z")));
		retirementDate = format.format(cal.getTime());
		
		echo("####### Change the retirementDate for API: '${apiName}' on path: '${apiPath}' #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("state", "deprecated");
		createVariable("retirementDate", retirementDate);
		createVariable("enforce", "true"); // Must be enforced, as it's a breaking change
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/5_flexible-status-retirementDate.json");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has Status Deprecated and the correct RetirementDate! #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "published")
			.validate("$.[?(@.id=='${apiId}')].retirementDate", cal.getTimeInMillis())
			.validate("$.[?(@.id=='${apiId}')].deprecated", "true"));
		
		echo("####### Going back to status Published  for API: '${apiName}' on path: '${apiPath}' #######");		
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("state", "published");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/basic/4_flexible-status-config.json");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' hasn't anymore deprecation #######");
		http(builder -> builder.client("apiManager").send().get("/proxies/${apiId}").header("Content-Type", "application/json"));

		http(builder -> builder.client("apiManager").receive().response(HttpStatus.OK).messageType(MessageType.JSON)
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "${state}")
			.validate("$.[?(@.id=='${apiId}')].deprecated", "false"));
	}
}
