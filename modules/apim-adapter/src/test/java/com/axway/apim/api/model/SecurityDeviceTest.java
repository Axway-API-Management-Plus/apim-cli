package com.axway.apim.api.model;

import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class SecurityDeviceTest {


    @Test
    public void compareSecurityDevice() throws JsonProcessingException {

        String request = "[ {\n" +
            "      \"name\" : \"Invoke Policy\",\n" +
            "      \"type\" : \"authPolicy\",\n" +
            "      \"order\" : 1,\n" +
            "      \"properties\" : {\n" +
            "        \"authenticationPolicy\" : \"Inbound security policy 1\",\n" +
            "        \"useClientRegistry\" : \"false\",\n" +
            "        \"subjectSelector\" : \"${authentication.subject.id}\",\n" +
            "        \"descriptionType\" : \"original\",\n" +
            "        \"descriptionUrl\" : \"\",\n" +
            "        \"descriptionMarkdown\" : \"\",\n" +
            "        \"description\" : \"\"\n" +
            "      }\n" +
            "    } ]\n" +
            "  } ]";

        ObjectMapper objectMapper = new ObjectMapper();
        List<SecurityDevice> securityDevices = objectMapper.readValue(request, new TypeReference<List<SecurityDevice>>() {
        });
        Assert.assertTrue(Utils.compareValues(securityDevices, securityDevices));
    }

    @Test
    public void compareTwoSecurityDevice() throws JsonProcessingException {



        String request = "[ {\n" +
            "      \"name\" : \"Invoke Policy\",\n" +
            "      \"type\" : \"authPolicy\",\n" +
            "      \"order\" : 1,\n" +
            "      \"properties\" : {\n" +
            "        \"authenticationPolicy\" : \"Inbound security policy 1\",\n" +
            "        \"useClientRegistry\" : \"false\",\n" +
            "        \"subjectSelector\" : \"${authentication.subject.id}\",\n" +
            "        \"descriptionType\" : \"original\",\n" +
            "        \"descriptionUrl\" : \"\",\n" +
            "        \"descriptionMarkdown\" : \"\",\n" +
            "        \"description\" : \"\"\n" +
            "      },\n" +
            "      \"convertPolicies\" : \"true\"\n" +
            "    } ]\n" +
            "  } ]";

        ObjectMapper objectMapper = new ObjectMapper();
        List<SecurityDevice> securityDevices = objectMapper.readValue(request, new TypeReference<List<SecurityDevice>>() {
        });
        Assert.assertTrue(Utils.compareValues(securityDevices, securityDevices));
    }
}
