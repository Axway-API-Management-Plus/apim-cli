package com.axway.apim.test;

import com.axway.apim.EndpointConfig;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.citrusframework.variable.GlobalVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Runs once before the whole integration-test suite to make sure the organizations, the org-admin
 * user and the test application required by the other *IT tests exist in the API-Manager, storing
 * the resolved/created IDs in {@link GlobalVariables} so they can be referenced as {@code ${orgId}}
 * etc. by the other tests.
 */
@ContextConfiguration(classes = {EndpointConfig.class})
public class CoreInitializationTestIT extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(CoreInitializationTestIT.class);

    @Autowired
    HttpClient httpClient;

    @Value("${apiManagerHost}")
    private String host;

    @Value("${apiManagerPort}")
    private int port;

    @Value("${apiManagerUser}")
    private String username;

    @Value("${apiManagerPass}")
    private String password;

    @Value("${oadminPassword1}")
    private String orgAdminPassword;

    @Value("${oadminUsername1}")
    private String orgAdminUsername;

    @Autowired
    GlobalVariables globalVariables;

    private static final String DEFAULT_PASSWORD = "changeme";

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        String format = username + ":" + DEFAULT_PASSWORD;
        String authorizationHeaderValue = "Basic " + Base64.getEncoder().encodeToString(format.getBytes());
        String url = "https://" + host + ":" + port + "/api/portal/v1.4";

        try {
            if (System.getenv("reset_password") != null && System.getenv("reset_password").equalsIgnoreCase("true")) {
                LOG.info("Change password of user for initial setup");
                postRequest(url + "/currentuser/changepassword", authorizationHeaderValue, password);
                format = username + ":" + password;
                authorizationHeaderValue = "Basic " + Base64.getEncoder().encodeToString(format.getBytes());
            }

            String orgNumber = (String) globalVariables.getVariables().get("orgNumber");
            String orgName = (String) globalVariables.getVariables().get("orgName");
            String orgId = resolveOrCreateOrganization(url, authorizationHeaderValue, orgName,
                "Test Org " + orgNumber);
            globalVariables.getVariables().put("orgId", orgId);

            LOG.info("Creating second organization");
            String orgName2 = (String) globalVariables.getVariables().get("orgName2");
            String orgId2 = resolveOrCreateOrganization(url, authorizationHeaderValue, orgName2, "Test Org 2");
            globalVariables.getVariables().put("orgId2", orgId2);

            LOG.info("Creating third organization");
            String orgName3 = (String) globalVariables.getVariables().get("orgName3");
            String orgId3 = resolveOrCreateOrganization(url, authorizationHeaderValue, orgName3, "Test Org 3");
            globalVariables.getVariables().put("orgId3", orgId3);

            String userName = (String) globalVariables.getVariables().get("oadminUsername1");
            String response = getRequest(url + "/users?field=loginName&op=eq&value=" + userName, authorizationHeaderValue);
            String oadminUserId1;
            if (!response.equals("[]")) {
                LOG.info("User Already exists");
                oadminUserId1 = JsonPath.parse(response).read("$.[0].id");
            } else {
                LOG.info("Creating oadmin user {}", orgAdminUsername);
                String userPayload = "{\"enabled\":true,\"loginName\":\"" + orgAdminUsername + "\",\"name\":\"Anna Owen " + orgNumber + "\","
                    + "\"email\":\"anna-" + orgNumber + "@axway.com\",\"role\":\"oadmin\",\"organizationId\":\"" + orgId + "\"}";
                String userResponse = createEntity(url + "/users", authorizationHeaderValue, userPayload, 201);
                DocumentContext userDocument = JsonPath.parse(userResponse);
                oadminUserId1 = userDocument.read("$.id");

                LOG.info("Updating password for oadmin user {}", orgAdminUsername);
                postRequest(url + "/users/" + oadminUserId1 + "/changepassword/", authorizationHeaderValue, DEFAULT_PASSWORD);
                String orgAdminFormat = orgAdminUsername + ":" + DEFAULT_PASSWORD;
                String orgAdminAuthorizationHeaderValue = "Basic " + Base64.getEncoder().encodeToString(orgAdminFormat.getBytes());
                postRequest(url + "/currentuser/changepassword", orgAdminAuthorizationHeaderValue, orgAdminPassword);
            }
            globalVariables.getVariables().put("oadminUserId1", oadminUserId1);

            String appName = (String) globalVariables.getVariables().get("testAppName");
            response = getRequest(url + "/applications?field=name&op=eq&value=" + appName, authorizationHeaderValue);
            String testAppId;
            if (!response.equals("[]")) {
                LOG.info("Application Already exists");
                testAppId = JsonPath.parse(response).read("$.[0].id");
            } else {
                String appPayload = "{\"name\":\"" + appName + "\",\"apis\":[],\"organizationId\":\"" + orgId + "\"}";
                String appResponse = createEntity(url + "/applications", authorizationHeaderValue, appPayload, 201);
                testAppId = JsonPath.parse(appResponse).read("$.id");
                LOG.info("Created a application: '{}' ID: '{}'", appName, testAppId);
            }
            globalVariables.getVariables().put("testAppId", testAppId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Looks up an organization by name and returns its ID, creating it first if it doesn't exist yet.
     */
    private String resolveOrCreateOrganization(String url, String authorizationHeaderValue, String orgName, String description) throws IOException, ParseException, URISyntaxException {
        String encodedOrgName = URLEncoder.encode(orgName, StandardCharsets.UTF_8);
        String response = getRequest(url + "/organizations?field=name&op=eq&value=" + encodedOrgName, authorizationHeaderValue);
        if (!response.equals("[]")) {
            LOG.info("Organization {} already exists", orgName);
            return JsonPath.parse(response).read("$.[0].id");
        }
        LOG.info("Creating Organization {}", orgName);
        String payload = "{\"name\": \"" + orgName + "\", \"description\": \"" + description + "\", \"enabled\": true, \"development\": true }";
        String orgResponse = createEntity(url + "/organizations", authorizationHeaderValue, payload, 201);
        return JsonPath.parse(orgResponse).read("$.id");
    }

    private String createEntity(String url, String authorizationHeaderValue, String jsonPayload, int expectedStatusCode) throws IOException, ParseException, URISyntaxException {
        URI uri = new URIBuilder(url).build();
        HttpEntity entity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);
        HttpPost post = new HttpPost(uri);
        post.setEntity(entity);
        post.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
        try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(post)) {
            String body = EntityUtils.toString(response.getEntity());
            if (response.getCode() != expectedStatusCode) {
                throw new RuntimeException("Error creating entity at " + url + ". Response-Code: " + response.getCode() + " Response Body: " + body);
            }
            return body;
        }
    }

    public String getRequest(String url, String authorizationHeaderValue) throws IOException, ParseException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
        try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    public void postRequest(String url, String authorizationHeaderValue, String newPassword) throws URISyntaxException, IOException, ParseException {
        URI uri = new URIBuilder(url).build();
        HttpEntity entity = new StringEntity("newPassword=" + newPassword + "&oldPassword=" + DEFAULT_PASSWORD, ContentType.APPLICATION_FORM_URLENCODED);
        HttpPost post = new HttpPost(uri);
        post.setEntity(entity);
        post.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);

        try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(post)) {
            int statusCode = response.getCode();
            if (statusCode != 204) {
                throw new RuntimeException("Error changing password of user. Response-Code: " + EntityUtils.toString(response.getEntity()));
            }
        }
    }
}
