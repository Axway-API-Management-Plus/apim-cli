package com.axway.apim.apiimport.actions;

import com.axway.apim.api.model.APIMethod;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.util.List;

public class ManageApiMethodsTest {

    @Test
    public void isMethodMismatchActualApi() throws JsonProcessingException {
        ManageApiMethods manageApiMethods = new ManageApiMethods();
        ObjectMapper objectMapper = new ObjectMapper();
        String actual = "[\n" +
                "  {\n" +
                "    \"name\": \"getUserByName\",\n" +
                "    \"summary\": \"Get user by user name\",\n" +
                "    \"descriptionType\": \"original\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"updateUser\",\n" +
                "    \"summary\": \"Update user\",\n" +
                "    \"descriptionType\": \"original\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  }\n" +
                "]";
        TypeReference<List<APIMethod>> apiMethodsTypeRef = new TypeReference<List<APIMethod>>() {};
        List<APIMethod> apiMethods = objectMapper.readValue(actual, apiMethodsTypeRef);

        String desired = "[\n" +
                "  {\n" +
                "    \"name\": \"getUserByName\",\n" +
                "    \"summary\": \"Get user by user name\",\n" +
                "    \"descriptionType\": \"original\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  }\n" +
                "]";
        List<APIMethod> apiMethodsDesired = objectMapper.readValue(desired, apiMethodsTypeRef);
        manageApiMethods.isMethodMismatch(apiMethods, apiMethodsDesired);
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "API Method mismatch")
    public void desiredMethodMismatchWithActualMethod() throws JsonProcessingException {
        ManageApiMethods manageApiMethods = new ManageApiMethods();
        ObjectMapper objectMapper = new ObjectMapper();
        String actual = "[\n" +
                "  {\n" +
                "    \"name\": \"getUserByName\",\n" +
                "    \"summary\": \"Get user by user name\",\n" +
                "    \"descriptionType\": \"original\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"updateUser\",\n" +
                "    \"summary\": \"Update user\",\n" +
                "    \"descriptionType\": \"original\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  }\n" +
                "]";
        TypeReference<List<APIMethod>> apiMethodsTypeRef = new TypeReference<List<APIMethod>>() {};
        List<APIMethod> apiMethods = objectMapper.readValue(actual, apiMethodsTypeRef);
        String desired = "[\n" +
                "  {\n" +
                "    \"name\": \"getUserByNames\",\n" +
                "    \"summary\": \"Get user by user names\",\n" +
                "    \"descriptionType\": \"original\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  }\n" +
                "]";
        List<APIMethod> apiMethodsDesired = objectMapper.readValue(desired, apiMethodsTypeRef);
        manageApiMethods.isMethodMismatch(apiMethods, apiMethodsDesired);
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "API Methods mismatch")
    public void ActualMethodMismatchWithDesiredMethod() throws JsonProcessingException {
        ManageApiMethods manageApiMethods = new ManageApiMethods();
        ObjectMapper objectMapper = new ObjectMapper();
        String actual = "[\n" +
                "  {\n" +
                "    \"name\": \"getUserByName\",\n" +
                "    \"summary\": \"Get user by user name\",\n" +
                "    \"descriptionType\": \"original\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  }\n" +
                "]";
        TypeReference<List<APIMethod>> apiMethodsTypeRef = new TypeReference<List<APIMethod>>() {};
        List<APIMethod> apiMethods = objectMapper.readValue(actual, apiMethodsTypeRef);
        String desired = "[\n" +
                "  {\n" +
                "    \"name\": \"getUserByName\",\n" +
                "    \"summary\": \"Get user by user name\",\n" +
                "    \"descriptionType\": \"original\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"updateUser\",\n" +
                "    \"summary\": \"Update user\",\n" +
                "    \"descriptionType\": \"original\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  }\n" +
                "]";

        List<APIMethod> apiMethodsDesired = objectMapper.readValue(desired, apiMethodsTypeRef);
        manageApiMethods.isMethodMismatch(apiMethods, apiMethodsDesired);
    }
}
