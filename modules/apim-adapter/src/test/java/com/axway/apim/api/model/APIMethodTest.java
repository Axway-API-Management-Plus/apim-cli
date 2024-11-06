package com.axway.apim.api.model;

import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class APIMethodTest {

    @Test
    public void testAPIMethodsWithDifferentOrder() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String actual = "[{\n" +
                "    \"name\": \"updateUser\",\n" +
                "    \"summary\": \"Update user\",\n" +
                "    \"descriptionType\": \"original\"\n" +
                "  }, {\n" +
                "    \"name\": \"getUserByName\",\n" +
                "    \"summary\": \"Get user by user name\",\n" +
                "    \"descriptionType\": \"original\"\n" +
                "  }]";
        TypeReference<List<APIMethod>> apiMethodsTypeRef = new TypeReference<List<APIMethod>>() {};
        List<APIMethod> apiMethods = objectMapper.readValue(actual, apiMethodsTypeRef);

        String desired = "[\n" +
                "  {\n" +
                "    \"name\": \"getUserByName\",\n" +
                "    \"summary\": \"Get user by user name\",\n" +
                "    \"descriptionType\": \"original\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"updateUser\",\n" +
                "    \"summary\": \"Update user\",\n" +
                "    \"descriptionType\": \"original\"\n" +
                "  }\n" +
                "]";
        List<APIMethod> apiMethodsDesired = objectMapper.readValue(desired, apiMethodsTypeRef);
        Assert.assertTrue(Utils.compareValues(apiMethods, apiMethodsDesired));
        //Assert.assertTrue(Utils.areEqualIgnoringOrder(apiMethods, apiMethodsDesired, new APIMethodByName()));

    }

    @Test
    public void testAPIMethodsWithTags() throws JsonProcessingException {
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
        Assert.assertTrue(Utils.compareValues(apiMethods, apiMethodsDesired));

    }

    @Test
    public void testAPIMethodsWithDiffTags() throws JsonProcessingException {
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
                "      \"stage\" : [ \"dev2\" ]\n" +
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
        Assert.assertFalse(Utils.compareValues(apiMethods, apiMethodsDesired));

    }

    @Test
    public void testAPIMethodsWithDiffTagsAndManualDesc() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String actual = "[\n" +
                "  {\n" +
                "    \"name\": \"getUserByName\",\n" +
                "    \"summary\": \"Get user by user name\",\n" +
                "    \"descriptionType\":\"manual\",\n" +
                "    \"descriptionManual\":\"This is my __markdown__ based API description.\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"updateUser\",\n" +
                "    \"summary\": \"Update user\",\n" +
                "    \"descriptionType\":\"manual\",\n" +
                "    \"descriptionManual\":\"This is my __markdown__ based API description.\",\n" +
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
                "    \"descriptionType\":\"manual\",\n" +
                "    \"descriptionManual\":\"This is my __markdown__ based API description.\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"updateUser\",\n" +
                "    \"summary\": \"Update user\",\n" +
                "    \"descriptionType\":\"manual\",\n" +
                "    \"descriptionManual\":\"This is my __markdown__ based API description.\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  }\n" +
                "]";

        List<APIMethod> apiMethodsDesired = objectMapper.readValue(desired, apiMethodsTypeRef);
        Assert.assertTrue(Utils.compareValues(apiMethods, apiMethodsDesired));

    }

    @Test
    public void testAPIMethodsWithDiffTagsAndUrlDesc() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String actual = "[\n" +
                "  {\n" +
                "    \"name\": \"getUserByName\",\n" +
                "    \"summary\": \"Get user by user name\",\n" +
                "    \"descriptionType\":\"url\",\n" +
                "    \"descriptionUrl\":\"https://api.axway.com\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"updateUser\",\n" +
                "    \"summary\": \"Update user\",\n" +
                "    \"descriptionType\":\"url\",\n" +
                "    \"descriptionUrl\":\"https://api.axway.com\",\n" +
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
                "    \"descriptionType\":\"url\",\n" +
                "    \"descriptionUrl\":\"https://api.axway.com\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"updateUser\",\n" +
                "    \"summary\": \"Update user\",\n" +
                "    \"descriptionType\":\"url\",\n" +
                "    \"descriptionUrl\":\"https://api.axway.com\",\n" +
                "    \"tags\" : {\n" +
                "      \"stage\" : [ \"dev\" ]\n" +
                "    }\n" +
                "  }\n" +
                "]";

        List<APIMethod> apiMethodsDesired = objectMapper.readValue(desired, apiMethodsTypeRef);
        Assert.assertTrue(Utils.compareValues(apiMethods, apiMethodsDesired));

    }

    @Test
    public void testAPIMethodsWithNoSummary() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String actual = "[{\n" +
            "    \"name\": \"updateUser\",\n" +
            "    \"descriptionType\": \"original\"\n" +
            "  }]";
        TypeReference<List<APIMethod>> apiMethodsTypeRef = new TypeReference<List<APIMethod>>() {};
        List<APIMethod> apiMethods = objectMapper.readValue(actual, apiMethodsTypeRef);

        String desired = "[{\n" +
            "    \"name\": \"updateUser\",\n" +
            "    \"descriptionType\": \"original\"\n" +
            "  }]";
        List<APIMethod> apiMethodsDesired = objectMapper.readValue(desired, apiMethodsTypeRef);
        Assert.assertTrue(Utils.compareValues(apiMethods, apiMethodsDesired));
        //Assert.assertTrue(Utils.areEqualIgnoringOrder(apiMethods, apiMethodsDesired, new APIMethodByName()));

    }


    @Test
    public void testAPIMethodsWithSummary() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String actual = "[{\n" +
            "    \"name\": \"updateUser\",\n" +
            "    \"summary\": \"Update user\",\n" +
            "    \"descriptionType\": \"original\"\n" +
            "  }]";
        TypeReference<List<APIMethod>> apiMethodsTypeRef = new TypeReference<List<APIMethod>>() {};
        List<APIMethod> apiMethods = objectMapper.readValue(actual, apiMethodsTypeRef);

        String desired = "[{\n" +
            "    \"name\": \"updateUser\",\n" +
            "    \"summary\": \"Update user\",\n" +
            "    \"descriptionType\": \"original\"\n" +
            "  }]";
        List<APIMethod> apiMethodsDesired = objectMapper.readValue(desired, apiMethodsTypeRef);
        Assert.assertTrue(Utils.compareValues(apiMethods, apiMethodsDesired));
        //Assert.assertTrue(Utils.areEqualIgnoringOrder(apiMethods, apiMethodsDesired, new APIMethodByName()));

    }

    @Test
    public void testAPIMethodsWithSourceSummary() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String actual = "[{\n" +
            "    \"name\": \"updateUser\",\n" +
            "    \"summary\": \"Update user\",\n" +
            "    \"descriptionType\": \"original\"\n" +
            "  }]";
        TypeReference<List<APIMethod>> apiMethodsTypeRef = new TypeReference<List<APIMethod>>() {};
        List<APIMethod> apiMethods = objectMapper.readValue(actual, apiMethodsTypeRef);

        String desired = "[{\n" +
            "    \"name\": \"updateUser\",\n" +
            "    \"descriptionType\": \"original\"\n" +
            "  }]";
        List<APIMethod> apiMethodsDesired = objectMapper.readValue(desired, apiMethodsTypeRef);
        Assert.assertFalse(Utils.compareValues(apiMethods, apiMethodsDesired));

    }

    @Test
    public void testAPIMethodsWithTargetSummary() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String actual = "[{\n" +
            "    \"name\": \"updateUser\",\n" +
            "    \"descriptionType\": \"original\"\n" +
            "  }]";
        TypeReference<List<APIMethod>> apiMethodsTypeRef = new TypeReference<List<APIMethod>>() {};
        List<APIMethod> apiMethods = objectMapper.readValue(actual, apiMethodsTypeRef);

        String desired = "[{\n" +
            "    \"name\": \"updateUser\",\n" +
            "    \"summary\": \"Update user\",\n" +
            "    \"descriptionType\": \"original\"\n" +
            "  }]";
        List<APIMethod> apiMethodsDesired = objectMapper.readValue(desired, apiMethodsTypeRef);
        Assert.assertFalse(Utils.compareValues(apiMethods, apiMethodsDesired));
    }

    @Test
    public void testAPIMethodsWithEmptyTags() {
        List<APIMethod> sourceAPIMethods = new ArrayList<>();
        APIMethod apiMethod = new APIMethod();
        apiMethod.setName("POST /web/auth/greenBees/setContact");
        apiMethod.setDescriptionType("original");
        sourceAPIMethods.add(apiMethod);

        APIMethod apiMethod1 = new APIMethod();
        apiMethod1.setName("POST /readAvailability");
        apiMethod1.setDescriptionType("original");
        sourceAPIMethods.add(apiMethod1);

        List<APIMethod> targetAPIMethods = new ArrayList<>();
        APIMethod apiMethod2 = new APIMethod();
        apiMethod2.setName("POST /web/auth/greenBees/setContact");
        apiMethod2.setDescriptionType("original");
        apiMethod2.setTags(new TagMap());
        targetAPIMethods.add(apiMethod2);

        APIMethod apiMethod21 = new APIMethod();
        apiMethod21.setName("POST /readAvailability");
        apiMethod21.setDescriptionType("original");
        apiMethod21.setTags(new TagMap());
        targetAPIMethods.add(apiMethod21);
        System.out.println(sourceAPIMethods);
        System.out.println(targetAPIMethods);
        Assert.assertTrue(Utils.compareValues(sourceAPIMethods, targetAPIMethods));
    }

}
