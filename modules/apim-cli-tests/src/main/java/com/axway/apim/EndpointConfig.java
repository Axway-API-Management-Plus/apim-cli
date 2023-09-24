package com.axway.apim;

import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.interceptor.LoggingClientInterceptor;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.variable.GlobalVariablesPropertyLoader;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

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
    public HttpClient apiManager() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return CitrusEndpoints
            .http()
            .client()
            .requestUrl("https://" + host + ":" + port + "/api/portal/v1.4")
            .requestFactory(sslRequestFactory())
            .interceptors(interceptors())
            .build();
    }

    @Bean
    public org.apache.http.client.HttpClient httpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        return HttpClientBuilder.create()
            .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .build();
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
    public HttpComponentsClientHttpRequestFactory sslRequestFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
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
