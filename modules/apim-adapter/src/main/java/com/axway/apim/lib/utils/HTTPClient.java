package com.axway.apim.lib.utils;

import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.*;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class HTTPClient implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(HTTPClient.class);
    private final URI url;
    private final String password;
    private final String username;
    private CloseableHttpClient closeableHttpClient = null;
    private HttpClientContext clientContext;

    public HTTPClient(String url, String username, String password) throws AppException {
        try {
            this.url = new URI(url);
            this.password = password;
            this.username = username;
            getClient();
        } catch (URISyntaxException e) {
            throw new AppException("Error creating HTTP-Client.", ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    public void getClient() throws AppException {
        try {
            SSLContextBuilder builder = SSLContextBuilder.create();
            builder.loadTrustMaterial(null, new TrustAllStrategy());
            TlsSocketStrategy tlsSocketStrategy = new DefaultClientTlsStrategy(builder.build(), HostnameVerificationPolicy.CLIENT, NoopHostnameVerifier.INSTANCE);
            PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsSocketStrategy)
                .build();
            var httpClientBuilder = HttpClients.custom()
                .setConnectionManager(connectionManager);
            if (this.username != null) {
                BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(new AuthScope(null, -1),
                    new UsernamePasswordCredentials(username, password != null ? password.toCharArray() : new char[0]));
                AuthCache authCache = new BasicAuthCache();
                BasicScheme basicAuth = new BasicScheme();
                authCache.put(new HttpHost(url.getScheme(), url.getHost(), url.getPort()), basicAuth);
                clientContext = HttpClientContext.create();
                clientContext.setAuthCache(authCache);
                clientContext.setCredentialsProvider(credsProvider);
                httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
            }
            this.closeableHttpClient = httpClientBuilder.build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new AppException("Error creating HTTP-Client.", ErrorCode.UNXPECTED_ERROR, e);
        }
    }

    public CloseableHttpResponse execute(ClassicHttpRequest request) throws IOException {
        return (CloseableHttpResponse) closeableHttpClient.executeOpen(null, request, clientContext);
    }

    @Override
    public void close() throws Exception {
        try {
            this.closeableHttpClient.close();
        } catch (IOException e) {
            LOG.error("error closing http client", e);
        }
    }
}
