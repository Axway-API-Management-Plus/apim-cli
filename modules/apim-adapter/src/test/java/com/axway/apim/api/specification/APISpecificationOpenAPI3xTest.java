package com.axway.apim.api.specification;

import com.axway.apim.api.model.APISpecificationFilter;
import com.axway.apim.api.model.DesiredAPISpecification;
import com.axway.apim.api.specification.APISpecification.APISpecType;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Objects;

public class APISpecificationOpenAPI3xTest {

	private static final String TEST_PACKAGE = "/com/axway/apim/adapter/spec";

	ObjectMapper mapper = new ObjectMapper();
	ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory());

	@Test
	public void keepOneUrlInOpenApiServers() throws IOException {

		byte[] content = getSwaggerContent(TEST_PACKAGE + "/petstore-openapi30.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "TestAPI");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767", null);
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof OAS3xSpecification);
		Assert.assertEquals(apiDefinition.getDescription(), "This is a sample server Petstore server.  You can find out more about Swagger at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, you can use the api key `special-key` to test the authorization filters.");
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("servers").size(), 1, "Expected to get only one server url");
	}

	@Test
	public void replaceServerURLIfHostNameIsNotPresent() throws IOException {
        CoreParameters coreParameters = CoreParameters.getInstance();
        coreParameters.setOverrideSpecBasePath(false);
		byte[] content = getSwaggerContent(TEST_PACKAGE + "/openapi.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "TestAPI");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767", null);
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof OAS3xSpecification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("servers").size(), 1, "Expected to get only one server url");
		Assert.assertEquals("https://myhost.customer.com:8767/api/v3", swagger.get("servers").get(0).get("url").asText());
	}

	@Test
	public void replaceServerURLIfHostNameIsPresent() throws IOException {
		CoreParameters coreParameters = CoreParameters.getInstance();
		coreParameters.setOverrideSpecBasePath(false);
		byte[] content = getSwaggerContent(TEST_PACKAGE + "/openapi-with-host.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "TestAPI");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767", null);
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof OAS3xSpecification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("servers").size(), 1, "Expected to get only one server url");
		Assert.assertEquals("https://myhost/api/v3", swagger.get("servers").get(0).get("url").asText());
	}

	@Test
	public void replaceServerURLIfHostNameIsPresent2() throws IOException {
		CoreParameters coreParameters = CoreParameters.getInstance();
		coreParameters.setOverrideSpecBasePath(true);
		byte[] content = getSwaggerContent(TEST_PACKAGE + "/openapi-with-host.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "TestAPI");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767/api/v3", null);
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof OAS3xSpecification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("servers").size(), 1, "Expected to get only one server url");
		Assert.assertEquals("https://myhost.customer.com:8767/api/v3", swagger.get("servers").get(0).get("url").asText());
	}

	@Test
	public void replaceServerURLIfHostNameIsNotPresentWithBackendBasePath() throws IOException {
        CoreParameters coreParameters = CoreParameters.getInstance();
        coreParameters.setOverrideSpecBasePath(false);
		byte[] content = getSwaggerContent(TEST_PACKAGE + "/openapi.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "TestAPI");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767/test", null);
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof OAS3xSpecification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("servers").size(), 1, "Expected to get only one server url");
		Assert.assertEquals("https://myhost.customer.com:8767/api/v3", swagger.get("servers").get(0).get("url").asText());
	}

	@Test
	public void testBerlinGroupYAM_API() throws IOException {
		byte[] content = getSwaggerContent(TEST_PACKAGE + "/psd2-api_1.3.6_errata20200327.yaml");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "TestAPI");
		Assert.assertTrue(apiDefinition instanceof OAS3xSpecification);
		apiDefinition.configureBasePath("https://myhost.customer.com:8767", null);
		// Check if the Swagger-File has been changed
		JsonNode swagger = ymlMapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals( swagger.get("servers").size(), 1, "Expected to get only one server url");
	}


	@Test
	public void testPetstoreSomeIncluded() throws IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		filterConfig.addInclude(new String[] {"/pet/{petId}:GET"}, null); // Only this single method should be included

		desiredAPISpec.setResource(TEST_PACKAGE+"/petstore-openapi30.json");
		desiredAPISpec.setFilter(filterConfig);

		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "Not required", "Test-API");

		Assert.assertSame(apiDefinition.getAPIDefinitionType(), APISpecType.OPEN_API_30);
		JsonNode filteredSpec = mapper.readTree(apiDefinition.getApiSpecificationContent());

		Assert.assertEquals(filteredSpec.get("paths").size(), 1, "Only one remaining method should be left.");
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/{petId}").get("get"), "/pet/{petId}:GET is the remaining method.");
	}

	@Test
	public void testPetstoreAllGetsIncluded() throws IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		filterConfig.addInclude(new String[] {"*:GET"}, null);

		desiredAPISpec.setResource(TEST_PACKAGE+"/petstore-openapi30.json");
		desiredAPISpec.setFilter(filterConfig);

		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "Not required", "Test-API");

		Assert.assertSame(apiDefinition.getAPIDefinitionType(), APISpecType.OPEN_API_30);
		JsonNode filteredSpec = mapper.readTree(apiDefinition.getApiSpecificationContent());

		Assert.assertEquals(filteredSpec.get("paths").size(), 8, "/ GET Methods are expected");
		Assert.assertNotNull(filteredSpec.get("paths").get("/pet/{petId}").get("get"), "/pet/{petId}:GET is one of the remaining methods.");
	}

	@Test
	public void testPetstoreSomeExcluded() throws IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		filterConfig.addExclude(new String[] {"/pet/{petId}:DELETE"}, null); // No DELETE-Method anymore for this path
		filterConfig.addExclude(new String[] {"/pet/{petId}/uploadImage:POST"}, null); // Last operation - Path should have been removed
		filterConfig.addExclude(new String[] {"/store/order/{orderId}:*"}, null); // Remove all operations for this path

		desiredAPISpec.setResource(TEST_PACKAGE+"/petstore-openapi30.json");
		desiredAPISpec.setFilter(filterConfig);

		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "Not required", "Test-API");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767/api/v1/myAPI", null);


		Assert.assertSame(apiDefinition.getAPIDefinitionType(), APISpecType.OPEN_API_30);
		JsonNode filteredSpec = mapper.readTree(apiDefinition.getApiSpecificationContent());

		Assert.assertNull(filteredSpec.get("paths").get("/pet/{petId}").get("delete"), "/pet/{petId}:DELETE should have been removed.");
		Assert.assertNull(filteredSpec.get("paths").get("/pet/{petId}/uploadImage"), "Entire path /pet/{petId}/uploadImage should have been removed.");
		Assert.assertNull(filteredSpec.get("paths").get("/store/order/{orderId}"), "Entire path /store/order/{orderId}:* should have been removed.");
	}

	@Test
	public void testPetstoreAllDeletesExcluded() throws IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		filterConfig.addExclude(new String[] {"*:DELETE"}, null);

		desiredAPISpec.setResource(TEST_PACKAGE+"/petstore-openapi30.json");
		desiredAPISpec.setFilter(filterConfig);

		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "Not required", "Test-API");

		Assert.assertSame(apiDefinition.getAPIDefinitionType(), APISpecType.OPEN_API_30);
		JsonNode filteredSpec = mapper.readTree(apiDefinition.getApiSpecificationContent());

		Assert.assertEquals(filteredSpec.get("paths").size(), 14, "All DELETE Methods are excluded");
		Assert.assertNull(filteredSpec.get("paths").get("/pet/{petId}").get("delete"), "Delete operation for /pet/{petId} should have been removed.");
		Assert.assertNull(filteredSpec.get("paths").get("/user/{username}").get("delete"), "Delete operation for /user/{username} should have been removed.");
		Assert.assertNull(filteredSpec.get("paths").get("/store/order/{orderId}").get("delete"), "Delete operation for /store/order/{orderId} should have been removed.");
	}

	@Test
	public void testTagPetIncludedOnly() throws IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		filterConfig.addInclude(null, new String[] {"pet"});
		filterConfig.addInclude(null, new String[] {"store"}); // Must be removed, as it is also excluded, which overrules the include
		filterConfig.addExclude(null, new String[] {"store"});
		filterConfig.addExclude(new String[] { "/pet/{petId}/uploadImage:POST" }, null);

		desiredAPISpec.setResource(TEST_PACKAGE+"/petstore-openapi30.json");
		desiredAPISpec.setFilter(filterConfig);

		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "Not required", "Test-API");

		Assert.assertSame(apiDefinition.getAPIDefinitionType(), APISpecType.OPEN_API_30);
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
	public void testCombinedFilter() throws IOException {
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

		Assert.assertSame(apiDefinition.getAPIDefinitionType(), APISpecType.OPEN_API_30);
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
			return IOUtils.toByteArray(Objects.requireNonNull(this.getClass().getResourceAsStream(swaggerFile)));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}

    @Test
    public void overrideServerURLWithBackendBasePath() throws IOException {
        CoreParameters coreParameters = CoreParameters.getInstance();
        coreParameters.setOverrideSpecBasePath(true);
        byte[] content = getSwaggerContent(TEST_PACKAGE + "/openapi.json");
        APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "TestAPI");
        apiDefinition.configureBasePath("https://myhost.customer.com:8767/test", null);
        // Check if the Swagger-File has been changed
        Assert.assertTrue(apiDefinition instanceof OAS3xSpecification);
        JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
        Assert.assertEquals(swagger.get("servers").size(), 1, "Expected to get only one server url");
        Assert.assertEquals("https://myhost.customer.com:8767/test", swagger.get("servers").get(0).get("url").asText());
    }

	@Test
	public void overrideServerURLWithBackendBasePath2() throws IOException {
		CoreParameters coreParameters = CoreParameters.getInstance();
		coreParameters.setOverrideSpecBasePath(true);
		byte[] content = getSwaggerContent(TEST_PACKAGE + "/openapi.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "TestAPI");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767/api/v3", null);
		// Check if the Swagger-File has been changed
		Assert.assertTrue(apiDefinition instanceof OAS3xSpecification);
		JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
		Assert.assertEquals(swagger.get("servers").size(), 1, "Expected to get only one server url");
		Assert.assertEquals("https://myhost.customer.com:8767/api/v3", swagger.get("servers").get(0).get("url").asText());
	}
}
