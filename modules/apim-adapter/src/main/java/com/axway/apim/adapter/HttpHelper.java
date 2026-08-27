package com.axway.apim.adapter;

import com.axway.apim.lib.utils.rest.RestAPICall;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

public class HttpHelper {

    public Response execute(RestAPICall restAPICall, boolean responseBody) throws IOException, ParseException {
        try(CloseableHttpResponse closeableHttpResponse = (CloseableHttpResponse) restAPICall.execute()){
            Response response = new Response();
            response.setStatusCode(closeableHttpResponse.getCode());
            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            if(responseBody && httpEntity != null)
                response.setResponseBody(EntityUtils.toString(httpEntity));
            return response;
        }
    }
}
