package com.axway.apim.api.specification;

import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class APISpecificationWADLTest {

    XmlMapper xmlMapper = new XmlMapper();


    private static final String testPackage = "/com/axway/apim/adapter/spec";

    @Test
    public void testSamplePaymentsWADLAPI() throws IOException {

        byte[] content = getAPISpecificationContent(testPackage + "/sample-payment-api.wadl");
        APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "sample-payment-api.wadl", "Test-API");
        apiDefinition.configureBasePath("https://myhost.customer.com:8767/api/v1/myAPI", null);

        Assert.assertTrue(apiDefinition instanceof WADLSpecification);
        // Check if the WADL-File has been changed based on the configured base path
        JsonNode wadl = xmlMapper.readTree(apiDefinition.getApiSpecificationContent());
        JsonNode resourcesNode = wadl.get("resources");
        String base = resourcesNode.get("base").asText();
        Assert.assertEquals(base, "https://myhost.customer.com:8767/api/v1/myAPI/");
    }

    @Test
    public void testSampleAccountsWADLAPI() throws IOException {

        byte[] content = getAPISpecificationContent(testPackage + "/sample-accounts-api.wadl");
        APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "sample-accounts-api.wadl", "Test-API");
        apiDefinition.configureBasePath("https://myhost.customer.com:8767/api/v1/myAPI", null);

        Assert.assertTrue(apiDefinition instanceof WADLSpecification);
        // Check if the WADL-File has been changed based on the configured base path
        JsonNode wadl = xmlMapper.readTree(apiDefinition.getApiSpecificationContent());
        JsonNode resourcesNode = wadl.get("resources");
        String base = resourcesNode.get("base").asText();
        Assert.assertEquals(base, "https://myhost.customer.com:8767/api/v1/myAPI/");
    }


    private byte[] getAPISpecificationContent(String swaggerFile) throws AppException {
        try {
            return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
        } catch (IOException e) {
            throw new AppException("Can't read Swagger-File: '" + swaggerFile + "'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
        }
    }
}
