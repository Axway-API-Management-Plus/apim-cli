package com.axway.apim.config;

import com.axway.apim.api.API;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(7070);

            HttpConfiguration http_config = new HttpConfiguration();
            http_config.setSecureScheme("https");
            http_config.setSecurePort(8443);
            http_config.setOutputBufferSize(32768);

            HttpConfiguration https_config = new HttpConfiguration(http_config);
            SecureRequestCustomizer src = new SecureRequestCustomizer();
            src.setStsMaxAge(2000);
            src.setStsIncludeSubDomains(true);
            https_config.addCustomizer(src);

            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath(resourcePath + File.separator + "test.keystore");
            sslContextFactory.setKeyStorePassword("changeit");
            sslContextFactory.setKeyManagerPassword("changeit");

            ServerConnector https = new ServerConnector(server,
                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                    new HttpConnectionFactory(https_config));
            https.setPort(8443);
            https.setIdleTimeout(500000);

            server.setConnectors(new Connector[] { connector, https });
            ServletHandler handler = new ServletHandler();
            server.setHandler(handler);
            handler.addServletWithMapping(OpenApiServlet.class, "/openapi.json");
            try {
                server.start();
                server.join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Thread.sleep(1000);
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
        generateTemplate.downloadCertificates(api, "config.json", "https://localhost:8443");
        Assert.assertEquals(1, api.getCaCerts().size());
    }

    @Test
    public void testGenerateAPIConfigWithHttpEndpoint() throws FileNotFoundException {
        String[] args = {"template", "generate", "-c", "api-config.json", "-a", "http://localhost:7070/openapi.json", "-apimCLIHome", apimCliHome, "-backendAuthType", "apikey", "-frontendAuthType", "apikey"};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(new FileInputStream("api-config.json"));
        Assert.assertEquals("Swagger Petstore - OpenAPI 3.0", documentContext.read("$.name"));
        Assert.assertEquals("published", documentContext.read("$.state"));
        Assert.assertEquals("/api/v3", documentContext.read("$.path"));
    }

    @Test
    public void testGenerateAPIConfigWithHttpsEndpoint() throws FileNotFoundException {
        String[] args = {"template", "generate", "-c", "api-config.json", "-a", "https://localhost:8443/openapi.json", "-apimCLIHome", apimCliHome, "-backendAuthType", "apikey", "-frontendAuthType", "apikey"};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(new FileInputStream("api-config.json"));
        Assert.assertEquals("Swagger Petstore - OpenAPI 3.0", documentContext.read("$.name"));
        Assert.assertEquals("published", documentContext.read("$.state"));
        Assert.assertEquals("/api/v3", documentContext.read("$.path"));
    }

    @Test
    public void testLocalApiSpecYaml() throws FileNotFoundException {
        String[] args = {"template", "generate", "-c", "api-config.json", "-a", "http://localhost:7070/openapi.json", "-apimCLIHome", apimCliHome, "-backendAuthType", "apikey", "-frontendAuthType", "apikey", "-o", "yaml"};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(new FileInputStream("api-config.json"));
        Assert.assertEquals("Swagger Petstore - OpenAPI 3.0", documentContext.read("$.name"));
        Assert.assertEquals("published", documentContext.read("$.state"));
        Assert.assertEquals("/api/v3", documentContext.read("$.path"));
        Assert.assertEquals(new File("openapi.yaml").exists(), true);
    }

    @Test
    public void testLocalApiSpecJsonWithHttps() throws FileNotFoundException {

        String[] args = {"template", "generate", "-c", "api-config.json", "-a", "https://localhost:8443/openapi.json", "-apimCLIHome", apimCliHome, "-backendAuthType", "apikey", "-frontendAuthType", "apikey", "-o", "json"};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(new FileInputStream("api-config.json"));
        Assert.assertEquals("Swagger Petstore - OpenAPI 3.0", documentContext.read("$.name"));
        Assert.assertEquals("published", documentContext.read("$.state"));
        Assert.assertEquals("/api/v3", documentContext.read("$.path"));
       // Assert.assertEquals(new File("openapi.json").exists(), true);
    }
}
