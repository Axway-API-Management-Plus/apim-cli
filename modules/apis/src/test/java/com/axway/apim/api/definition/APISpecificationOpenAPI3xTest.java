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
		filterConfig.addInclude(new String[] {"/pet/{petId}:GET"}, null); // Only this single method should be included
		
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
		filterConfig.addInclude(new String[] {"*:GET"}, null);
		
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
		filterConfig.addExclude(new String[] {"/pet/{petId}:DELETE"}, null); // No DELETE-Method anymore for this path
		filterConfig.addExclude(new String[] {"/pet/{petId}/uploadImage:POST"}, null); // Last operation - Path should have been removed
		filterConfig.addExclude(new String[] {"/store/order/{orderId}:*"}, null); // Remove all operations for this path
		
		
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
		filterConfig.addExclude(new String[] {"*:DELETE"}, null);
		
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
	
	@Test
	public void testTagPetIncludedOnly() throws AppException, IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		filterConfig.addInclude(null, new String[] {"pet"});
		filterConfig.addInclude(null, new String[] {"store"}); // Must be removed, as it is also excluded, which overrules the include
		filterConfig.addExclude(null, new String[] {"store"});
		filterConfig.addExclude(new String[] { "/pet/{petId}/uploadImage:POST" }, null);
		
		desiredAPISpec.setResource(TEST_PACKAGE+"/petstore-openapi30.json");
		desiredAPISpec.setFilter(filterConfig);
		
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "Not required", "Test-API");
		
		Assert.assertTrue(apiDefinition.getAPIDefinitionType() == APISpecType.OPEN_API_30);
		JsonNode filteredSpec = mapper.readTree(apiDefinition.getApiSpecificationContent());
		
		Assert.assertNull(filteredSpec.get("paths").get("/pet/{petId}/uploadImage"));
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet").get("post"));
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet").get("put"));
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/findByStatus").get("get"));
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/findByTags").get("get"));
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/{petId}").get("get"));
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/{petId}").get("post"));
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/{petId}").get("delete"));
		
		Assert.assertNull(filteredSpec.get("paths").get("/store/order"));
		Assert.assertNull(filteredSpec.get("paths").get("/store/order/{orderId}"));
		Assert.assertNull(filteredSpec.get("paths").get("/store/inventory"));
		
		Assert.assertNull(filteredSpec.get("paths").get("/user/createWithArray"));
		Assert.assertNull(filteredSpec.get("paths").get("/user/createWithList"));
		Assert.assertNull(filteredSpec.get("paths").get("/user/{username}"));
		Assert.assertNull(filteredSpec.get("paths").get("/user/login"));
		Assert.assertNull(filteredSpec.get("paths").get("/user/logout"));
		Assert.assertNull(filteredSpec.get("paths").get("/user"));
	}
	
	@Test
	public void testCombinedFilter() throws AppException, IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		// Include all GET-Methods from tags pet and store
		filterConfig.addInclude(new String[] {"*:GET", "/pet/{petId}/uploadImage:POST"}, new String[] {"pet"}, new String[] {"Category"});
		filterConfig.addInclude(new String[] {"*:DELETE", "*:PUT", "/store/order:POST"}, new String[] {"store"}, new String[] {"Pet", "Order"});
		// But in general POST methods should be removed
		filterConfig.addExclude(new String[] {"*:POST"}, null, new String[] {"Order"});
		
		desiredAPISpec.setResource(TEST_PACKAGE+"/petstore-openapi30.json");
		desiredAPISpec.setFilter(filterConfig);
		
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "Not required", "Test-API");
		
		Assert.assertTrue(apiDefinition.getAPIDefinitionType() == APISpecType.OPEN_API_30);
		JsonNode filteredSpec = mapper.readTree(apiDefinition.getApiSpecificationContent());
		
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/{petId}/uploadImage"));
		Assert.assertNull(filteredSpec.get("paths").get("/pet"), "Entire path /get must be removed, as it contains no GET method and POST and PUT should only be included for store");
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/findByStatus").get("get"));
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/findByTags").get("get"));
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/{petId}").get("get"));
		Assert.assertNull(filteredSpec.get("paths").get("/pet/{petId}").get("post"));
		Assert.assertNull(filteredSpec.get("paths").get("/pet/{petId}").get("delete"));
		
		Assert.assertNotNull(filteredSpec.get("paths").get("/store/order").get("post"));
		Assert.assertNotNull(filteredSpec.get("paths").get("/store/order/{orderId}").get("delete"));
		Assert.assertNull(filteredSpec.get("paths").get("/store/inventory"));
		
		Assert.assertNull(filteredSpec.get("paths").get("/user/createWithArray"));
		Assert.assertNull(filteredSpec.get("paths").get("/user/createWithList"));
		Assert.assertNull(filteredSpec.get("paths").get("/user/{username}"));
		Assert.assertNull(filteredSpec.get("paths").get("/user/login"));
		Assert.assertNull(filteredSpec.get("paths").get("/user/logout"));
		Assert.assertNull(filteredSpec.get("paths").get("/user"));
		
		Assert.assertNotNull(filteredSpec.get("components").get("schemas").get("Category"));
		Assert.assertNotNull(filteredSpec.get("components").get("schemas").get("Pet"));
		Assert.assertNull(filteredSpec.get("components").get("schemas").get("Order"));
		Assert.assertNull(filteredSpec.get("components").get("schemas").get("User"));
		Assert.assertNull(filteredSpec.get("components").get("schemas").get("ApiResponse"));
	}
	
	
	private byte[] getSwaggerContent(String swaggerFile) throws AppException {
		try {
			return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}
}
