package com.axway.apim.test.basic;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static Logger LOG = LoggerFactory.getLogger(WSDLFromURLDirectTestIT.class);
	
	@Autowired
	private WSDLImportTestAction wsdlImport;
	
	@CitrusTest(name = "WSDLFromURLDirectTestIT")
	public void setupDevOrgTest() {
		description("Validates a WSDL-File can be taken from a URL using the direct instruction.");
		
		LOG.info("Default Charset=" + Charset.defaultCharset());
		LOG.info("file.encoding=" + System.getProperty("file.encoding"));
		LOG.info("Default Charset=" + Charset.defaultCharset());
		LOG.info("Default Charset in Use=" + getDefaultCharSet());
    		
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
		
		echo("####### Setting the status to Published #######");
		createVariable("wsdlURL", "http://www.dneonline.com/calculator.asmx?wsdl");
		createVariable("configFile", "/com/axway/apim/test/files/basic/wsdl-minimal-config.json");
		createVariable("status", "published");
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
			.validate("$.[?(@.id=='${apiId}')].name", "${apiName}")
			.validate("$.[?(@.id=='${apiId}')].state", "published");
		
		echo("####### Now performing a change, which required to Re-Create the API #######");
		createVariable("wsdlURL", "http://www.dneonline.com/calculator.asmx?wsdl");
		createVariable("configFile", "/com/axway/apim/test/files/basic/wsdl-minimal-config-with-tags.json");
		createVariable("status", "published");
		createVariable("expectedReturnCode", "0");
		action(wsdlImport);

	}

	private  String getDefaultCharSet() {
		OutputStreamWriter writer = new OutputStreamWriter(new ByteArrayOutputStream());
		String enc = writer.getEncoding();
		return enc;
	}

}
