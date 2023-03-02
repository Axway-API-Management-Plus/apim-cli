package com.axway.apim.test;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.runner.TestRunner;
import com.consol.citrus.dsl.runner.TestRunnerBeforeSuiteSupport;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.variable.GlobalVariables;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Base64;

public class CoreInitializationTestIT extends TestRunnerBeforeSuiteSupport {

    @Autowired
    HttpClient httpClient;

    @Autowired
    private com.consol.citrus.http.client.HttpClient apiManager;

    @Value("${apiManagerHost}")
    private String host;

    @Value("${apiManagerPort}")
    private int port;

    @Value("${apiManagerUser}")
    private String username;

    @Value("${apiManagerPass}")
    private String password;

    @Autowired
    GlobalVariables globalVariables;


    @Override
    public void beforeSuite(TestRunner testRunner) {
        String format = username + ":" + password;
        String authorizationHeaderValue = "Basic " + Base64.getEncoder().encodeToString(format.getBytes());
        String url = "https://" + host + ":" + port + "/api/portal/v1.4";

        try {
            String orgName = URLEncoder.encode((String) globalVariables.getVariables().get("orgName"), "UTF-8");
            HttpGet httpGet = new HttpGet(url + "/organizations?field=name&op=eq&value=" + orgName);
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String response = EntityUtils.toString(httpResponse.getEntity());
            DocumentContext documentContext = JsonPath.parse(response);
            if (!response.equals("[]")) {
                testRunner.echo("Organization ${orgName} Already exists");
                String orgId = documentContext.read("$.[0].id");
                testRunner.variable("orgId", orgId);
            } else {
                testRunner.echo("Creating Organization ${orgName}");
                testRunner.http(action -> action.client(apiManager)
                        .send()
                        .post("/organizations")
                        .name("orgCreatedRequest")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .payload("{\"name\": \"${orgName}\", \"description\": \"Test Org ${orgNumber}\", \"enabled\": true, \"development\": true }"));
                testRunner.echo("####### Validate Test-Organisation: ${orgName} has been created #######");
                testRunner.http(action -> action.client(apiManager)
                        .receive()
                        .response(HttpStatus.CREATED)
                        .messageType(MessageType.JSON)
                        .validate("$.name", "${orgName}")
                        .extractFromPayload("$.id", "orgId"));
                testRunner.echo("####### Extracted organization id: ${orgId} as attribute: orgId #######");
            }

            testRunner.echo("Creating second organization");
            String orgName2 = URLEncoder.encode((String) globalVariables.getVariables().get("orgName2"), "UTF-8");
            httpGet = new HttpGet(url + "/organizations?field=name&op=eq&value=" + orgName2);
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
            httpResponse = httpClient.execute(httpGet);
            response = EntityUtils.toString(httpResponse.getEntity());

            if (!response.equals("[]")) {
                testRunner.echo("Organization ${orgName2} Already exists");
                documentContext = JsonPath.parse(response);
                String orgId = documentContext.read("$.[0].id");
                testRunner.variable("orgId2", orgId);
            } else {
                testRunner.echo("Creating Organization ${orgName2}");
                testRunner.http(action -> action.client(apiManager)
                        .send()
                        .post("/organizations")
                        .name("orgCreatedRequest")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .payload("{\"name\": \"${orgName2}\", \"description\": \"Test Org 2\", \"enabled\": true, \"development\": true }"));
                testRunner.echo("####### Validate Test-Organisation: ${orgName} has been created #######");
                testRunner.http(action -> action.client(apiManager)
                        .receive()
                        .response(HttpStatus.CREATED)
                        .messageType(MessageType.JSON)
                        .validate("$.name", "${orgName2}")
                        .extractFromPayload("$.id", "orgId2"));
                testRunner.echo("####### Extracted organization id: ${orgId2} as attribute: orgId2 #######");
            }

            testRunner.echo("Creating third organization");
            String orgName3 = URLEncoder.encode((String) globalVariables.getVariables().get("orgName3"), "UTF-8");
            httpGet = new HttpGet(url + "/organizations?field=name&op=eq&value=" + orgName3);
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
            httpResponse = httpClient.execute(httpGet);
            response = EntityUtils.toString(httpResponse.getEntity());

            if (!response.equals("[]")) {
                testRunner.echo("Organization ${orgName3} Already exists");
                documentContext = JsonPath.parse(response);
                String orgId = documentContext.read("$.[0].id");
                testRunner.variable("orgId3", orgId);
            } else {
                testRunner.echo("Creating Organization ${orgName3}");
                testRunner.http(action -> action.client(apiManager)
                        .send()
                        .post("/organizations")
                        .name("orgCreatedRequest")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .payload("{\"name\": \"${orgName3}\", \"description\": \"Test Org 3\", \"enabled\": true, \"development\": true }"));
                testRunner.echo("####### Validate Test-Organisation: ${orgName} has been created #######");
                testRunner.http(action -> action.client(apiManager)
                        .receive()
                        .response(HttpStatus.CREATED)
                        .messageType(MessageType.JSON)
                        .validate("$.name", "${orgName3}")
                        .extractFromPayload("$.id", "orgId3"));
                testRunner.echo("####### Extracted organization id: ${orgId3} as attribute: orgId2 #######");
            }


            String userName = (String) globalVariables.getVariables().get("oadminUsername1");
            httpGet = new HttpGet(url + "/users?field=loginName&op=eq&value=" + userName);
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
            httpResponse = httpClient.execute(httpGet);
            response = EntityUtils.toString(httpResponse.getEntity());
            if (!response.equals("[]")) {
                testRunner.echo("User Already exists");
                documentContext = JsonPath.parse(response);
                String userId = documentContext.read("$.[0].id");
                testRunner.variable("oadminUserId1", userId);
            } else {
                testRunner.echo("Creating oadmin user ${oadminUsername1}");
                testRunner.http(action -> action.client(apiManager)
                        .send()
                        .post("/users")
                        .header("Content-Type", "application/json")
                        .payload("{\"enabled\":true,\"loginName\":\"${oadminUsername1}\",\"name\":\"Anna Owen ${orgNumber}\",\"email\":\"anna-${orgNumber}@axway.com\",\"role\":\"oadmin\",\"organizationId\":\"${orgId}\"}"));
                testRunner.http(action -> action.client(apiManager).receive().response(HttpStatus.CREATED).messageType(MessageType.JSON)
                        .extractFromPayload("$.id", "oadminUserId1")
                        .extractFromPayload("$.loginName", "oadminUsername1"));
            }
            testRunner.echo("Updating password for oadmin user ${oadminUsername1}");
            testRunner.http(action -> action.client(apiManager).send()
                    .post("/users/${oadminUserId1}/changepassword/")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .payload("newPassword=${oadminPassword1}"));

            testRunner.http(action -> action.client(apiManager).receive().response(HttpStatus.NO_CONTENT));

            String appName = (String) globalVariables.getVariables().get("testAppName");
            httpGet = new HttpGet(url + "/applications?field=name&op=eq&value=" + appName);
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
            httpResponse = httpClient.execute(httpGet);
            response = EntityUtils.toString(httpResponse.getEntity());
            if (!response.equals("[]")) {
                documentContext = JsonPath.parse(response);
                testRunner.echo("Application Already exists");
                String testAppId = documentContext.read("$.[0].id");
                testRunner.variable("testAppId", testAppId);

            } else {
                testRunner.http(action -> action.client(apiManager)
                        .send()
                        .post("/applications")
                        .name("orgCreatedRequest")
                        .header("Content-Type", "application/json")
                        .payload("{\"name\":\"${testAppName}\",\"apis\":[],\"organizationId\":\"${orgId}\"}"));

                testRunner.http(action -> action.client(apiManager)
                        .receive()
                        .response(HttpStatus.CREATED)
                        .messageType(MessageType.JSON)
                        .extractFromPayload("$.id", "testAppId")
                        .extractFromPayload("$.name", "testAppName"));
                testRunner.echo("####### Created a application: '${testAppName}' ID: '${testAppId}' (testAppName/testAppId) #######");

            }
            // Adjusting the API-Manager config in preparation to run integration tests
            testRunner.echo("Turn off changePasswordOnFirstLogin and passwordExpiryEnabled validation to run integration tests");
            testRunner.http(action -> action.client(apiManager).send().put("/config").header("Content-Type", "application/json")
                    .payload(new ClassPathResource("/com/axway/apim/test/files/config/apimanager-test-config.json")));
            testRunner.run(new AbstractTestAction() {
                @Override
                public void doExecute(TestContext testContext) {
                    globalVariables.getVariables().put("orgId", testContext.getVariable("orgId"));
                    globalVariables.getVariables().put("orgId2", testContext.getVariable("orgId2"));
                    globalVariables.getVariables().put("orgId3", testContext.getVariable("orgId3"));
                    globalVariables.getVariables().put("testAppId", testContext.getVariable("testAppId"));
                    globalVariables.getVariables().put("oadminUserId1", testContext.getVariable("oadminUserId1"));
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}