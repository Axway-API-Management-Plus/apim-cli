package com.axway.apim.api.model;

import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityProfileTest {

    @Test
    public void securityProfileTest() throws JsonProcessingException {
        String request = "[\n" +
            "            {\n" +
            "                \"name\": \"_default\",\n" +
            "                \"isDefault\": true,\n" +
            "                \"devices\": [\n" +
            "                    {\n" +
            "                        \"name\": \"Invoke Policy\",\n" +
            "                        \"type\": \"authPolicy\",\n" +
            "                        \"order\": 1,\n" +
            "                        \"properties\": {\n" +
            "                            \"authenticationPolicy\": \"Inbound security policy 1\",\n" +
            "                            \"useClientRegistry\": \"true\",\n" +
            "                            \"subjectSelector\": \"${authentication.subject.id}\",\n" +
            "                            \"descriptionType\": \"original\",\n" +
            "                            \"descriptionUrl\": \"\",\n" +
            "                            \"descriptionMarkdown\": \"\",\n" +
            "                            \"description\": \"\"\n" +
            "                        }\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ]";

        ObjectMapper objectMapper = new ObjectMapper();
        List<SecurityProfile> securityProfiles = objectMapper.readValue(request, new TypeReference<List<SecurityProfile>>() {
        });
        Assert.assertTrue(Utils.compareValues(securityProfiles, securityProfiles));
    }

    @Test
    public void compareExternalOauth() throws JsonProcessingException {
        String newConfig = "[ {\n" +
            "  \"name\" : \"_default\",\n" +
            "  \"isDefault\" : true,\n" +
            "  \"devices\" : [ {\n" +
            "    \"name\" : \"OAuth (External)\",\n" +
            "    \"type\" : \"oauthExternal\",\n" +
            "    \"order\" : 1,\n" +
            "    \"properties\" : {\n" +
            "      \"tokenStore\" : \"Tokeninfo policy 1\",\n" +
            "      \"accessTokenLocation\" : \"HEADER\",\n" +
            "      \"authorizationHeaderPrefix\" : \"Bearer\",\n" +
            "      \"accessTokenLocationQueryString\" : \"\",\n" +
            "      \"scopesMustMatch\" : \"Any\",\n" +
            "      \"scopes\" : \"1.0\",\n" +
            "      \"removeCredentialsOnSuccess\" : \"false\",\n" +
            "      \"implicitGrantEnabled\" : \"true\",\n" +
            "      \"implicitGrantLoginEndpointUrl\" : \"https://login.microsoftonline.com/5983457345783489759834753/oauth2/authorize\",\n" +
            "      \"implicitGrantLoginTokenName\" : \"access_token\",\n" +
            "      \"authCodeGrantTypeEnabled\" : \"true\",\n" +
            "      \"authCodeGrantTypeRequestEndpointUrl\" : \"https://login.microsoftonline.com/5983457345783489759834753/oauth2/authorize\",\n" +
            "      \"authCodeGrantTypeRequestClientIdName\" : \"client_id\",\n" +
            "      \"authCodeGrantTypeRequestSecretName\" : \"client_secret\",\n" +
            "      \"authCodeGrantTypeTokenEndpointUrl\" : \"https://login.microsoftonline.com/5983457345783489759834753/oauth2/token\",\n" +
            "      \"authCodeGrantTypeTokenEndpointTokenName\" : \"access_code\",\n" +
            "      \"useClientRegistry\" : \"true\",\n" +
            "      \"subjectSelector\" : \"${oauth.token.client_id}\",\n" +
            "      \"oauth.token.client_id\" : \"${oauth.token.client_id}\",\n" +
            "      \"oauth.token.scopes\" : \"${oauth.token.scopes}\",\n" +
            "      \"oauth.token.valid\" : \"${oauth.token.valid}\"\n" +
            "    }\n" +
            "  } ]\n" +
            "} ]";

        ObjectMapper objectMapper = new ObjectMapper();
        List<SecurityProfile> securityProfiles = objectMapper.readValue(newConfig, new TypeReference<List<SecurityProfile>>() {
        });

        String actualConfig = "[ {\n" +
            "  \"name\" : \"_default\",\n" +
            "  \"isDefault\" : true,\n" +
            "  \"devices\" : [ {\n" +
            "    \"name\" : \"OAuth (External)\",\n" +
            "    \"type\" : \"oauthExternal\",\n" +
            "    \"order\" : 1,\n" +
            "    \"properties\" : {\n" +
            "      \"accessTokenLocation\" : \"HEADER\",\n" +
            "      \"authorizationHeaderPrefix\" : \"Bearer\",\n" +
            "      \"accessTokenLocationQueryString\" : \"\",\n" +
            "      \"scopesMustMatch\" : \"Any\",\n" +
            "      \"scopes\" : \"1.0\",\n" +
            "      \"removeCredentialsOnSuccess\" : \"false\",\n" +
            "      \"implicitGrantEnabled\" : \"true\",\n" +
            "      \"implicitGrantLoginEndpointUrl\" : \"https://login.microsoftonline.com/5983457345783489759834753/oauth2/authorize\",\n" +
            "      \"implicitGrantLoginTokenName\" : \"access_token\",\n" +
            "      \"authCodeGrantTypeEnabled\" : \"true\",\n" +
            "      \"authCodeGrantTypeRequestEndpointUrl\" : \"https://login.microsoftonline.com/5983457345783489759834753/oauth2/authorize\",\n" +
            "      \"authCodeGrantTypeRequestClientIdName\" : \"client_id\",\n" +
            "      \"authCodeGrantTypeRequestSecretName\" : \"client_secret\",\n" +
            "      \"authCodeGrantTypeTokenEndpointUrl\" : \"https://login.microsoftonline.com/5983457345783489759834753/oauth2/token\",\n" +
            "      \"authCodeGrantTypeTokenEndpointTokenName\" : \"access_code\",\n" +
            "      \"useClientRegistry\" : \"true\",\n" +
            "      \"subjectSelector\" : \"${oauth.token.client_id}\",\n" +
            "      \"oauth.token.client_id\" : \"${oauth.token.client_id}\",\n" +
            "      \"oauth.token.scopes\" : \"${oauth.token.scopes}\",\n" +
            "      \"oauth.token.valid\" : \"${oauth.token.valid}\"\n" +
            "    }\n" +
            "  } ]\n" +
            "} ]";

        List<SecurityProfile> actualSecurityProfiles = objectMapper.readValue(actualConfig, new TypeReference<List<SecurityProfile>>() {
        });

        Assert.assertFalse(Utils.compareValues(securityProfiles, actualSecurityProfiles));


    }

    @Test
    public void compareExternalOauthWithEmptyValues() throws JsonProcessingException {
        String importConfig = "[\n" +
            "    {\n" +
            "        \"devices\": [\n" +
            "            {\n" +
            "                \"name\": \"OAuth (External)\",\n" +
            "                \"order\": 1,\n" +
            "                \"properties\": {\n" +
            "                    \"accessTokenLocation\": \"HEADER\",\n" +
            "                    \"accessTokenLocationQueryString\": \"\",\n" +
            "                    \"authCodeGrantTypeEnabled\": \"true\",\n" +
            "                    \"authCodeGrantTypeRequestClientIdName\": \"client_id\",\n" +
            "                    \"authCodeGrantTypeRequestEndpointUrl\": \"https://login.microsoftonline.com/5983457345783489759834753/oauth2/authorize\",\n" +
            "                    \"authCodeGrantTypeRequestSecretName\": \"client_secret\",\n" +
            "                    \"authCodeGrantTypeTokenEndpointTokenName\": \"access_code\",\n" +
            "                    \"authCodeGrantTypeTokenEndpointUrl\": \"https://login.microsoftonline.com/5983457345783489759834753/oauth2/token\",\n" +
            "                    \"authorizationHeaderPrefix\": \"Bearer\",\n" +
            "                    \"implicitGrantEnabled\": \"true\",\n" +
            "                    \"implicitGrantLoginEndpointUrl\": \"https://login.microsoftonline.com/5983457345783489759834753/oauth2/authorize\",\n" +
            "                    \"implicitGrantLoginTokenName\": \"access_token\",\n" +
            "                    \"oauth.token.client_id\": \"${oauth.token.client_id}\",\n" +
            "                    \"oauth.token.scopes\": \"${oauth.token.scopes}\",\n" +
            "                    \"oauth.token.valid\": \"${oauth.token.valid}\",\n" +
            "                    \"removeCredentialsOnSuccess\": \"false\",\n" +
            "                    \"scopes\": \"1.0\",\n" +
            "                    \"scopesMustMatch\": \"Any\",\n" +
            "                    \"subjectSelector\": \"${oauth.token.client_id}\",\n" +
            "                    \"tokenStore\": \"Tokeninfo policy 1\",\n" +
            "                    \"useClientRegistry\": \"true\"\n" +
            "                },\n" +
            "                \"type\": \"oauthExternal\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"isDefault\": true,\n" +
            "        \"name\": \"_default\"\n" +
            "    }\n" +
            "]";

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);


        List<SecurityProfile> securityProfiles = objectMapper.readValue(importConfig, new TypeReference<List<SecurityProfile>>() {

        });

        String exportedConfig = "[\n" +
            "    {\n" +
            "        \"devices\": [\n" +
            "            {\n" +
            "                \"name\": \"OAuth (External)\",\n" +
            "                \"order\": 1,\n" +
            "                \"properties\": {\n" +
            "                    \"accessTokenLocation\": \"HEADER\",\n" +
            "                    \"authCodeGrantTypeEnabled\": \"true\",\n" +
            "                    \"authCodeGrantTypeRequestClientIdName\": \"client_id\",\n" +
            "                    \"authCodeGrantTypeRequestEndpointUrl\": \"https://login.microsoftonline.com/5983457345783489759834753/oauth2/authorize\",\n" +
            "                    \"authCodeGrantTypeRequestSecretName\": \"client_secret\",\n" +
            "                    \"authCodeGrantTypeTokenEndpointTokenName\": \"access_code\",\n" +
            "                    \"authCodeGrantTypeTokenEndpointUrl\": \"https://login.microsoftonline.com/5983457345783489759834753/oauth2/token\",\n" +
            "                    \"authorizationHeaderPrefix\": \"Bearer\",\n" +
            "                    \"implicitGrantEnabled\": \"true\",\n" +
            "                    \"implicitGrantLoginEndpointUrl\": \"https://login.microsoftonline.com/5983457345783489759834753/oauth2/authorize\",\n" +
            "                    \"implicitGrantLoginTokenName\": \"access_token\",\n" +
            "                    \"oauth.token.client_id\": \"${oauth.token.client_id}\",\n" +
            "                    \"oauth.token.scopes\": \"${oauth.token.scopes}\",\n" +
            "                    \"oauth.token.valid\": \"${oauth.token.valid}\",\n" +
            "                    \"removeCredentialsOnSuccess\": \"false\",\n" +
            "                    \"scopes\": \"1.0\",\n" +
            "                    \"scopesMustMatch\": \"Any\",\n" +
            "                    \"subjectSelector\": \"${oauth.token.client_id}\",\n" +
            "                    \"tokenStore\": \"Tokeninfo policy 1\",\n" +
            "                    \"useClientRegistry\": \"true\"\n" +
            "                },\n" +
            "                \"type\": \"oauthExternal\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"isDefault\": true,\n" +
            "        \"name\": \"_default\"\n" +
            "    }\n" +
            "]";
        List<SecurityProfile> actualSecurityProfiles = objectMapper.readValue(exportedConfig, new TypeReference<List<SecurityProfile>>() {
        });
        // Ignore empty filed use objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString
        securityProfiles = objectMapper.readValue(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(securityProfiles), new TypeReference<List<SecurityProfile>>() {

        });

        actualSecurityProfiles = objectMapper.readValue(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualSecurityProfiles), new TypeReference<List<SecurityProfile>>() {
        });
        Assert.assertTrue(Utils.compareValues(securityProfiles, actualSecurityProfiles));

    }
}

