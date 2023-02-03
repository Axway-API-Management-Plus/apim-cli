package com.axway.apim.api.apiSpecification;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Objects;

public class APISpecificationSwagger1xTest {

    private static final String testPackage = "/com/axway/apim/adapter/spec";

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testSwagger12() throws IOException {
        byte[] content = getSwaggerContent(testPackage + "/swagger12.json");
        APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
        apiDefinition.configureBasePath("https://petstore.swagger.io", null);
        Assert.assertTrue(apiDefinition instanceof Swagger1xSpecification, "Specification must be an Swagger12Specification");
        Assert.assertEquals(apiDefinition.getDescription(), "Swagger 1.2 Description");
        JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
        Assert.assertEquals(swagger.get("basePath").asText(), "https://example.com:8443/test-api");
    }


    @Test
    public void testSwagger11Specification() throws IOException {
        byte[] content = getSwaggerContent(testPackage + "/swagger11.json");
        APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "teststore.json", "Test-API");
        apiDefinition.configureBasePath("https://petstore.swagger.io:443/myapi/", null);
        Assert.assertTrue(apiDefinition instanceof Swagger1xSpecification, "Specification must be an Swagger12Specification");
        JsonNode swagger = mapper.readTree(apiDefinition.getApiSpecificationContent());
        Assert.assertEquals(swagger.get("basePath").asText(), "http://emr-system:8081");
    }


    private byte[] getSwaggerContent(String swaggerFile) throws AppException {
        try {
            return IOUtils.toByteArray(Objects.requireNonNull(this.getClass().getResourceAsStream(swaggerFile)));
        } catch (IOException e) {
            throw new AppException("Can't read Swagger-File: '" + swaggerFile + "'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
        }
    }
}
