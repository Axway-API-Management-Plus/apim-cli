package com.axway.apim.api.model;

import com.axway.apim.lib.CoreParameters;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ServiceProfileTest {

    @Test
    public void compareServiceProfileWithDifferentApiId(){
        ServiceProfile serviceProfile = new ServiceProfile();
        serviceProfile.setApiId("12345678");
        serviceProfile.setBasePath("https://api.axway.com");
        ServiceProfile serviceProfileTarget = new ServiceProfile();
        serviceProfileTarget.setApiId("912345678");
        serviceProfileTarget.setBasePath("https://api.axway.com");
        Assert.assertEquals(serviceProfile, serviceProfileTarget);
    }

    @Test
    public void compareServiceProfileWithDifferentApiIdAndBasePath(){
        ServiceProfile serviceProfile = new ServiceProfile();
        serviceProfile.setApiId("12345678");
        serviceProfile.setBasePath("https://api.axway.com");
        ServiceProfile serviceProfileTarget = new ServiceProfile();
        serviceProfileTarget.setApiId("912345678");
        serviceProfileTarget.setBasePath("https://api.dev.axway.com");
        Assert.assertNotEquals(serviceProfile, serviceProfileTarget);
    }

    @Test
    public void compareServiceProfileWithOverrideSpecBasePath(){
        CoreParameters.getInstance().setOverrideSpecBasePath(true);
        ServiceProfile serviceProfile = new ServiceProfile();
        serviceProfile.setApiId("12345678");
        serviceProfile.setBasePath("https://api.axway.com");
        ServiceProfile serviceProfileTarget = new ServiceProfile();
        serviceProfileTarget.setApiId("912345678");
        serviceProfileTarget.setBasePath("https://api.dev.axway.com/test");
        Assert.assertNotEquals(serviceProfile, serviceProfileTarget);
    }
}
