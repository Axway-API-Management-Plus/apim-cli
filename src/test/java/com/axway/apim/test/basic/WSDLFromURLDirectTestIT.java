package com.axway.apim.test.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.WSDLImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="WSDLFromURLDirectTestIT")
public class WSDLFromURLDirectTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private WSDLImportTestAction wsdlImport;
	
	@CitrusTest(name = "WSDLFromURLDirectTestIT")
	public void setupDevOrgTest() {
		description("Validates a WSDL-File can be taken from a URL using the direct instruction.");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/direct-url-wsdl-${apiNumber}");
		variable("apiName", "Direct-URL-WSDL from URL-${apiNumber}");
		

		
		echo("####### Importing API: '${apiName}' on path: '${apiPath}' for the first time from URL #######");
		createVariable("wsdlURL", "http://www.dneonline.com/calculator.asmx?wsdl");
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
		createVariable("wsdlURL", "http://www.dneonline.com/calculator.asmx?wsdl");
		createVariable("configFile", "/com/axway/apim/test/files/basic/wsdl-minimal-config.json");
		createVariable("status", "unpublished");
		createVariable("expectedReturnCode", "10");
		action(wsdlImport);

	}

}
