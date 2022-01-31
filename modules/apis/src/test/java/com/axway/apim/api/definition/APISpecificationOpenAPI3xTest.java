package com.axway.apim.api.definition;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.apiSpecification.APISpecification;
import com.axway.apim.api.apiSpecification.APISpecificationFactory;
import com.axway.apim.api.apiSpecification.OAS3xSpecification;
import com.axway.apim.api.apiSpecification.APISpecification.APISpecType;
import com.axway.apim.api.model.APISpecificationFilter;
import com.axway.apim.api.model.DesiredAPISpecification;
import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class APISpecificationOpenAPI3xTest {
	
	private static final String TEST_PACKAGE = "/com/axway/apim/api/definition";
	
	ObjectMapper mapper = new ObjectMapper();
	ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory());
	
	@BeforeClass
	private void initTestIndicator() {
		APIImportParams params = new APIImportParams();
		params.setReplaceHostInSwagger(true);
	}
	
	@Test
	public void backendHostAndBasePath() throws AppException, IOException {

		byte[] content = getSwaggerContent(TEST_PACKAGE + "/petstore-openapi30.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "TestAPI");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI", null);
		
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof OAS3xSpecification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals( ((ArrayNode) swagger.get("servers")).size(), 1, "Expected to get only one server url");
		Assert.assertEquals( ((ArrayNode) swagger.get("servers")).get(0).get("url").asText(), "https://myhost.customer.com:8767/api/v1/myAPI/");
	}
	
	@Test
	public void testBerlinGroupYAM_API() throws AppException, IOException {
		APIManagerAdapter.apiManagerVersion="7.7.0";
		byte[] content = getSwaggerContent(TEST_PACKAGE + "/psd2-api_1.3.6_errata20200327.yaml");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "TestAPI");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI", null);
		
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof OAS3xSpecification);
		JsonNode swagger = ymlMapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals( ((ArrayNode) swagger.get("servers")).size(), 1, "Expected to get only one server url");
		Assert.assertEquals( ((ArrayNode) swagger.get("servers")).get(0).get("url").asText(), "https://myhost.customer.com:8767/api/v1/myAPI/");
	}
	
	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "The configured backendBasepath: 'An-Invalid-URL' is invalid.")
	public void testInvalidBackendBasepath() throws AppException, IOException {

		byte[] content = getSwaggerContent(TEST_PACKAGE + "/petstore-openapi30.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
		apiDefinition.configureBasepath("An-Invalid-URL", null);
	}
	
	@Test
	public void testPetstoreSomeIncluded() throws AppException, IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		filterConfig.getInclude().addPath("/pet/{petId}:GET"); // Only this single method should be included
		
		desiredAPISpec.setResource(TEST_PACKAGE+"/petstore-openapi30.json");
		desiredAPISpec.setFilter(filterConfig);
		
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "Not required", "Test-API");
		
		Assert.assertTrue(apiDefinition.getAPIDefinitionType() == APISpecType.OPEN_API_30);
		JsonNode filteredSpec = mapper.readTree(apiDefinition.getApiSpecificationContent());
		
		Assert.assertEquals(filteredSpec.get("paths").size(), 1, "Only one remaining method should be left.");
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/{petId}").get("get"), "/pet/{petId}:GET is the remaining method.");
	}
	
	@Test
	public void testPetstoreAllGetsIncluded() throws AppException, IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		filterConfig.getInclude().addPath("*:GET");
		
		desiredAPISpec.setResource(TEST_PACKAGE+"/petstore-openapi30.json");
		desiredAPISpec.setFilter(filterConfig);
		
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "Not required", "Test-API");
		
		Assert.assertTrue(apiDefinition.getAPIDefinitionType() == APISpecType.OPEN_API_30);
		JsonNode filteredSpec = mapper.readTree(apiDefinition.getApiSpecificationContent());
		
		Assert.assertEquals(filteredSpec.get("paths").size(), 8, "/ GET Methods are expected");
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/{petId}").get("get"), "/pet/{petId}:GET is one of the remaining methods.");
	}
	
	@Test
	public void testPetstoreSomeExcluded() throws AppException, IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		filterConfig.getExclude().addPath("/pet/{petId}:DELETE"); // No DELETE-Method anymore for this path
		filterConfig.getExclude().addPath("/pet/{petId}/uploadImage:POST"); // Last operation - Path should have been removed
		filterConfig.getExclude().addPath("/store/order/{orderId}:*"); // Remove all operations for this path
		
		
		desiredAPISpec.setResource(TEST_PACKAGE+"/petstore-openapi30.json");
		desiredAPISpec.setFilter(filterConfig);
		
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "Not required", "Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI", null);
		
		
		Assert.assertTrue(apiDefinition.getAPIDefinitionType() == APISpecType.OPEN_API_30);
		JsonNode filteredSpec = mapper.readTree(apiDefinition.getApiSpecificationContent());
		
		Assert.assertNull(filteredSpec.get("paths").get("/pet/{petId}").get("delete"), "/pet/{petId}:DELETE should have been removed.");
		Assert.assertNull(filteredSpec.get("paths").get("/pet/{petId}/uploadImage"), "Entire path /pet/{petId}/uploadImage should have been removed.");
		Assert.assertNull(filteredSpec.get("paths").get("/store/order/{orderId}"), "Entire path /store/order/{orderId}:* should have been removed.");
	}
	
	@Test
	public void testPetstoreAllDeletesExcluded() throws AppException, IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		filterConfig.getExclude().addPath("*:DELETE");
		
		desiredAPISpec.setResource(TEST_PACKAGE+"/petstore-openapi30.json");
		desiredAPISpec.setFilter(filterConfig);
		
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "Not required", "Test-API");
		
		Assert.assertTrue(apiDefinition.getAPIDefinitionType() == APISpecType.OPEN_API_30);
		JsonNode filteredSpec = mapper.readTree(apiDefinition.getApiSpecificationContent());
		
		Assert.assertEquals(filteredSpec.get("paths").size(), 14, "All DELETE Methods are excluded");
		Assert.assertNull(filteredSpec.get("paths").get("/pet/{petId}").get("delete"), "Delete operation for /pet/{petId} should have been removed.");
		Assert.assertNull(filteredSpec.get("paths").get("/user/{username}").get("delete"), "Delete operation for /user/{username} should have been removed.");
		Assert.assertNull(filteredSpec.get("paths").get("/store/order/{orderId}").get("delete"), "Delete operation for /store/order/{orderId} should have been removed.");
	}
	
	
	private byte[] getSwaggerContent(String swaggerFile) throws AppException {
		try {
			return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}
}
