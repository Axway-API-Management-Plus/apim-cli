package com.axway.apim.api.definition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APISpecificationODataTest {
	
	ObjectMapper mapper = new ObjectMapper();
	
	private static String TEST_PACKAGE = "/com/axway/apim/api/definition/odata";
	
	@BeforeClass
	private void initTestIndicator() {
		APIImportParams params = new APIImportParams();
		params.setReplaceHostInSwagger(true);
	}
	
	@Test
	public void testODataV2API() throws AppException, IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/ODataV2NorthWindMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/ODataV2NorthWindOpenAPI3.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "northwind-odata-v2.xml$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testODataV2APIWithFunctions() throws AppException, IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/ODataV2ODataDemoMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/ODataV2ODataDemoOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testODataV2APIBackendFromMetadata() throws AppException, IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/ODataV2NorthWindMetadata.xml");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/Northwind/Northwind.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasepath(null);
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		JsonNode openAPI = mapper.readTree(apiDefinition.getApiSpecificationContent());
		
		Assert.assertEquals(openAPI.get("servers").get(0).get("url").asText(), "https://services.odata.org/V2/Northwind/Northwind.svc"); // Has our configured base path
	}
	
	@Test
	public void testSAPODataV2AccountDuplicateCheck() throws AppException, IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/SAP-AccountDuplicateCheckMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/SAP-AccountDuplicateCheckOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testSAPODataV2360ReviewsManagement() throws AppException, IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/SAP-PMGMMultiraterMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/SAP-PMGMMultiraterOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testSAPODataAccountsAndTransactionsAPI() throws AppException, IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/SAP-AccountsAndTransactionsMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/SAP-AccountsAndTransactionsOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testSAPODataBusinessPartnerA2XAPI() throws AppException, IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/SAP-BusinessPartnerA2XMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/SAP-BusinessPartnerA2XOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testSAPODataBasicProductAvailabilityInfoAPI() throws AppException, IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/SAP-BasicProductAvailabilityInfoMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/SAP-BasicProductAvailabilityInfoOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testSAPODataMasterDataForBusinessPartnerAPI() throws AppException, IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/SAP-MasterDataForBusinessPartnerMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/SAP-MasterDataForBusinessPartnerOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	@Test
	public void testSAPODataCustomerMaterialA2XAPI() throws AppException, IOException {
		
		byte[] odataMetadata = getAPISpecificationContent(TEST_PACKAGE+"/SAP-CustomerMaterialA2XMetadata.xml");
		byte[] odataOpenAPI3 = getAPISpecificationContent(TEST_PACKAGE+"/SAP-CustomerMaterialA2XOpenAPI.json");
		APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(odataMetadata, "https://services.odata.org/V2/(S(owef4vwcosio0xpu1glpf320))/OData/OData.svc/$metadata", "OData-V2-Test-API");
		apiDefinition.configureBasepath("https://myhost.customer.com:8767/api/v1/myAPI");
		
		Assert.assertTrue(apiDefinition instanceof ODataV2Specification);
		if(apiDefinition.getApiSpecificationContent().length!=odataOpenAPI3.length) {
			System.out.print(new String(apiDefinition.getApiSpecificationContent(), StandardCharsets.UTF_8));
		}
		Assert.assertEquals(apiDefinition.getApiSpecificationContent(), odataOpenAPI3);
	}
	
	
	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "Detected OData V3 specification, which is not yet supported by the APIM-CLI..*")
	public void testODataV3API() throws AppException, IOException {
		
		byte[] content = getAPISpecificationContent(TEST_PACKAGE+"/ODataV3ODataDemoMetadata.xml");
		APISpecificationFactory.getAPISpecification(content, "https://any.odata.service", "OData-V3-Test-API");
	}
	
	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "Detected OData V4 specification, which is not yet supported by the APIM-CLI..*")
	public void testODataV4API() throws AppException, IOException {
		
		byte[] content = getAPISpecificationContent(TEST_PACKAGE+"/ODataV4TrippinServiceMetadata.xml");
		APISpecificationFactory.getAPISpecification(content, "https://any.odata.service", "OData-V4-Test-API");
	}
	
	
	private byte[] getAPISpecificationContent(String swaggerFile) throws AppException {
		try {
			return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
		} catch (IOException e) {
			throw new AppException("Can't read Swagger-File: '"+swaggerFile+"'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
		}
	}
}
