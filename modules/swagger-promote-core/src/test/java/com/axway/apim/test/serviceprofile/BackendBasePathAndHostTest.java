package com.axway.apim.test.serviceprofile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.api.definition.APISpecification;
import com.axway.apim.api.definition.APISpecificationFactory;
import com.axway.apim.api.model.APIDefintion;
import com.axway.apim.apiimport.DesiredAPI;
import com.axway.apim.lib.CommandParameters;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BackendBasePathAndHostTest {
	
	@BeforeClass
	private void initTestIndicator() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("replaceHostInSwagger", "true");
		new CommandParameters(params);
	}
	
	@Test
	public void backendHostAndBasePath() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		byte[] content = getSwaggerContent("/api_definition_1/petstore.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "https://myhost.customer.com:8767/api/v1/myAPI");
		testAPI.setAPIDefinition(apiDefinition);
		
		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper();
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getApiSpecificationContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com:8767");
		Assert.assertEquals(swagger.get("basePath").asText(), "/api/v1/myAPI");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	@Test
	public void backendHostOnly() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		byte[] content = getSwaggerContent("/api_definition_1/petstore.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "http://myhost.customer.com:8767");
		testAPI.setAPIDefinition(apiDefinition);

		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper();
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getApiSpecificationContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com:8767");
		Assert.assertEquals(swagger.get("basePath").asText(), "/v2");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "http");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	@Test
	public void backendHostBasisBasePath() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		byte[] content = getSwaggerContent("/api_definition_1/petstore.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "https://myhost.customer.com/");
		testAPI.setAPIDefinition(apiDefinition);

		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper();
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getApiSpecificationContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com");
		Assert.assertEquals(swagger.get("basePath").asText(), "/");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	@Test
	public void swaggerWithoutSchemes() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		byte[] content = getSwaggerContent("/api_definition_1/petstore-without-schemes.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "https://myhost.customer.com/");
		testAPI.setAPIDefinition(apiDefinition);

		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper();
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getApiSpecificationContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com");
		Assert.assertEquals(swagger.get("basePath").asText(), "/");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	@Test
	public void backendBasepathChangesNothing() throws AppException, IOException {
		DesiredAPI testAPI = new DesiredAPI();
		byte[] content = getSwaggerContent("/api_definition_1/petstore-only-https-scheme.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "https://petstore.swagger.io");
		testAPI.setAPIDefinition(apiDefinition);

		// Check the Service-Profile
		Assert.assertNull(testAPI.getServiceProfiles(), "ServiceProfiles should be null, as we have already changed host and basePath in the Swagger-File");
		
		// Check if the Swagger-File has been changed
		ObjectMapper mapper = new ObjectMapper();
		JsonNode swagger = mapper.readTree(testAPI.getAPIDefinition().getApiSpecificationContent());
		Assert.assertEquals(swagger.get("host").asText(), "petstore.swagger.io");
		Assert.assertEquals(swagger.get("basePath").asText(), "/v2");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	
	
	private byte[] getSwaggerContent(String swaggerFile) throws AppException {
		try {
			return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}
}
