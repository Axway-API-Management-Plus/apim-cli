package com.axway.apim.api.definition;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.api.definition.APISpecification;
import com.axway.apim.api.definition.APISpecificationFactory;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APISpecificationSwagger12Test {
	
	private static final String testPackage = "/com/axway/apim/api/definition";
	
	ObjectMapper mapper = new ObjectMapper();
	
	@BeforeClass
	private void initTestIndicator() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("replaceHostInSwagger", "true");
		new CommandParameters(params);
	}
	
	@Test
	public void standardPort() throws AppException, IOException {
		byte[] content = getSwaggerContent(testPackage + "/swagger12.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "https://petstore.swagger.io");
		
		Assert.assertTrue(apiDefinition instanceof Swagger12Specification, "Specification must be an Swagger12Specification");
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("basePath").asText(), "https://petstore.swagger.io");
	}
	
	@Test
	public void specificPort() throws AppException, IOException {
		byte[] content = getSwaggerContent(testPackage + "/swagger12.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "https://petstore.swagger.io:8180");
		
		Assert.assertTrue(apiDefinition instanceof Swagger12Specification, "Specification must be an Swagger12Specification");
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("basePath").asText(), "https://petstore.swagger.io:8180");
	}
	
	@Test
	public void standardPortGiven() throws AppException, IOException {
		byte[] content = getSwaggerContent(testPackage + "/swagger12.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "https://petstore.swagger.io:443");
		
		Assert.assertTrue(apiDefinition instanceof Swagger12Specification, "Specification must be an Swagger12Specification");
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("basePath").asText(), "https://petstore.swagger.io:443");
	}
	
	@Test
	public void includingBasePath() throws AppException, IOException {
		byte[] content = getSwaggerContent(testPackage + "/swagger12.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "https://petstore.swagger.io:443/myapi");
		
		Assert.assertTrue(apiDefinition instanceof Swagger12Specification, "Specification must be an Swagger12Specification");
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("basePath").asText(), "https://petstore.swagger.io:443/myapi");
	}
	
	
	private byte[] getSwaggerContent(String swaggerFile) throws AppException {
		try {
			return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}
}
