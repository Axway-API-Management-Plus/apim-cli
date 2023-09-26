package com.axway.apim.api.model;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class SecurityDeviceTest extends WiremockWrapper {

    private APIManagerAdapter apiManagerAdapter;

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
        CoreParameters coreParameters = new CoreParameters();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());
        try {
            apiManagerAdapter = APIManagerAdapter.getInstance();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public void close() {
        Utils.deleteInstance(apiManagerAdapter);

        super.close();
    }

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
