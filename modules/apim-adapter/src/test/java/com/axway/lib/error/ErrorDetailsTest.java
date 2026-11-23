package com.axway.lib.error;

import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.error.ErrorCode;
import com.axway.apim.lib.error.ErrorDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class ErrorDetailsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void simpleError() throws IOException {
        AppException exception = new AppException("A breaking change", ErrorCode.BREAKING_CHANGE_DETECTED);
        String json = ErrorDetails.toJson(exception, ErrorCode.NO_CHANGE);

        Assert.assertFalse(json.contains("\n"), "Must be a single line");
        JsonNode node = mapper.readTree(json);
        Assert.assertEquals(node.get("errorCode").asInt(), 15);
        Assert.assertEquals(node.get("error").asText(), "BREAKING_CHANGE_DETECTED");
        Assert.assertEquals(node.get("mappedErrorCode").asInt(), 10);
        Assert.assertEquals(node.get("message").asText(), "A breaking change");
        Assert.assertEquals(node.get("details").size(), 0);
    }

    @Test
    public void causesAndSecondMessageAreDetails() throws IOException {
        Exception root = new IllegalStateException("Connection refused");
        AppException inner = new AppException("Can't read the API", "Second message", ErrorCode.UNKNOWN_API, root);
        AppException exception = new AppException("Error updating API", ErrorCode.CANT_UPDATE_API_PROXY, inner);
        JsonNode node = mapper.readTree(ErrorDetails.toJson(exception, ErrorCode.UNKNOWN_API));

        // The error of the cause is the error of the exception
        Assert.assertEquals(node.get("errorCode").asInt(), ErrorCode.UNKNOWN_API.getCode());
        Assert.assertEquals(node.get("message").asText(), "Error updating API");
        JsonNode details = node.get("details");
        Assert.assertEquals(details.size(), 3, "Details: " + details);
        Assert.assertEquals(details.get(0).asText(), "Can't read the API");
        Assert.assertEquals(details.get(1).asText(), "Second message");
        Assert.assertEquals(details.get(2).asText(), "Connection refused");
    }

    @Test
    public void specialCharactersAreEscaped() throws IOException {
        AppException exception = new AppException("Invalid \"value\"\nin line 2", ErrorCode.UNXPECTED_ERROR);
        String json = ErrorDetails.toJson(exception, ErrorCode.UNXPECTED_ERROR);
        Assert.assertFalse(json.contains("\n"), "Must be a single line");
        Assert.assertEquals(mapper.readTree(json).get("message").asText(), "Invalid \"value\"\nin line 2");
    }
}
