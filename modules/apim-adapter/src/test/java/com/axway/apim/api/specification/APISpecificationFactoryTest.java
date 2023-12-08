package com.axway.apim.api.specification;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerAPIAdapter;
import com.axway.apim.adapter.apis.APIManagerOrganizationAdapter;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.util.Asserts;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class APISpecificationFactoryTest extends WiremockWrapper {


    @BeforeClass
    public void init() {
        initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }

    ClassLoader classLoader = this.getClass().getClassLoader();

    @Test
    public void getAPISpecificationOpenApi() throws AppException {
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("openapi.json", specDirPath, "petstore");
        Assert.assertEquals(APISpecification.APISpecType.valueOf("OPEN_API_30"), apiSpecification.getAPIDefinitionType());
        Assert.assertNotNull(apiSpecification.getDescription());
        Assert.assertNotNull(apiSpecification.getApiSpecificationContent());
    }

    @Test
    public void getAPISpecificationSwagger2() throws AppException {
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("airports_swagger_20.json", specDirPath, "petstore");
        Assert.assertEquals(APISpecification.APISpecType.valueOf("SWAGGER_API_20"), apiSpecification.getAPIDefinitionType());
        Assert.assertNotNull(apiSpecification.getDescription());
        Assert.assertNotNull(apiSpecification.getApiSpecificationContent());
    }

    @Test
    public void getAPISpecificationWADL() throws AppException {
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("sample-accounts-api.wadl", specDirPath, "petstore");
        Assert.assertEquals(APISpecification.APISpecType.valueOf("WADL_API"), apiSpecification.getAPIDefinitionType());
        Assert.assertNotNull(apiSpecification.getApiSpecificationContent());
    }

    @Test
    public void getAPISpecificationWSDL() throws AppException {
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("sample.wsdl", specDirPath, "petstore");
        Assert.assertEquals(APISpecification.APISpecType.valueOf("WSDL_API"), apiSpecification.getAPIDefinitionType());
        Assert.assertNotNull(apiSpecification.getApiSpecificationContent());
    }

    @Test
    public void getAPISpecificationOdataV2() throws AppException {
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("ODataV2NorthWindMetadata.xml", specDirPath, "petstore");
        Assert.assertEquals(APISpecification.APISpecType.valueOf("ODATA_V2"), apiSpecification.getAPIDefinitionType());
        Assert.assertNotNull(apiSpecification.getApiSpecificationContent());
    }

    @Test(expectedExceptions = AppException.class)
    public void getAPISpecificationOdataV3() throws AppException {
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec/odata").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("ODataV3ODataDemoMetadata.xml", specDirPath, "petstore");
        Assert.assertEquals(APISpecification.APISpecType.valueOf("ODATA_V3"), apiSpecification.getAPIDefinitionType());
    }

    @Test
    public void getAPISpecificationOdataV4() throws AppException {
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec/odata").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("ODataV4TrippinServiceMetadata.xml", specDirPath, "petstore");
        Assert.assertEquals(APISpecification.APISpecType.valueOf("ODATA_V4"), apiSpecification.getAPIDefinitionType());
        Assert.assertNotNull(apiSpecification.getApiSpecificationContent());
    }

    @Test
    public void getAPISpecificationSwagger12() throws AppException {
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("swagger12.json", specDirPath, "petstore");
        Assert.assertEquals(APISpecification.APISpecType.SWAGGER_API_1X, apiSpecification.getAPIDefinitionType());
        Assert.assertNotNull(apiSpecification.getApiSpecificationContent());
    }

    @Test
    public void getAPISpecificationSwagger11() throws AppException {
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("swagger11.json", specDirPath, "petstore");
        Assert.assertEquals(APISpecification.APISpecType.SWAGGER_API_1X, apiSpecification.getAPIDefinitionType());
        Assert.assertNotNull(apiSpecification.getApiSpecificationContent());
    }

    @Test
    public void getAPISpecificationGraphql() throws AppException {
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("starwars.graphqls", specDirPath, "starwars");
        Assert.assertEquals(APISpecification.APISpecType.GRAPHQL, apiSpecification.getAPIDefinitionType());
        Assert.assertNotNull(apiSpecification.getApiSpecificationContent());
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "Can't handle API specification. No suitable API-Specification implementation available.")
    public void getAPISpecificationUnknown() throws AppException {
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("unknown.txt", specDirPath, "petstore");
        Assert.assertEquals(APISpecification.APISpecType.valueOf("UNKNOWN"), apiSpecification.getAPIDefinitionType());
    }

    @Test
    public void downloadOpenApi() throws IOException{
        try(InputStream inputStream = APISpecificationFactory.getAPIDefinitionFromURL("https://localhost:8075/openapi.json")) {
           ObjectMapper objectMapper = new ObjectMapper();
           Map<String, Object> json = objectMapper.readValue(inputStream, Map.class);
            Assert.assertEquals((String)json.get("openapi"), "3.0.2");
        }
    }

    @Test
    public void downloadWsdl() throws IOException{
        try(InputStream inputStream = APISpecificationFactory.getAPIDefinitionFromURL("https://localhost:8075/sample.wsdl")) {
            String content = IOUtils.toString(inputStream, "UTF-8");
            Assert.assertTrue(content.contains("CustomBinding_MNBArfolyamServiceSoap"));
        }
    }

    @Test
    public void downloadGraphql() throws IOException{
        try(InputStream inputStream = APISpecificationFactory.getAPIDefinitionFromURL("https://localhost:8075/graphql/starwars.graphqls")) {
            String content = IOUtils.toString(inputStream, "UTF-8");
            Assert.assertTrue(content.contains("schema {"));
        }
    }

    @Test
    public void downloadGraphqlFromIntrospection() throws IOException {
        try(InputStream inputStream = APISpecificationFactory.getAPIDefinitionFromURL("https://localhost:8075/graphql/introspection")) {
            String content = IOUtils.toString(inputStream, "UTF-8");
            Assert.assertTrue(content.contains("schema {"));
        }
    }
}
