package com.axway.lib;

import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Base64;

public class BasicAuthInterceptor implements ClientHttpRequestInterceptor {


    @Value("${apiManagerUser}")
    private String username;

    @Value("${apiManagerPass}")
    private String password;

    private String authorizationHeaderValue;

    public String getAuthorizationHeaderValue(){
        if(authorizationHeaderValue != null)
            return authorizationHeaderValue;
        String format = username + ":" + password;
        authorizationHeaderValue =  "Basic " + Base64.getEncoder().encodeToString(format.getBytes());
        return authorizationHeaderValue;
    }


    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderValue());
        return execution.execute(request, body);
    }
}
