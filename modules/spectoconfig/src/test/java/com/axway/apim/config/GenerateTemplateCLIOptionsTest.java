package com.axway.apim.config;

import com.axway.apim.config.model.GenerateTemplateParameters;
import com.axway.apim.lib.CLIOptions;
import com.axway.apim.lib.error.AppException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GenerateTemplateCLIOptionsTest {

    private String apimCliHome;
    private String openApiLocation;

    @BeforeClass
    private void init() throws  URISyntaxException {
        URI uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        Path path = Paths.get(uri);
        openApiLocation = path + File.separator  + "openapi.json";
        apimCliHome =  path + File.separator + "apimcli";
    }

    @Test
    public void testAppImportParameters() throws AppException {
        String[] args = {"-c", "myOrgConfig.json", "-a", openApiLocation, "-apimCLIHome", apimCliHome, "-backendAuthType", "oauth", "-frontendAuthType", "oauth"};
        CLIOptions options = GenerateTemplateCLIOptions.create(args);
        GenerateTemplateParameters params = (GenerateTemplateParameters) options.getParams();
        // Validate core parameters are included
        Assert.assertEquals(params.getConfig(), "myOrgConfig.json");
        // Validate App-Import parameters
        Assert.assertEquals(params.getApiDefinition(), openApiLocation);
        Assert.assertEquals(params.getBackendAuthType(), "oauth");
        Assert.assertEquals(params.getFrontendAuthType(), "oauth");

    }

    @Test
    public void testGenerateAPIConfig() throws IOException {
        String[] args = {"template", "generate", "-c", "test/api-config.json", "-a", openApiLocation, "-apimCLIHome", apimCliHome};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(Files.newInputStream(Paths.get("test/api-config.json")));

        Assert.assertEquals("Swagger Petstore - OpenAPI 3.0", documentContext.read("$.name"));
        Assert.assertEquals("published", documentContext.read("$.state"));
        Assert.assertEquals("/api/v3", documentContext.read("$.path"));
        Assert.assertEquals("1.0.11", documentContext.read("$.version"));
        Assert.assertEquals("API Development", documentContext.read("$.organization"));

        Assert.assertEquals(openApiLocation, documentContext.read("$.apiSpecification.resource"));
        Assert.assertEquals("https://localhost", documentContext.read("$.backendBasepath"));

        Assert.assertEquals("Everything about your Pets", documentContext.read("$.tags.pet[0]"));
        Assert.assertEquals("Access to Petstore orders", documentContext.read("$.tags.store[0]"));
        Assert.assertEquals("Operations about user", documentContext.read("$.tags.user[0]"));

        Assert.assertEquals("_default", documentContext.read("$.corsProfiles[0].name"));
        Assert.assertEquals("*", documentContext.read("$.corsProfiles[0].origins[0]"));
        Assert.assertTrue(documentContext.read("$.corsProfiles[0].isDefault", Boolean.class));
        //Assert.assertEquals("Authorization", documentContext.read("$.corsProfiles[0].allowedHeaders[0]"));
        Assert.assertEquals("X-CorrelationID", documentContext.read("$.corsProfiles[0].exposedHeaders[0]"));
        Assert.assertFalse(documentContext.read("$.corsProfiles[0].supportCredentials", Boolean.class));
        Assert.assertEquals(0, documentContext.read("$.corsProfiles[0].maxAgeSeconds", Integer.class).intValue());
    }


    @Test
    public void testGenerateAPIConfigWithFrontendApikey() throws IOException {
        String[] args = {"template", "generate", "-c", "test/api-config.json", "-a", openApiLocation, "-apimCLIHome", apimCliHome, "-backendAuthType", "apiKey", "-frontendAuthType", "apiKey"};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(Files.newInputStream(Paths.get("test/api-config.json")));

        Assert.assertEquals("apiKey", documentContext.read("$.securityProfiles[0].devices[0].type"));
        Assert.assertEquals("_default", documentContext.read("$.securityProfiles[0].devices[0].name"));
        Assert.assertEquals(1, documentContext.read("$.securityProfiles[0].devices[0].order", Integer.class).intValue());
        Assert.assertEquals("KeyId", documentContext.read("$.securityProfiles[0].devices[0].properties.apiKeyFieldName"));
        Assert.assertEquals("HEADER", documentContext.read("$.securityProfiles[0].devices[0].properties.takeFrom"));
        Assert.assertEquals("true", documentContext.read("$.securityProfiles[0].devices[0].properties.removeCredentialsOnSuccess"));

    }

    @Test
    public void testGenerateAPIConfigWithFrontendOauth() throws IOException {
        String[] args = {"template", "generate", "-c", "test/api-config.json", "-a", openApiLocation, "-apimCLIHome", apimCliHome, "-backendAuthType", "apiKey", "-frontendAuthType", "oauth"};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(Files.newInputStream(Paths.get("test/api-config.json")));
        Assert.assertEquals("oauth", documentContext.read("$.securityProfiles[0].devices[0].type"));
        Assert.assertEquals("_default", documentContext.read("$.securityProfiles[0].devices[0].name"));
        Assert.assertEquals(1, documentContext.read("$.securityProfiles[0].devices[0].order", Integer.class).intValue());
        Assert.assertEquals("OAuth Access Token Store", documentContext.read("$.securityProfiles[0].devices[0].properties.tokenStore"));
        Assert.assertEquals("HEADER", documentContext.read("$.securityProfiles[0].devices[0].properties.accessTokenLocation"));
        Assert.assertEquals("Bearer", documentContext.read("$.securityProfiles[0].devices[0].properties.authorizationHeaderPrefix"));
        //Assert.assertEquals("", documentContext.read("$.securityProfiles.devices[0].properties.accessTokenLocationQueryString"));
        Assert.assertEquals("All", documentContext.read("$.securityProfiles[0].devices[0].properties.scopesMustMatch"));
        Assert.assertEquals("resource.WRITE, resource.READ", documentContext.read("$.securityProfiles[0].devices[0].properties.scopes"));
        Assert.assertEquals("true", documentContext.read("$.securityProfiles[0].devices[0].properties.removeCredentialsOnSuccess"));
        Assert.assertEquals("true", documentContext.read("$.securityProfiles[0].devices[0].properties.implicitGrantEnabled"));
        Assert.assertEquals("https://localhost:8089/api/oauth/authorize", documentContext.read("$.securityProfiles[0].devices[0].properties.implicitGrantLoginEndpointUrl"));
        Assert.assertEquals("access_token", documentContext.read("$.securityProfiles[0].devices[0].properties.implicitGrantLoginTokenName"));
        Assert.assertEquals("true", documentContext.read("$.securityProfiles[0].devices[0].properties.authCodeGrantTypeEnabled"));
        Assert.assertEquals("https://localhost:8089/api/oauth/authorize", documentContext.read("$.securityProfiles[0].devices[0].properties.authCodeGrantTypeRequestEndpointUrl"));
        Assert.assertEquals("client_id", documentContext.read("$.securityProfiles[0].devices[0].properties.authCodeGrantTypeRequestClientIdName"));
        Assert.assertEquals("client_secret", documentContext.read("$.securityProfiles[0].devices[0].properties.authCodeGrantTypeRequestSecretName"));
        Assert.assertEquals("https://localhost:8089/api/oauth/token", documentContext.read("$.securityProfiles[0].devices[0].properties.authCodeGrantTypeTokenEndpointUrl"));
        Assert.assertEquals("access_code", documentContext.read("$.securityProfiles[0].devices[0].properties.authCodeGrantTypeTokenEndpointTokenName"));


    }

    @Test
    public void testGenerateAPIConfigWithFrontendExternalOauth() throws IOException {
        String[] args = {"template", "generate", "-c", "test/api-config.json", "-a", openApiLocation, "-apimCLIHome", apimCliHome, "-backendAuthType", "apiKey", "-frontendAuthType", "oauth-external"};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(Files.newInputStream(Paths.get("test/api-config.json")));
        Assert.assertEquals("oauthExternal", documentContext.read("$.securityProfiles[0].devices[0].type"));
        Assert.assertEquals("_default", documentContext.read("$.securityProfiles[0].devices[0].name"));
        Assert.assertEquals(1, documentContext.read("$.securityProfiles[0].devices[0].order", Integer.class).intValue());
        Assert.assertEquals("Tokeninfo policy 1", documentContext.read("$.securityProfiles[0].devices[0].properties.tokenStore"));
        Assert.assertEquals("HEADER", documentContext.read("$.securityProfiles[0].devices[0].properties.accessTokenLocation"));
        Assert.assertEquals("Bearer", documentContext.read("$.securityProfiles[0].devices[0].properties.authorizationHeaderPrefix"));
        //Assert.assertEquals("", documentContext.read("$.securityProfiles.devices[0].properties.accessTokenLocationQueryString"));
        Assert.assertEquals("All", documentContext.read("$.securityProfiles[0].devices[0].properties.scopesMustMatch"));
        Assert.assertEquals("resource.WRITE, resource.READ", documentContext.read("$.securityProfiles[0].devices[0].properties.scopes"));
        Assert.assertEquals("true", documentContext.read("$.securityProfiles[0].devices[0].properties.removeCredentialsOnSuccess"));
        Assert.assertEquals("true", documentContext.read("$.securityProfiles[0].devices[0].properties.implicitGrantEnabled"));
        Assert.assertEquals("true", documentContext.read("$.securityProfiles[0].devices[0].properties.useClientRegistry"));

        Assert.assertEquals("${oauth.token.client_id}", documentContext.read("$.securityProfiles[0].devices[0].properties.subjectSelector"));
        Assert.assertEquals("https://localhost:8089/api/oauth/authorize", documentContext.read("$.securityProfiles[0].devices[0].properties.implicitGrantLoginEndpointUrl"));
        Assert.assertEquals("access_token", documentContext.read("$.securityProfiles[0].devices[0].properties.implicitGrantLoginTokenName"));
        Assert.assertEquals("true", documentContext.read("$.securityProfiles[0].devices[0].properties.authCodeGrantTypeEnabled"));

        Assert.assertEquals("https://localhost:8089/api/oauth/authorize", documentContext.read("$.securityProfiles[0].devices[0].properties.authCodeGrantTypeRequestEndpointUrl"));
        Assert.assertEquals("client_id", documentContext.read("$.securityProfiles[0].devices[0].properties.authCodeGrantTypeRequestClientIdName"));
        Assert.assertEquals("client_secret", documentContext.read("$.securityProfiles[0].devices[0].properties.authCodeGrantTypeRequestSecretName"));
        Assert.assertEquals("https://localhost:8089/api/oauth/token", documentContext.read("$.securityProfiles[0].devices[0].properties.authCodeGrantTypeTokenEndpointUrl"));
        Assert.assertEquals("access_code", documentContext.read("$.securityProfiles[0].devices[0].properties.authCodeGrantTypeTokenEndpointTokenName"));

    }

    @Test
    public void testGenerateAPIConfigWithBackendApikey() throws IOException {
        String[] args = {"template", "generate", "-c", "test/api-config.json", "-a", openApiLocation, "-apimCLIHome", apimCliHome, "-backendAuthType", "apiKey", "-frontendAuthType", "apiKey"};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(Files.newInputStream(Paths.get("test/api-config.json")));
        Assert.assertEquals("_default", documentContext.read("$.authenticationProfiles[0].name"));
        Assert.assertEquals("apiKey", documentContext.read("$.authenticationProfiles[0].type"));
        Assert.assertEquals("4249823490238490", documentContext.read("$.authenticationProfiles[0].parameters.apiKey"));
        Assert.assertEquals("KeyId", documentContext.read("$.authenticationProfiles[0].parameters.apiKeyField"));
        Assert.assertEquals("QUERYSTRING_PARAMETER", documentContext.read("$.authenticationProfiles[0].parameters.httpLocation"));
        Assert.assertTrue(documentContext.read("$.authenticationProfiles[0].isDefault", Boolean.class));
    }

    @Test
    public void testGenerateAPIConfigWithBackendOauth() throws IOException {
        String[] args = {"template", "generate", "-c", "test/api-config.json", "-a", openApiLocation, "-apimCLIHome", apimCliHome, "-backendAuthType", "oauth", "-frontendAuthType", "apiKey"};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(Files.newInputStream(Paths.get("test/api-config.json")));
        Assert.assertEquals("_default", documentContext.read("$.authenticationProfiles[0].name"));
        Assert.assertEquals("oauth", documentContext.read("$.authenticationProfiles[0].type"));
        Assert.assertEquals("<Name-of-configured-OAuth-Profile>", documentContext.read("$.authenticationProfiles[0].parameters.providerProfile"));
        Assert.assertEquals("${authentication.subject.id}", documentContext.read("$.authenticationProfiles[0].parameters.ownerId"));
        Assert.assertTrue(documentContext.read("$.authenticationProfiles[0].isDefault", Boolean.class));
    }

    @Test
    public void testGenerateAPIConfigWithBackendSSL() throws IOException {
        String[] args = {"template", "generate", "-c", "test/api-config.json", "-a", openApiLocation, "-apimCLIHome", apimCliHome, "-backendAuthType", "ssl", "-frontendAuthType", "apiKey"};
        GenerateTemplate.generate(args);
        DocumentContext documentContext = JsonPath.parse(Files.newInputStream(Paths.get("test/api-config.json")));
        Assert.assertEquals("_default", documentContext.read("$.authenticationProfiles[0].name"));
        Assert.assertEquals("ssl", documentContext.read("$.authenticationProfiles[0].type"));
        Assert.assertEquals("file", documentContext.read("$.authenticationProfiles[0].parameters.source"));
        Assert.assertEquals("../certificates/clientcert.pfx", documentContext.read("$.authenticationProfiles[0].parameters.certFile"));
        Assert.assertEquals("********", documentContext.read("$.authenticationProfiles[0].parameters.password"));
        Assert.assertTrue(documentContext.read("$.authenticationProfiles[0].parameters.trustAll", Boolean.class));
        Assert.assertTrue(documentContext.read("$.authenticationProfiles[0].isDefault", Boolean.class));
    }

    @Test
    public void printUsage() throws AppException {
        PrintStream old = System.out;
        String[] args = {"template", "generate", "-c", "test/api-config.json", "-a", openApiLocation, "-apimCLIHome", apimCliHome, "-backendAuthType", "ssl", "-frontendAuthType", "apiKey"};
        CLIOptions options = GenerateTemplateCLIOptions.create(args);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
        options.printUsage("test", args);
        System.setOut(old);
        Assert.assertTrue(byteArrayOutputStream.toString().contains("generate"));
    }
}
