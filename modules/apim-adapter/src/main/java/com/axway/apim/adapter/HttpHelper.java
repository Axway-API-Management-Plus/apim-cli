package com.axway.apim.adapter;

import com.axway.apim.lib.utils.rest.RestAPICall;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpHelper {

    public Response execute(RestAPICall restAPICall, boolean responseBody) throws IOException {
        try(CloseableHttpResponse closeableHttpResponse = (CloseableHttpResponse) restAPICall.execute()){
            Response response = new Response();
            response.setStatusCode(closeableHttpResponse.getStatusLine().getStatusCode());
            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            if(responseBody && httpEntity != null)
                response.setResponse(EntityUtils.toString(httpEntity));
            return response;
        }
    }
}
