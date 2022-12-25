package com.axway.apim.adapter;

import com.axway.apim.lib.utils.rest.RestAPICall;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;

public class HttpHelper {

    public int execute(RestAPICall restAPICall) throws IOException {
        try(CloseableHttpResponse closeableHttpResponse = (CloseableHttpResponse) restAPICall.execute()){
            return closeableHttpResponse.getStatusLine().getStatusCode();
        }
    }
}
