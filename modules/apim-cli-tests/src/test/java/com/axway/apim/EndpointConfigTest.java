package com.axway.apim;


import org.citrusframework.http.client.HttpClient;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.variable.GlobalVariablesPropertyLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class EndpointConfigTest extends TestNGCitrusSpringSupport {

    @Autowired
    ApplicationContext context;
    @Test
    public void testConfiguration() throws IOException {
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

        HttpRequest httpRequest = new ClientHttpRequest() {

            HttpHeaders httpHeaders = new HttpHeaders();

            @Override
            public ClientHttpResponse execute() throws IOException {
                return null;
            }

            @Override
            public OutputStream getBody() throws IOException {
                return null;
            }

            @Override
            public HttpMethod getMethod() {
                return null;
            }

            @Override
            public URI getURI() {
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                return httpHeaders;
            }
        };

        ClientHttpRequestExecution clientHttpRequestExecution = (request, body) -> {
            ClientHttpResponse clientHttpResponse = new MockClientHttpResponse(body, HttpStatus.OK);
            return clientHttpResponse;
        };
        ClientHttpResponse httpResponse = basicAuthInterceptor.intercept(httpRequest, "hello world".getBytes(), clientHttpRequestExecution);
        Assert.assertNotNull(httpResponse);
        Assert.assertNotNull(httpRequest.getHeaders().get(HttpHeaders.AUTHORIZATION));

    }
}
