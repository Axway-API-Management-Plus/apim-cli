package com.axway.apim.api.definition;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APISpecificationWSDLTest {
	
	private static final String testPackage = "/com/axway/apim/api/definition";
	
	ObjectMapper mapper = new ObjectMapper();
	
	@BeforeClass
	private void initTestIndicator() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("replaceHostInSwagger", "true");
		new CommandParameters(params);
	}
	
	@Test
	public void isWSDLSpecification() throws AppException, IOException {

		byte[] content = getSwaggerContent(testPackage + "/sample-wsdl.xml");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "http://www.mnb.hu/arfolyamok.asmx?WSDL", "https://myhost.customer.com:8767/api/v1/myAPI");
		
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof WSDLSpecification);
	}
	
	
	private byte[] getSwaggerContent(String swaggerFile) throws AppException {
		try {
			return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}
}
