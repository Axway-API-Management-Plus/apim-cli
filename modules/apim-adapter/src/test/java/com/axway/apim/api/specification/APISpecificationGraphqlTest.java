package com.axway.apim.api.specification;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class APISpecificationGraphqlTest {

    private static final String testPackage = "/com/axway/apim/adapter/spec";

    @Test
    public void isGraphqlSpecificationBasedOnFile() throws IOException {
        byte[] content = IOUtils.toByteArray(this.getClass().getResourceAsStream(testPackage + "/starwars.graphqls"));
        APISpecification apiDefinition = APISpecificationFactory.getAPISpecification(content, testPackage + "/starwars.graphqls", "Starwars");
        // Check, if the specification has been identified as a WSDL
        Assert.assertTrue(apiDefinition instanceof GraphqlSpecification);
    }
}
