package com.axway.apim.api.model.apps;

import org.testng.Assert;
import org.testng.annotations.Test;

public class APIKeyTest {

    @Test
    public void testEquals(){
        APIKey source = new APIKey();
        APIKey target = new APIKey();
        Assert.assertEquals(source, target);
    }
}
