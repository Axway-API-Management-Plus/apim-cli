package com.axway.apim.api.specification;

import com.axway.apim.api.API;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class APISpecificationWSDLTest {

    private static final String testPackage = "/com/axway/apim/adapter/spec";

    @Test
    public void isWSDLSpecificationBasedOnTheURL() throws IOException {

        byte[] content = getWSDLContent(testPackage + "/sample.wsdl");
        APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "http://www.mnb.hu/arfolyamok.asmx?WSDL", "Test-API");
        // Check, if the specification has been identified as a WSDL
        Assert.assertTrue(apiDefinition instanceof WSDLSpecification);
        API testAPI = new API();
        apiDefinition.configureBasePath("https://some-url-com", testAPI);
        Assert.assertEquals(testAPI.getServiceProfiles().get("_default").getBasePath(), "https://some-url-com");
    }

    @Test
    public void isWSDLSpecificationBasedOnWSDLString() throws IOException {

        byte[] content = getWSDLContent(testPackage + "/sample.wsdl");
        APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, "http://wsdl.from.a.resource", "Test-API");
        // Check, if the specification has been identified as a WSDL
        Assert.assertTrue(apiDefinition instanceof WSDLSpecification);
    }


    @Test
    public void invalidWSDLSpecification() throws IOException {
        byte[] content = getWSDLContent(testPackage + "/invalidwsdl.xml");
        WSDLSpecification wsdlSpecification = new WSDLSpecification();
        wsdlSpecification.apiSpecificationFile = "invalidwsdl.xml";
        Assert.assertFalse(wsdlSpecification.parse(content));
    }

    @Test
    public void validWSDLSpecification() throws IOException {
        byte[] content = getWSDLContent(testPackage + "/sample.wsdl");
        WSDLSpecification wsdlSpecification = new WSDLSpecification();
        wsdlSpecification.apiSpecificationFile = "validwsdl.xml";
        Assert.assertTrue(wsdlSpecification.parse(content));
    }

    @Test
    public void validWSDLSpecificationWithoutNameSpace() throws IOException {
        byte[] content = getWSDLContent(testPackage + "/wsdl-without-ns.wsdl");
        WSDLSpecification wsdlSpecification = new WSDLSpecification();
        wsdlSpecification.apiSpecificationFile = "validwsdl.xml";
        Assert.assertTrue(wsdlSpecification.parse(content));
    }




    private byte[] getWSDLContent(String swaggerFile) throws AppException {
        try {
            return IOUtils.toByteArray(this.getClass().getResourceAsStream(swaggerFile));
        } catch (IOException e) {
            throw new AppException("Can't read Swagger-File: '" + swaggerFile + "'", ErrorCode.CANT_READ_API_DEFINITION_FILE);
        }
    }
}
