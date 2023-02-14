package com.axway.lib;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

@Test
public class TestIT extends TestNGCitrusTestRunner {

    @Autowired
    private HttpClient apiManagerClient;

    @CitrusTest
    public void okTest() {

        createVariable("orgNumber", RandomNumberFunction.getRandomNumber(4, true));
        createVariable("orgName", "API Development ${orgNumber}");

        http(action -> action.client(apiManagerClient)
                .send()
                .post("/organizations")
                .name("orgCreatedRequest")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .payload("{\"name\": \"${orgName}\", \"description\": \"Test Org ${orgNumber}\", \"enabled\": true, \"development\": true }"));

        echo("####### Validate Test-Organisation: ${orgName} has been created #######");
        http(action -> action.client(apiManagerClient)
                .receive()
                .response(HttpStatus.CREATED)
                .messageType(MessageType.JSON)
                .validate("$.name", "${orgName}")
                .extractFromPayload("$.id", "orgId"));
    }
}
