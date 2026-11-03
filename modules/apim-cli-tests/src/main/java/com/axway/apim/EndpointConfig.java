package com.axway.apim;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.citrusframework.dsl.endpoint.CitrusEndpoints;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.interceptor.LoggingClientInterceptor;
import org.citrusframework.spring.variable.GlobalVariablesPropertyLoader;
import org.citrusframework.variable.GlobalVariables;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@PropertySource({"classpath:citrus-global-variables.properties"})
public class EndpointConfig implements EnvironmentAware {

    /**
     * Environment variable per property, so that the credentials and the host are not stored in the repository.
     * They cannot be expressed as ${...} placeholders inside citrus-global-variables.properties, because the Citrus
     * GlobalVariablesPropertyLoader reads that very file and resolves ${...} as a Citrus test variable, which fails
     * for anything it does not know.
     */
    private static final Map<String, String> ENVIRONMENT_OVERRIDES = buildEnvironmentOverrides();

    private static Map<String, String> buildEnvironmentOverrides() {
        Map<String, String> overrides = new LinkedHashMap<>();
        overrides.put("apiManagerHost", "apimanager_host");
        overrides.put("apiManagerPass", "apimanager_password");
        overrides.put("oadminPassword1", "oadmin_password");
        return overrides;
    }

    @Value("${apiManagerHost}")
    private String host;

    @Value("${apiManagerPort}")
    private int port;

    /**
     * Makes the environment variables win over the defaults of citrus-global-variables.properties for every
     * Spring @Value in the test suite. Runs before any bean is created, so the placeholders resolve against it.
     */
    @Override
    public void setEnvironment(Environment environment) {
        if (!(environment instanceof ConfigurableEnvironment)) return;
        Map<String, Object> resolved = new LinkedHashMap<>();
        for (Map.Entry<String, String> override : ENVIRONMENT_OVERRIDES.entrySet()) {
            String value = environment.getProperty(override.getValue());
            if (value != null && !value.isEmpty()) {
                resolved.put(override.getKey(), value);
            }
        }
        if (!resolved.isEmpty()) {
            ((ConfigurableEnvironment) environment).getPropertySources()
                .addFirst(new MapPropertySource("apimCliTestEnvironmentOverrides", resolved));
        }
    }

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
            TlsSocketStrategy tlsSocketStrategy = new DefaultClientTlsStrategy(
                sslContext, HostnameVerificationPolicy.CLIENT, NoopHostnameVerifier.INSTANCE);
            PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsSocketStrategy)
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
        return new GlobalVariables();
    }

    @Bean
    public GlobalVariablesPropertyLoader globalVariablesPropertyLoader() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.getPropertyFiles().add("classpath:citrus-global-variables.properties");
        return propertyLoader;
    }

    /**
     * The property loader fills the Citrus global variables straight from the file and therefore knows nothing
     * about the environment overrides. This re-reads every variable through the Spring environment, where the
     * overrides registered in {@link #setEnvironment(Environment)} take precedence.
     * Declaring the loader as a parameter guarantees it has already run.
     */
    @Bean
    public GlobalVariableEnvironmentOverrides globalVariableEnvironmentOverrides(GlobalVariables globalVariables,
                                                                                 GlobalVariablesPropertyLoader propertyLoader,
                                                                                 Environment environment) {
        return new GlobalVariableEnvironmentOverrides(globalVariables, environment);
    }

    public static class GlobalVariableEnvironmentOverrides {

        GlobalVariableEnvironmentOverrides(GlobalVariables globalVariables, Environment environment) {
            for (String name : new ArrayList<>(globalVariables.getVariables().keySet())) {
                String resolved = environment.getProperty(name);
                if (resolved != null) {
                    globalVariables.getVariables().put(name, resolved);
                }
            }
        }
    }
}
