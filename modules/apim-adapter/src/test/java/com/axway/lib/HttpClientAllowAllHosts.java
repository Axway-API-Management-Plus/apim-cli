package com.axway.lib;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class HttpClientAllowAllHosts {

    public HttpClient httpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        return httpClient;
    }

    public HttpComponentsClientHttpRequestFactory sslRequestFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }
}
