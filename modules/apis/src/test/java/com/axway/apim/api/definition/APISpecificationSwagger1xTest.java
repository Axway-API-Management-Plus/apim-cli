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

public class APISpecificationSwagger1xTest {
	
	private static final String testPackage = "/com/axway/apim/api/definition";
	
	ObjectMapper mapper = new ObjectMapper();
	
	@BeforeClass
	private void initTestIndicator() {
		APIImportParams params = new APIImportParams();
		params.setReplaceHostInSwagger(true);
	}
	
	@Test
	public void standardPort() throws AppException, IOException {
		byte[] content = getSwaggerContent(testPackage + "/swagger12.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
		apiDefinition.configureBasepath("https://petstore.swagger.io", null);
		
		Assert.assertTrue(apiDefinition instanceof Swagger1xSpecification, "Specification must be an Swagger12Specification");
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("basePath").asText(), "https://petstore.swagger.io");
	}
	
	@Test
	public void specificPort() throws AppException, IOException {
		byte[] content = getSwaggerContent(testPackage + "/swagger12.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
		apiDefinition.configureBasepath("https://petstore.swagger.io:8180", null);
		
		Assert.assertTrue(apiDefinition instanceof Swagger1xSpecification, "Specification must be an Swagger12Specification");
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("basePath").asText(), "https://petstore.swagger.io:8180");
	}
	
	@Test
	public void standardPortGiven() throws AppException, IOException {
		byte[] content = getSwaggerContent(testPackage + "/swagger12.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
		apiDefinition.configureBasepath("https://petstore.swagger.io:443", null);
		
		Assert.assertTrue(apiDefinition instanceof Swagger1xSpecification, "Specification must be an Swagger12Specification");
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("basePath").asText(), "https://petstore.swagger.io:443");
	}
	
	@Test
	public void includingBasePath() throws AppException, IOException {
		byte[] content = getSwaggerContent(testPackage + "/swagger12.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
		apiDefinition.configureBasepath("https://petstore.swagger.io:443/myapi", null);
		
		Assert.assertTrue(apiDefinition instanceof Swagger1xSpecification, "Specification must be an Swagger12Specification");
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("basePath").asText(), "https://petstore.swagger.io:443/myapi/");
	}
	
	@Test
	public void testSwagger11Specification() throws AppException, IOException {
		byte[] content = getSwaggerContent(testPackage + "/swagger11.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
		apiDefinition.configureBasepath("https://petstore.swagger.io:443/myapi/", null);
		
		Assert.assertTrue(apiDefinition instanceof Swagger1xSpecification, "Specification must be an Swagger12Specification");
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("basePath").asText(), "https://petstore.swagger.io:443/myapi/");
	}
	
	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "The configured backendBasepath: 'An-Invalid-URL' is invalid.")
	public void testInvalidBackendBasepath() throws AppException, IOException {

		byte[] content = getSwaggerContent(testPackage + "/swagger11.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
		apiDefinition.configureBasepath("An-Invalid-URL", null);
	}
	
	
	private byte[] getSwaggerContent(String swaggerFile) throws AppException {
		try {
			return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}
}
