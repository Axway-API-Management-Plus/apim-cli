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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class APISpecificationOpenAPI3xTest {

	private static final String TEST_PACKAGE = "/com/axway/apim/adapter/spec";

	ObjectMapper mapper = new ObjectMapper();
	ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory());



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

	@Test
	public void resolveExternalReferencesOpenAPI31Yaml() throws IOException, AppException {
		// Issue #582: OpenAPI 3.1 specifications with boolean schemas (additionalProperties: false) in referenced files
		Path dir = Files.createTempDirectory("oas31-refs");
		Path pet = dir.resolve("Pet.yaml");
		Files.write(pet, ("type: object\n"
			+ "additionalProperties: false\n"
			+ "properties:\n"
			+ "  name:\n"
			+ "    type: string\n").getBytes(StandardCharsets.UTF_8));
		String spec = "openapi: \"3.1.0\"\n"
			+ "info:\n"
			+ "  title: Pets\n"
			+ "  version: \"1.0\"\n"
			+ "servers:\n"
			+ "  - url: https://petstore.example.com/v1\n"
			+ "paths:\n"
			+ "  /pets:\n"
			+ "    get:\n"
			+ "      operationId: getPets\n"
			+ "      responses:\n"
			+ "        '200':\n"
			+ "          description: OK\n"
			+ "          content:\n"
			+ "            application/json:\n"
			+ "              schema:\n"
			+ "                $ref: '" + pet.toAbsolutePath() + "'\n";
		OAS3xSpecification specification = new OAS3xSpecification();
		specification.setResolveOpenApiRef(true);
		specification.setApiSpecificationFile("pets.yaml");
		Assert.assertTrue(specification.parse(spec.getBytes(StandardCharsets.UTF_8)));

		byte[] content = specification.getApiSpecificationContent();
		String resolved = new String(content, StandardCharsets.UTF_8);
		Assert.assertFalse(resolved.contains("Pet.yaml"), "The reference to the file must be resolved. Content: " + resolved);
		Assert.assertFalse(resolved.contains("booleanSchemaValue"), resolved);
		Assert.assertFalse(resolved.contains("types:"), "Model specific properties must not be serialized: " + resolved);
		// It must still be YAML and an OpenAPI 3.1 specification
		Assert.assertTrue(specification.getAPIDefinitionType() == APISpecType.OPEN_API_30_YAML);
		JsonNode openAPI = ymlMapper.readTree(content);
		Assert.assertEquals(openAPI.get("openapi").asText(), "3.1.0");
		JsonNode schema = findPetSchema(openAPI);
		Assert.assertTrue(schema.get("additionalProperties").isBoolean(), "additionalProperties must be a boolean. Schema: " + schema);
		Assert.assertFalse(schema.get("additionalProperties").asBoolean());
		Assert.assertEquals(schema.get("properties").get("name").get("type").asText(), "string");
	}

	@Test
	public void resolveExternalReferencesOpenAPI30Json() throws IOException, AppException {
		Path dir = Files.createTempDirectory("oas30-refs");
		Path pet = dir.resolve("Pet.json");
		Files.write(pet, "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}".getBytes(StandardCharsets.UTF_8));
		String spec = "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"Pets\",\"version\":\"1.0\"},\"servers\":[{\"url\":\"https://petstore.example.com/v1\"}],"
			+ "\"paths\":{\"/pets\":{\"get\":{\"operationId\":\"getPets\",\"responses\":{\"200\":{\"description\":\"OK\",\"content\":{\"application/json\":"
			+ "{\"schema\":{\"$ref\":\"" + pet.toAbsolutePath() + "\"}}}}}}}}}";
		OAS3xSpecification specification = new OAS3xSpecification();
		specification.setResolveOpenApiRef(true);
		specification.setApiSpecificationFile("pets.json");
		Assert.assertTrue(specification.parse(spec.getBytes(StandardCharsets.UTF_8)));

		byte[] content = specification.getApiSpecificationContent();
		Assert.assertFalse(new String(content, StandardCharsets.UTF_8).contains("Pet.json"), "The reference to the file must be resolved");
		Assert.assertTrue(specification.getAPIDefinitionType() == APISpecType.OPEN_API_30);
		JsonNode openAPI = mapper.readTree(content);
		Assert.assertEquals(openAPI.get("openapi").asText(), "3.0.1");
		Assert.assertEquals(findPetSchema(openAPI).get("properties").get("name").get("type").asText(), "string");
	}

	@Test
	public void referencesAreNotResolvedByDefault() throws IOException, AppException {
		String spec = "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"Pets\",\"version\":\"1.0\"},\"paths\":{}}";
		OAS3xSpecification specification = new OAS3xSpecification();
		specification.setResolveOpenApiRef(false);
		Assert.assertTrue(specification.parse(spec.getBytes(StandardCharsets.UTF_8)));
		Assert.assertEquals(specification.getApiSpecificationContent(), spec.getBytes(StandardCharsets.UTF_8), "The given specification must be untouched");
	}

	@Test
	public void noOpenAPISpecificationIsNotResolved() throws AppException {
		OAS3xSpecification specification = new OAS3xSpecification();
		specification.setResolveOpenApiRef(true);
		Assert.assertFalse(specification.parse("{\"swagger\":\"2.0\",\"info\":{\"title\":\"Pets\",\"version\":\"1.0\"},\"paths\":{}}".getBytes(StandardCharsets.UTF_8)));
	}

	private JsonNode findPetSchema(JsonNode openAPI) {
		// The referenced file is either inlined or moved to the components by the resolver
		JsonNode schema = openAPI.at("/paths/~1pets/get/responses/200/content/application~1json/schema");
		if (schema.has("$ref")) {
			String ref = schema.get("$ref").asText();
			Assert.assertTrue(ref.startsWith("#/"), "Only internal references are expected: " + ref);
			schema = openAPI.at(ref.substring(1));
		}
		Assert.assertFalse(schema.isMissingNode(), "Pet schema not found");
		return schema;
	}
}
