package com.axway.apim.test.cacerts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="BasicCaCertTest")
public class BasicCaCertTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private ImportTestAction swaggerImport;
	
	@CitrusTest(name = "BasicCaCertTest")
	public void run() {
		description("Test to validate, that Certificates will be imported");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/cacerts-test-${apiNumber}");
		variable("apiName", "Certificates Test ${apiNumber}");
		variable("status", "unpublished");

		echo("####### Importing API: '${apiName}' on path: '${apiPath}' with following settings: #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/cacerts/1_basic_certs.json");
		createVariable("certFile4", "/com/axway/apim/test/files/cacerts/../certificates/DSTRootCAX3.crt");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' with correct settings #######");
		http().client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.validate("$.[?(@.path=='${apiPath}')].caCerts[?(@.md5Fingerprint=='41:03:52:DC:0F:F7:50:1B:16:F0:02:8E:BA:6F:45:C5')].name", "@assertThat(containsString(Digital Signature Trust Co))@")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### Simulate Re-Import without changes #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/cacerts/1_basic_certs.json");
		createVariable("expectedReturnCode", "10");
		action(swaggerImport);
		
		echo("####### Re-Import with a new certificate #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/com/axway/apim/test/files/security/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/com/axway/apim/test/files/cacerts/1_basic_certs.json");
		createVariable("certFile4", "/com/axway/apim/test/files/cacerts/../certificates/GlobalSignRootCA-R2.crt");
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate the new certificate has been replaced #######");
		http().client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "unpublished")
			.validate("$.[?(@.path=='${apiPath}')].caCerts[?(@.md5Fingerprint=='94:14:77:7E:3E:5E:FD:8F:30:BD:41:B0:CF:E7:D0:30')].name", "@assertThat(containsString(GlobalSign Root CA - R2))@")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
	}
}
