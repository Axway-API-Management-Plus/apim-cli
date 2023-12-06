package com.axway.apim.api.model;

import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class APIAccessTest {

    @Test
    public void compareAPIAccess(){
        List<APIAccess> source = new ArrayList<>();
        APIAccess apiAccess1 = new APIAccess();
        apiAccess1.setApiName("api1");
        apiAccess1.setApiVersion("1.0.0");

        APIAccess apiAccess2 = new APIAccess();
        apiAccess1.setApiName("api2");
        apiAccess1.setApiVersion("1.0.0");

        source.add(apiAccess1);
        source.add(apiAccess2);

        List<APIAccess> target = new ArrayList<>();

        target.add(apiAccess1);
        target.add(apiAccess2);

        Assert.assertTrue(  Utils.compareValues(source, target));
    }

    @Test
    public void compareAPIAccessDifferentOrder(){
        List<APIAccess> source = new ArrayList<>();
        APIAccess apiAccess1 = new APIAccess();
        apiAccess1.setApiName("api1");
        apiAccess1.setApiVersion("1.0.0");

        APIAccess apiAccess2 = new APIAccess();
        apiAccess1.setApiName("api2");
        apiAccess1.setApiVersion("1.0.0");

        source.add(apiAccess1);
        source.add(apiAccess2);

        List<APIAccess> target = new ArrayList<>();
        target.add(apiAccess2);
        target.add(apiAccess1);
        Assert.assertTrue(  Utils.compareValues(source, target));
    }
    @Test
    public void compareAPIAccessDifferentOrderWithIds(){
        List<APIAccess> source = new ArrayList<>();
        APIAccess apiAccess1 = new APIAccess();
        apiAccess1.setApiName("Test-App-API2-9729");
        apiAccess1.setApiVersion("1.0.0");
        apiAccess1.setId("efadbfb7-432c-4b55-ab07-cbfbf78f060e");
        apiAccess1.setState("approved");
        apiAccess1.setApiId("80ac0c19-aa6b-49f5-9fac-d526f3acf96a");

        APIAccess apiAccess2 = new APIAccess();
        apiAccess2.setApiName("Test-App-API1-9729");
        apiAccess2.setApiVersion("1.0.0");
        apiAccess2.setId("e2df9f6d-b33d-47e4-a2a9-d3cd91ade68e");
        apiAccess2.setState("approved");

        apiAccess2.setApiId("7008e73b-93f9-4eb3-9eb3-11afc4e08a6f");

        source.add(apiAccess1);
        source.add(apiAccess2);

        List<APIAccess> target = new ArrayList<>();

        APIAccess apiAccess3 = new APIAccess();
        apiAccess3.setApiName("Test-App-API1-9729");
        apiAccess3.setApiVersion("1.0.0");
        APIAccess apiAccess4 = new APIAccess();
        apiAccess4.setApiName("Test-App-API2-9729");
        apiAccess4.setApiVersion("1.0.0");


        target.add(apiAccess3);
        target.add(apiAccess4);

        Assert.assertTrue(  Utils.compareValues(source, target));
    }


}
