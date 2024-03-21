package com.axway.apim;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.interceptor.LoggingClientInterceptor;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.variable.GlobalVariablesPropertyLoader;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

@Configuration
@PropertySource({"classpath:citrus-global-variables.properties"})
public class EndpointConfig {

    @Value("${apiManagerHost}")
    private String host;

    @Value("${apiManagerPort}")
    private int port;

    @Bean
    public HttpClient apiManager() {
        return CitrusEndpoints
            .http()
            .client()
            .requestUrl("https://" + host + ":" + port + "/api/portal/v1.4")
            .requestFactory(sslRequestFactory())
            .interceptors(interceptors())
            .build();
    }

    @Bean
    public org.apache.hc.client5.http.classic.HttpClient httpClient() {

        try {
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext, NoopHostnameVerifier.INSTANCE);
            PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();
            return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new BeanCreationException("Failed to create http client for ssl connection", e);
        }
    }

    @Bean
    public List<ClientHttpRequestInterceptor> interceptors() {
        return Arrays.asList(new LoggingClientInterceptor(), basicAuthInterceptor());
    }

    @Bean
    BasicAuthInterceptor basicAuthInterceptor() {
        return new BasicAuthInterceptor();
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory sslRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }

    @Bean
    public GlobalVariables globalVariables() {
        GlobalVariables globalVariables = new GlobalVariables();
        globalVariables.getVariables().put("myVar", "foo");
        return globalVariables;
    }

    @Bean
    public GlobalVariablesPropertyLoader globalVariablesPropertyLoader() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.getPropertyFiles().add("classpath:citrus-global-variables.properties");
        return propertyLoader;
    }
}
