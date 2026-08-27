package com.axway.apim.lib.utils.rest;

import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The interface to the API-Manager itself responsible to set up the underlying HTTPS-Communication.
 * It's used by the RESTAPICall.
 * Implemented as a Singleton, which holds the actual connection to the API-Manager.
 *
 * @author cwiechmann@axway.com
 */
public class APIMHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(APIMHttpClient.class);
    private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager httpClientConnectionManager;
    private HttpClientContext clientContext;
    private final BasicCookieStore cookieStore = new BasicCookieStore();
    private String csrfToken;
    private static APIMHttpClient apimHttpClient;

    public static APIMHttpClient getInstance() throws AppException {
        if (apimHttpClient == null) {
            apimHttpClient = new APIMHttpClient();
        }
        return apimHttpClient;
    }

    public static void deleteInstances() {
        apimHttpClient = null;
    }

    private APIMHttpClient() throws AppException {
        CoreParameters params = CoreParameters.getInstance();
        createConnection(params.getAPIManagerURL());
    }

    private void createConnection(URI uri) throws AppException {
        HttpHost targetHost;
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            CoreParameters params = CoreParameters.getInstance();
            int timeout = params.getTimeout();
            LOG.debug("API Manager CLI http client timeout : {}", timeout);

            builder.loadTrustMaterial(null, new TrustAllStrategy());
            TlsSocketStrategy tlsSocketStrategy = new DefaultClientTlsStrategy(builder.build(), HostnameVerificationPolicy.CLIENT, NoopHostnameVerifier.INSTANCE);

            httpClientConnectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsSocketStrategy)
                .setMaxConnTotal(5)
                .setMaxConnPerRoute(2)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                    .setConnectTimeout(Timeout.ofMilliseconds(timeout))
                    .build())
                .build();
            targetHost = new HttpHost(uri.getScheme(), uri.getHost(), uri.getPort());
            // Add AuthCache to the execution context
            clientContext = HttpClientContext.create();
            clientContext.setCookieStore(cookieStore);
            httpClientConnectionManager.setMaxPerRoute(new HttpRoute(targetHost), 2);
            // We have make sure, that cookies are correctly parsed!
            RequestConfig.Builder defaultRequestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofMilliseconds(timeout))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(timeout))
                .setCookieSpec(StandardCookieSpec.RELAXED);
            HttpClientBuilder clientBuilder = HttpClientBuilder.create()
                .disableRedirectHandling()
                .setConnectionManager(httpClientConnectionManager)
                .useSystemProperties();

            // Check if a proxy is configured
            if (params.getProxyHost() != null) {
                LOG.debug("API Manager CLI using Http(s) proxy : {}", params.getProxyHost());
                HttpHost proxyHost = new HttpHost(params.getProxyHost(), params.getProxyPort());
                HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
                clientBuilder.setRoutePlanner(routePlanner);
                if (params.getProxyUsername() != null) {
                    LOG.debug("API Manager CLI using Http(s) proxy Authentication");
                    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(
                        new AuthScope(params.getProxyHost(), params.getProxyPort()),
                        new UsernamePasswordCredentials(params.getProxyUsername(), params.getProxyPassword().toCharArray()));
                    clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
                defaultRequestConfig.setProxy(proxyHost);
            }
            if (params.isDisableCompression())
                clientBuilder.disableContentCompression();
            clientBuilder.setDefaultRequestConfig(defaultRequestConfig.build());
            String customHeader = params.getCustomHeaders();
            if (customHeader != null) {
                List<Header> headers = splitStringToHttpHeaders(customHeader);
                clientBuilder.setDefaultHeaders(headers);
            }
            this.httpClient = clientBuilder.build();
        } catch (Exception e) {
            throw new AppException("Can't create connection to API-Manager.", ErrorCode.API_MANAGER_COMMUNICATION);
        }
    }

    public List<Header> splitStringToHttpHeaders(String input) {
        List<Header> headers = new ArrayList<>();
        String[] headersArray = input.split(",");
        for (String headerStr : headersArray) {
            StringTokenizer tokenizer = new StringTokenizer(headerStr, ":");
            while (tokenizer.hasMoreTokens()) {
                String key = tokenizer.nextToken();
                String value = tokenizer.nextToken();
                Header header = new BasicHeader(key, value);
                headers.add(header);
            }
        }
        return headers;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public HttpClientContext getClientContext() {
        return clientContext;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    @Override
    public String toString() {
        return "APIMHttpClient [cookieStore=" + cookieStore + ", csrfToken=" + csrfToken + "]";
    }

    public void close() {
        if (httpClientConnectionManager != null) {
            httpClientConnectionManager.close();
        }
    }
}
