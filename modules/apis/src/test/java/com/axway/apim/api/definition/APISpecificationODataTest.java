package com.axway.apim.api.definition;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

public class APISpecificationODataTest {
	
	ObjectMapper mapper = new ObjectMapper();
	
	@BeforeClass
	private void initTestIndicator() {
		APIImportParams params = new APIImportParams();
		params.setReplaceHostInSwagger(true);
	}
	
	@Test
	public void testODataV2API() throws AppException, IOException {
		
		byte[] content = getAPISpecificationContent("/api_definition_1/ODataV2NorthWindMetadata.xml");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "northwind-odata-v2.xml$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		OpenAPI openAPI = mapper.readValue(apiDefinition.getApiSpecificationContent(), OpenAPI.class);
		
		// OpenAPI has been created - Perform some assertions
		Assert.assertEquals(openAPI.getServers().size(), 1); // One Server is expected
		Assert.assertEquals(openAPI.getServers().get(0).getUrl(), "https://myhost.customer.com:8767/api/v1/myAPI/"); // Has our configured base path
		Assert.assertEquals(openAPI.getPaths().size(), 26); // For each entity a path should have being created
		
		PathItem categoryPaths = openAPI.getPaths().get("/Categories({CategoryID})*");
		Assert.assertNotNull(categoryPaths, "Expected a path for /Categories({CategoryID})*");
		
		// Check the GET Operation for this entity 
		Operation categoryGet = categoryPaths.getGet();
		Assert.assertNotNull(categoryGet, "Expected a GET operation for /Categories({CategoryID})*");
		Assert.assertNull(categoryGet.getRequestBody()); // No body is expected for a GET Method
		Assert.assertEquals(categoryGet.getParameters().size(), 8); // For each entity a path should have being created
		
		// Check the POST Operation for this entity
		Operation categoryPost = categoryPaths.getPost();
		Assert.assertNotNull(categoryPost.getRequestBody(), "Body for a POST method is expected"); // A body is expected for a POST Method
		Assert.assertNotNull(categoryPost.getRequestBody().getContent(), "Body content for a POST method is expected"); // A body content is expected for a POST Method
		Assert.assertNotNull(categoryPost.getRequestBody().getContent().get("application/json"), "Expected content type should be JSON");
	}
	
	@Test
	public void testODataV2APIBackendFromMetadata() throws AppException, IOException {
		
		byte[] content = getAPISpecificationContent("/api_definition_1/ODataV2NorthWindMetadata.xml");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "https://services.odata.org/V2/Northwind/Northwind.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasepath(null);
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		OpenAPI openAPI = mapper.readValue(apiDefinition.getApiSpecificationContent(), OpenAPI.class);
		
		// OpenAPI has been created - Perform some assertions
		Assert.assertNotNull(openAPI.getServers(), "Expected OpenAPI servers set based on given MetaData URL");
		Assert.assertEquals(openAPI.getServers().size(), 1); // One Server is expected
		Assert.assertEquals(openAPI.getServers().get(0).getUrl(), "https://services.odata.org/V2/Northwind/Northwind.svc"); // Has our configured base path
	}
	
	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "Detected OData V4 specification, which is not yet supported by the APIM-CLI..*")
	public void testODataV4API() throws AppException, IOException {
		
		byte[] content = getAPISpecificationContent("/api_definition_1/ODataV4TrippinServiceMetadata.xml");
		APISpecificationFactory.getAPISpecification(content, "https://services.odata.org/TripPinRESTierService/(S(5kkh4dfw32ks0ph51razkxao))/$metadata", "OData-V4-Test-API");
	}
	
	
	private byte[] getAPISpecificationContent(String swaggerFile) throws AppException {
		try {
			return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}
}
