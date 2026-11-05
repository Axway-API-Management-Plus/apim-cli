package com.axway.lib.utils;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.lib.utils.HTTPClient;
import com.axway.apim.lib.utils.Utils;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

public class HttpClientTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
    }

    @AfterClass
    public void close() {
        super.close();
    }

    @Test
    public void downloadApiSpec() throws Exception {
        String url = "https://localhost:8075/api/portal/v1.4/apirepo/1f4263ca-7f03-41d9-9d34-9eff79d29bd8/download?original=true";
        HTTPClient httpClient = new HTTPClient(url, "user", Utils.getEncryptedPassword());
        httpClient.getClient();
        RequestConfig config = RequestConfig.custom()
                .setCircularRedirectsAllowed(true)
                .build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(config);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
            Assert.assertEquals(200, httpResponse.getCode());
            String response = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
            Assert.assertNotNull(response);
        }finally {
            httpClient.close();
        }
    }
}
