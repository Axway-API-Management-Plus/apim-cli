package com.axway.apim.api.specification;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.error.AppException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class APISpecificationFactoryTest {
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
        Assert.assertEquals(APISpecification.APISpecType.valueOf("SWAGGER_API_1x"), apiSpecification.getAPIDefinitionType());
        Assert.assertNotNull(apiSpecification.getApiSpecificationContent());
    }
    @Test
    public void getAPISpecificationSwagger11() throws AppException {
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("swagger11.json", specDirPath, "petstore");
        Assert.assertEquals(APISpecification.APISpecType.valueOf("SWAGGER_API_1x"), apiSpecification.getAPIDefinitionType());
        Assert.assertNotNull(apiSpecification.getApiSpecificationContent());
    }
    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "Can't handle API specification. No suitable API-Specification implementation available.")
    public void getAPISpecificationUnknown() throws AppException {
        String specDirPath = classLoader.getResource("com/axway/apim/adapter/spec").getFile();
        APISpecification apiSpecification = APISpecificationFactory.getAPISpecification("unknown.txt", specDirPath, "petstore");
        Assert.assertEquals(APISpecification.APISpecType.valueOf("UNKNOWN"), apiSpecification.getAPIDefinitionType());
    }
}
