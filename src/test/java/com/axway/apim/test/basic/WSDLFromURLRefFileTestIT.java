package com.axway.apim.test.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.axway.apim.test.WSDLImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="WSDLFromURLRefFileTestIT")
public class WSDLFromURLRefFileTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private WSDLImportTestAction wsdlImport;
	
	@CitrusTest(name = "WSDLFromURLRefFileTestIT")
	public void setupDevOrgTest() {
		description("Validates a WSDL-File can be taken from a URL using a REF-File.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/ref-file-wsdl-${apiNumber}");
		variable("apiName", "Ref-File-WSDL from URL-${apiNumber}");
		

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time from URL #######");
		createVariable("wsdlURL", "/com/axway/apim/test/files/basic/wsdl-file-with-username.url");
		createVariable("configFile", "/com/axway/apim/test/files/basic/wsdl-minimal-config.json");
		createVariable("status", "unpublished");
		createVariable("expectedReturnCode", "0");
		action(wsdlImport);
		
		echo("####### Validate API: '${apiName}' on path: '${apiPath}' has been imported #######");
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
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### Re-Import API from URL without a change #######");
		createVariable("wsdlURL", "/com/axway/apim/test/files/basic/wsdl-file-with-username.url");
		createVariable("configFile", "/com/axway/apim/test/files/basic/wsdl-minimal-config.json");
		createVariable("status", "unpublished");
		createVariable("expectedReturnCode", "10");
		action(wsdlImport);
	}

}
