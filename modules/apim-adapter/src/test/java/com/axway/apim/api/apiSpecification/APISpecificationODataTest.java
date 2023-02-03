package com.axway.apim.api.apiSpecification;

import com.axway.apim.api.model.APISpecificationFilter;
import com.axway.apim.api.model.DesiredAPISpecification;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class APISpecificationODataTest {
	
	ObjectMapper mapper = new ObjectMapper();
	
	private static final String TEST_PACKAGE = "/com/axway/apim/adapter/spec/odata";


	
	@Test
	public void testODataV2API() throws IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/ODataV2NorthWindMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/ODataV2NorthWindOpenAPI3.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "northwind-odata-v2.xml$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767/api/v1/myAPI", null);
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		Assert.assertEquals(apiDefinition.getDescription(), "The OData Service from northwind-odata-v2.xml$metadata");
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testODataV2APIFilteredSomeExludes() throws IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		filterConfig.addExclude(new String[] {"*:DELETE"}, null); // Exclude all DELETE-Methods
		filterConfig.addExclude(new String[] {"*:POST"}, null); // Exclude all POST-Methods
		filterConfig.addExclude(new String[] {"/Suppliers*:*" }, null); // Exclude all HTTP-Verbs for /Suppliers*
		desiredAPISpec.setResource(TEST_PACKAGE+"/ODataV2NorthWindMetadata.xml");
		desiredAPISpec.setFilter(filterConfig);
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "northwind-odata-v2.xml$metadata", "OData-V2-Test-API");
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		JsonNode filteredSpec = mapper.readTree(apiDefinition.getApiSpecificationContent());
		
		for(JsonNode path : filteredSpec.get("paths")) {
			Assert.assertNull(path.get("delete"), "No delete method expected for path: " + path.get("delete"));
			Assert.assertNull(path.get("post"), "No post method expected for path: " + path.get("post"));
		}
		Assert.assertNull(filteredSpec.get("paths").get("/Suppliers*"), "/Suppliers* should have been removed");
	}
	
	@Test
	public void testODataV2APIFilteredSomeIncludes() throws IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		filterConfig.addInclude(new String[] {"*:GET"}, null); // Include all GET-Methods
		filterConfig.addInclude(new String[] {"*:PATCH"}, null); // Include all PUT-Methods
		desiredAPISpec.setResource(TEST_PACKAGE+"/ODataV2NorthWindMetadata.xml");
		desiredAPISpec.setFilter(filterConfig);
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "northwind-odata-v2.xml$metadata", "OData-V2-Test-API");
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		JsonNode filteredSpec = mapper.readTree(apiDefinition.getApiSpecificationContent());
		
		Assert.assertNotNull(filteredSpec.get("paths").get("/Categories*").get("get"), "Get method expected for path: /Categories*");
		Assert.assertNotNull(filteredSpec.get("paths").get("/Products*").get("get"), "Get method expected for path: /Products*");
		Assert.assertNotNull(filteredSpec.get("paths").get("/Products({Id})*").get("get"), "Get method expected for path: /Products({Id})*");
		Assert.assertNotNull(filteredSpec.get("paths").get("/Products({Id})*").get("patch"), "Patch method expected for path: /Products({Id})*");
		
		Assert.assertNull(filteredSpec.get("paths").get("/Products({Id})*").get("delete"), "Delete method NOT expected for path: /Products({Id})*");
		Assert.assertNull(filteredSpec.get("paths").get("/Products({Id})*").get("post"), "Post method NOT expected for path: /Products({Id})*");
	}
	
	@Test
	public void testODataV2APIFilteredWithTagsAndModels() throws IOException {
		DesiredAPISpecification desiredAPISpec = new DesiredAPISpecification();
		APISpecificationFilter filterConfig = new APISpecificationFilter();
		desiredAPISpec.setResource(TEST_PACKAGE+"/ODataV2NorthWindMetadata.xml");
		filterConfig.addInclude(null, new String[] {"Regions"});
		filterConfig.addExclude(new String[] {"/Regions({Id})*:DELETE"}, null); // Should be excluded, even the tag is included
		filterConfig.addInclude(null, new String[] {"Order_Details"});
		filterConfig.addInclude(null, new String[] {"Products"});
		filterConfig.addExclude(null, new String[] {"Products"}, new String[] {"Summary_of_Sales_by_Quarter"}); // Must override the products tag
		filterConfig.addInclude(null, null, new String[] {"Invoice", "Customer", "Summary_of_Sales_by_Quarter"});
		desiredAPISpec.setFilter(filterConfig);
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(desiredAPISpec, "northwind-odata-v2.xml$metadata", "OData-V2-Test-API");
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		JsonNode filteredSpec = mapper.readTree(apiDefinition.getApiSpecificationContent());
		
		Assert.assertNotNull(filteredSpec.get("paths").get("/Order_Details*").get("get"), "/Order_Details*:GET is expected");
		Assert.assertNotNull(filteredSpec.get("paths").get("/Order_Details({Id})*").get("get"), "/Order_Details({Id})*:GET is expected");
		Assert.assertNotNull(filteredSpec.get("paths").get("/Regions*").get("get"), "/Regions*:GET is expected");
		Assert.assertNotNull(filteredSpec.get("paths").get("/Regions({Id})*").get("patch"), "/Regions({Id})*:PATCH is expected");
		Assert.assertNull(filteredSpec.get("paths").get("/Regions({Id})*").get("delete"), "/Regions({Id})*:DELETE should be filtered");
		Assert.assertNull(filteredSpec.get("paths").get("/Categories*"), "/Categories* should be filtered");
		Assert.assertNull(filteredSpec.get("paths").get("/Products*"), "/Regions({Id}) should be filtered");
		Assert.assertNull(filteredSpec.get("paths").get("/Employees({Id})*"), "/Employees({Id}) should be filtered");
		
		Assert.assertNotNull(filteredSpec.get("components").get("schemas").get("Invoice"));
		Assert.assertNotNull(filteredSpec.get("components").get("schemas").get("Customer"));
		Assert.assertNull(filteredSpec.get("components").get("schemas").get("Category"));
		Assert.assertNull(filteredSpec.get("components").get("schemas").get("Sales_by_Category"));
		Assert.assertNull(filteredSpec.get("components").get("schemas").get("Summary_of_Sales_by_Quarter"), "Must be excluded even if part of the includes");
	}
	
	@Test
	public void testODataV2APIWithFunctions() throws IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/ODataV2ODataDemoMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/ODataV2ODataDemoOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767/api/v1/myAPI", null);
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testODataV2APIBackendFromMetadata() throws IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/ODataV2NorthWindMetadata.xml");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/Northwind/Northwind.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasePath(null, null);
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		JsonNode openAPI = mapper.readTree(apiDefinition.getApiSpecificationContent());
		
		Assert.assertEquals(openAPI.get("servers").get(0).get("url").asText(), "https://services.odata.org/V2/Northwind/Northwind.svc"); // Has our configured base path
	}
	
	@Test
	public void testSAPODataV2AccountDuplicateCheck() throws IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/SAP-AccountDuplicateCheckMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/SAP-AccountDuplicateCheckOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767/api/v1/myAPI", null);
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testSAPODataV2360ReviewsManagement() throws IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/SAP-PMGMMultiraterMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/SAP-PMGMMultiraterOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767/api/v1/myAPI", null);
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testSAPODataAccountsAndTransactionsAPI() throws IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/SAP-AccountsAndTransactionsMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/SAP-AccountsAndTransactionsOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767/api/v1/myAPI", null);
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testSAPODataBusinessPartnerA2XAPI() throws IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/SAP-BusinessPartnerA2XMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/SAP-BusinessPartnerA2XOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767/api/v1/myAPI", null);
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testSAPODataBasicProductAvailabilityInfoAPI() throws IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/SAP-BasicProductAvailabilityInfoMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/SAP-BasicProductAvailabilityInfoOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767/api/v1/myAPI", null);
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testSAPODataMasterDataForBusinessPartnerAPI() throws IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/SAP-MasterDataForBusinessPartnerMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/SAP-MasterDataForBusinessPartnerOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767/api/v1/myAPI", null);
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testSAPODataCustomerMaterialA2XAPI() throws IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/SAP-CustomerMaterialA2XMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/SAP-CustomerMaterialA2XOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasePath("https://myhost.customer.com:8767/api/v1/myAPI", null);
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "Detected OData V3 specification, which is not yet supported by the APIM-CLI..*")
	public void testODataV3API() throws IOException {
		
		byte[] content = getAPISpecificationContent(TEST_PACKAGE+"/ODataV3ODataDemoMetadata.xml");
		APISpecificationFactory.getAPISpecification(content, "https://any.odata.service", "OData-V3-Test-API");
	}
	
	@Test
	public void testODataV4API() throws IOException {
		
		byte[] content = getAPISpecificationContent(TEST_PACKAGE+"/ODataV4TrippinServiceMetadata.xml");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "https://any.odata.service", "OData-V4-Test-API");
		Assert.assertTrue(apiDefinition instanceof ODataV4Specification);
		ODataV4Specification oDataV4Specification = (ODataV4Specification) apiDefinition;
		byte[] openAPI = oDataV4Specification.getApiSpecificationContent();
		ObjectMapper objectMapper = new ObjectMapper();
		TypeReference<OpenAPI> api = new TypeReference<OpenAPI>(){};
		OpenAPI generatedAPI = objectMapper.readValue(openAPI, api);
		Assert.assertEquals(generatedAPI.getInfo().getTitle(), "Trippin OData Service");
	}


	@Test
	public void testOData4SAPSalesPricingReadOdataV4() throws IOException {
		byte[] content = getAPISpecificationContent(TEST_PACKAGE+"/ODataV4_SAP_OP_SLSPRCGACCESSSEQUENCE_0001.edmx");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "https://any.odata.service", "OData-V4-SAP");
		Assert.assertTrue(apiDefinition instanceof ODataV4Specification);
		ODataV4Specification oDataV4Specification = (ODataV4Specification) apiDefinition;
		byte[] openAPI = oDataV4Specification.getApiSpecificationContent();
		ObjectMapper objectMapper = new ObjectMapper();
		TypeReference<OpenAPI> api = new TypeReference<OpenAPI>(){};
		OpenAPI generatedAPI = objectMapper.readValue(openAPI, api);
		//Console.println(new String(openAPI));
		Assert.assertEquals(generatedAPI.getInfo().getTitle(), "com.sap.gateway.srvd_a2x.api_slsprcgaccesssequence.v0001 OData Service");
	}

	
	private byte[] getAPISpecificationContent(String swaggerFile) throws AppException {
		try {
			return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}
}
