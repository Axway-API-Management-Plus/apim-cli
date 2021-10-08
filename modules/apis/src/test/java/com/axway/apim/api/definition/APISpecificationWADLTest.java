package com.axway.apim.api.definition;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class APISpecificationWADLTest {
	
	XmlMapper xmlMapper = new XmlMapper();
	
	@BeforeClass
	private void initTestIndicator() {
		APIImportParams params = new APIImportParams();
		params.setReplaceHostInSwagger(true);
	}
	
	@Test
	public void testSamplePaymentsWADLAPI() throws AppException, IOException {

		byte[] content = getAPISpecificationContent("/api_definition_1/sample-payment-api.wadl");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "sample-payment-api.wadl", "Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		
		Assert.assertTrue(apiDefinition instanceof WADLSpecification);
		// Check if the WADL-File has been changed based on the configured base path
		JsonNode wadl = xmlMapper.readTree(apiDefinition.getApiSpecificationContent());
		JsonNode resourcesNode = wadl.get("resources");
		String base = resourcesNode.get("base").asText();
		Assert.assertEquals(base, "https://myhost.customer.com:8767/api/v1/myAPI/");
	}
	
	@Test
	public void testSampleAccoutnsWADLAPI() throws AppException, IOException {

		byte[] content = getAPISpecificationContent("/api_definition_1/sample-accounts-api.wadl");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "sample-accounts-api.wadl", "Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		
		Assert.assertTrue(apiDefinition instanceof WADLSpecification);
		// Check if the WADL-File has been changed based on the configured base path
		JsonNode wadl = xmlMapper.readTree(apiDefinition.getApiSpecificationContent());
		JsonNode resourcesNode = wadl.get("resources");
		String base = resourcesNode.get("base").asText();
		Assert.assertEquals(base, "https://myhost.customer.com:8767/api/v1/myAPI/");
	}
	
	
	private byte[] getAPISpecificationContent(String swaggerFile) throws AppException {
		try {
			return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}
}
