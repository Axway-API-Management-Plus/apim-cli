package com.axway.apim.config;

import com.axway.apim.adapter.jackson.CustomYamlFactory;
import com.axway.apim.api.API;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.util.concurrent.Executors;

public class GenerateTemplateTest {

    private String apimCliHome;
    private String resourcePath;

    @BeforeClass
    private void init() throws URISyntaxException {
        URI uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        resourcePath = Paths.get(uri).toString();
        apimCliHome =  resourcePath + File.separator + "apimcli";

    }

    Server server = new Server();
    @BeforeClass
    public void start() throws InterruptedException {
        Executors.newSingleThreadExecutor().execute(() -> {
            try(ServerConnector connector = new ServerConnector(server)) {
                connector.setPort(7070);
                HttpConfiguration https_config = getHttpConfiguration();
                SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
                sslContextFactory.setKeyStorePath(resourcePath + File.separator + "keystore.jks");
                sslContextFactory.setKeyStorePassword("changeit");
                sslContextFactory.setKeyManagerPassword("changeit");
                try (ServerConnector https = new ServerConnector(server,
                        new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                        new HttpConnectionFactory(https_config))) {

                    https.setPort(8443);
                    https.setHost("0.0.0.0");
                    https.setIdleTimeout(500000);
                    server.setConnectors(new Connector[]{connector, https});
                    ServletHandler handler = new ServletHandler();
                    server.setHandler(handler);
                    handler.addServletWithMapping(OpenApiServlet.class, "/openapi.json");
                    try {
                        server.start();
                        server.join();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        Thread.sleep(1000);
    }

    private static HttpConfiguration getHttpConfiguration() {
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(8443);
        http_config.setOutputBufferSize(32768);
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        SecureRequestCustomizer src = new SecureRequestCustomizer();
        src.setStsMaxAge(2000);
        src.setStsIncludeSubDomains(true);
        https_config.addCustomizer(src);
        return https_config;
    }

    @AfterClass
    public void stop() throws Exception {
        server.stop();
    }
    @Test
    public void testDownloadCertificates() throws IOException, CertificateEncodingException, NoSuchAlgorithmException, KeyManagementException {
        HttpsURLConnection.setDefaultHostnameVerifier ((hostname, session) -> true);
        GenerateTemplate generateTemplate = new GenerateTemplate();
        API api = new API();
        String filePath = generateTemplate.downloadCertificatesAndContent(api, "config.json", "https://localhost:8443/openapi.json");
        Assert.assertNotNull(filePath);
        Assert.assertEquals(1, api.getCaCerts().size());
    }

    @Test
    public void testDownloadContent() throws IOException {
        GenerateTemplate generateTemplate = new GenerateTemplate();
        String filePath = generateTemplate.downloadContent( "config.json", "http://localhost:7070/openapi.json");
        Assert.assertNotNull(filePath);
    }

    @Test
    public void testGenerateAPIConfigWithHttpEndpoint() throws IOException {
        String[] args = {"template", "generate", "-c", "api-config.json", "-a", "http://localhost:7070/openapi.json", "-apimCLIHome", apimCliHome, "-backendAuthType", "apiKey", "-frontendAuthType", "apiKey"};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(Files.newInputStream(Paths.get("api-config.json")));
        Assert.assertEquals("Swagger Petstore - OpenAPI 3.0", documentContext.read("$.name"));
        Assert.assertEquals("published", documentContext.read("$.state"));
        Assert.assertEquals("/api/v3", documentContext.read("$.path"));
        Assert.assertEquals("apiKey", documentContext.read("$.securityProfiles[0].devices[0].type"));
        Assert.assertEquals("apiKey", documentContext.read("$.authenticationProfiles[0].type"));

    }

    @Test
    public void testGenerateAPIConfigWithHttpsEndpoint() throws IOException {
        String[] args = {"template", "generate", "-c", "api-config.json", "-a", "https://localhost:8443/openapi.json", "-apimCLIHome", apimCliHome, "-backendAuthType", "apiKey", "-frontendAuthType", "apiKey"};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(Files.newInputStream(Paths.get("api-config.json")));
        Assert.assertEquals("Swagger Petstore - OpenAPI 3.0", documentContext.read("$.name"));
        Assert.assertEquals("published", documentContext.read("$.state"));
        Assert.assertEquals("/api/v3", documentContext.read("$.path"));
    }

    @Test
    public void testLocalApiSpecYaml() throws IOException {
        String[] args = {"template", "generate", "-c", "api-config.yaml", "-a", "http://localhost:7070/openapi.json", "-apimCLIHome", apimCliHome, "-backendAuthType", "apiKey", "-frontendAuthType", "apiKey", "-o", "yaml"};
        GenerateTemplate.generate(args);
        ObjectMapper objectMapperYaml = new ObjectMapper(CustomYamlFactory.createYamlFactory());
        JsonNode jsonNode = objectMapperYaml.readTree(Files.newInputStream(Paths.get("api-config.yaml")));
        ObjectMapper objectMapper = new ObjectMapper();
        DocumentContext documentContext = JsonPath.parse(objectMapper.writeValueAsString(jsonNode));
        Assert.assertEquals("Swagger Petstore - OpenAPI 3.0", documentContext.read("$.name"));
        Assert.assertEquals("published", documentContext.read("$.state"));
        Assert.assertEquals("/api/v3", documentContext.read("$.path"));

    }

    @Test
    public void testLocalApiSpecJsonWithHttps() throws IOException {

        String[] args = {"template", "generate", "-c", "api-config.json", "-a", "https://localhost:8443/openapi.json", "-apimCLIHome", apimCliHome, "-backendAuthType", "apiKey", "-frontendAuthType", "apiKey", "-o", "json"};
        int returnCode = GenerateTemplate.generate(args);
        Assert.assertEquals(returnCode, 0);
        DocumentContext documentContext = JsonPath.parse(Files.newInputStream(Paths.get("api-config.json")));
        Assert.assertEquals("Swagger Petstore - OpenAPI 3.0", documentContext.read("$.name"));
        Assert.assertEquals("published", documentContext.read("$.state"));
        Assert.assertEquals("/api/v3", documentContext.read("$.path"));
    }

    @Test
    public void testWithDefaultFrontendAndBackendAuth() throws IOException {
        String[] args = {"template", "generate", "-c", "api-config.json", "-a", "http://localhost:7070/openapi.json", "-apimCLIHome", apimCliHome};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(Files.newInputStream(Paths.get("api-config.json")));
        Assert.assertEquals("Swagger Petstore - OpenAPI 3.0", documentContext.read("$.name"));
        Assert.assertEquals("published", documentContext.read("$.state"));
        Assert.assertEquals("/api/v3", documentContext.read("$.path"));
        Assert.assertEquals("passThrough", documentContext.read("$.securityProfiles[0].devices[0].type"));
    }

    @Test
    public void testWithFrontendAuthAlternateName() throws IOException {
        String[] args = {"template", "generate", "-c", "api-config.json", "-a", "http://localhost:7070/openapi.json", "-apimCLIHome", apimCliHome, "-frontendAuthType", "passthrough"};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(Files.newInputStream(Paths.get("api-config.json")));
        Assert.assertEquals("Swagger Petstore - OpenAPI 3.0", documentContext.read("$.name"));
        Assert.assertEquals("published", documentContext.read("$.state"));
        Assert.assertEquals("/api/v3", documentContext.read("$.path"));
        Assert.assertEquals("passThrough", documentContext.read("$.securityProfiles[0].devices[0].type"));
    }


}
