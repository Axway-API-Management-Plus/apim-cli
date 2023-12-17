package com.axway.apim;


import org.citrusframework.http.client.HttpClient;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.variable.GlobalVariablesPropertyLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EndpointConfigTest extends TestNGCitrusSpringSupport {

    @Autowired
    ApplicationContext context;
    @Test
    public void testConfiguration(){
        HttpClient httpClient = context.getBean("apiManager", HttpClient.class);
        Assert.assertNotNull(httpClient);

        GlobalVariables globalVariables = context.getBean("globalVariables", GlobalVariables.class);
        Assert.assertNotNull(globalVariables);
        Assert.assertEquals(globalVariables.getVariables().get("myVar"), "foo");


        GlobalVariablesPropertyLoader globalVariablesPropertyLoader = context.getBean("globalVariablesPropertyLoader", GlobalVariablesPropertyLoader.class);
        Assert.assertNotNull(globalVariablesPropertyLoader);
        Assert.assertNotNull(globalVariablesPropertyLoader.getPropertyFiles());

        BasicAuthInterceptor basicAuthInterceptor =  context.getBean("basicAuthInterceptor", BasicAuthInterceptor.class);
        String basicAuthHeaderValue = basicAuthInterceptor.getAuthorizationHeaderValue();
        Assert.assertNotNull(basicAuthHeaderValue);

        // get cached value
        basicAuthHeaderValue = basicAuthInterceptor.getAuthorizationHeaderValue();
        Assert.assertNotNull(basicAuthHeaderValue);

    }
}
