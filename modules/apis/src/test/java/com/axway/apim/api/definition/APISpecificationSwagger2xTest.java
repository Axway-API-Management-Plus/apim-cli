package com.axway.apim.api.definition;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.api.apiSpecification.APISpecification;
import com.axway.apim.api.apiSpecification.APISpecificationFactory;
import com.axway.apim.api.apiSpecification.Swagger2xSpecification;
import com.axway.apim.api.apiSpecification.APISpecification.APISpecType;
import com.axway.apim.api.model.APISpecificationFilter;
import com.axway.apim.api.model.DesiredAPISpecification;
import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APISpecificationSwagger2xTest {
	
	ObjectMapper mapper = new ObjectMapper();
	
	@BeforeClass
	private void initTestIndicator() {
		APIImportParams params = new APIImportParams();
		params.setReplaceHostInSwagger(true);
	}
	
	@Test
	public void testAirportsAPI() throws AppException, IOException {

		byte[] content = getSwaggerContent("/api_definition_1/airports_swagger_20.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "airports_swagger_20.json", "Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI", null);
		
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof Swagger2xSpecification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com:8767");
		Assert.assertEquals(swagger.get("basePath").asText(), "/api/v1/myAPI/");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	@Test
	public void backendHostAndBasePath() throws AppException, IOException {

		byte[] content = getSwaggerContent("/api_definition_1/petstore.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI", null);
		
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof Swagger2xSpecification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com:8767");
		Assert.assertEquals(swagger.get("basePath").asText(), "/api/v1/myAPI/"); 
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	@Test
	public void backendHostOnly() throws AppException, IOException {
		byte[] content = getSwaggerContent("/api_definition_1/petstore.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
		apiDefinition.configureBasepath("http://myhost.customer.com:8767", null);

		Assert.assertTrue(apiDefinition instanceof Swagger2xSpecification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com:8767");
		Assert.assertEquals(swagger.get("basePath").asText(), "/v2");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "http");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	@Test
	public void backendHostBasisBasePath() throws AppException, IOException {
		byte[] content = getSwaggerContent("/api_definition_1/petstore.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com/", null);
		
		Assert.assertTrue(apiDefinition instanceof Swagger2xSpecification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com");
		Assert.assertEquals(swagger.get("basePath").asText(), "/");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	@Test
	public void swaggerWithoutSchemes() throws AppException, IOException {
		byte[] content = getSwaggerContent("/api_definition_1/petstore-without-schemes.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com/", null);
		
		Assert.assertTrue(apiDefinition instanceof Swagger2xSpecification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("host").asText(), "myhost.customer.com");
		Assert.assertEquals(swagger.get("basePath").asText(), "/");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	@Test
	public void backendBasepathChangesNothing() throws AppException, IOException {
		byte[] content = getSwaggerContent("/api_definition_1/petstore-only-https-scheme.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
		apiDefinition.configureBasepath("https://petstore.swagger.io", null);
		
		Assert.assertTrue(apiDefinition instanceof Swagger2xSpecification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("host").asText(), "petstore.swagger.io");
		Assert.assertEquals(swagger.get("basePath").asText(), "/v2");
		Assert.assertEquals(swagger.get("schemes").get(0).asText(), "https");
		Assert.assertEquals(swagger.get("schemes").size(), 1);
	}
	
	@Test
	public void testWithoutBackendBasepath() throws AppException, IOException {

		byte[] content = getSwaggerContent("/api_definition_1/petstore.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
		
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof Swagger2xSpecification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("host").asText(), "petstore.swagger.io");
		Assert.assertEquals(swagger.get("basePath").asText(), "/v2");
		Assert.assertEquals(swagger.get("schemes").size(), 2);
	}
	
	@Test
	public void testPetstoreFiltered() throws AppException, IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		filterConfig.getPaths().addInclude("/pet/findByStatus:GET");
		filterConfig.getPaths().addInclude("*:DELETE");
		filterConfig.getPaths().addExclude("/user/{username}:DELETE");
		
		desiredAPISpec.setResource("/api_definition_1/petstore.json");
		desiredAPISpec.setFilter(filterConfig);
		
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "Not required", "Test-API");
		
		Assert.assertTrue(apiDefinition.getAPIDefinitionType() == APISpecType.SWAGGGER_API_20);
		JsonNode filteredSpec = mapper.readTree(apiDefinition.getApiSpecificationContent());
		
		Assert.assertEquals(filteredSpec.get("paths").size(), 3, "3 Methods expected");
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/findByStatus").get("get"), "/pet/findByStatus:GET expected");
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/{petId}").get("delete"), "/pet/{petId}:DELETE expected");
		Assert.assertNotNull(filteredSpec.get("paths").get("/store/order/{orderId}").get("delete"), "/store/order/{orderId}:DELETE expected");
		Assert.assertNull(filteredSpec.get("paths").get("/user/{username}"), "/user/{username}:DELETE NOT expected");
	}
	
	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "The configured backendBasepath: 'An-Invalid-URL' is invalid.")
	public void testInvalidBackendBasepath() throws AppException, IOException {

		byte[] content = getSwaggerContent("/api_definition_1/petstore.json");
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
