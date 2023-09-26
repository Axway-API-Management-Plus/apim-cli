package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.Policy;
import com.axway.apim.api.model.SecurityProfile;
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

public class APIManagerPoliciesAdapterTest extends WiremockWrapper {

    private APIManagerPoliciesAdapter apiManagerPoliciesAdapter;
    private APIManagerAdapter apiManagerAdapter;

    @BeforeClass
    public void init() {
        try {
            initWiremock();
            CoreParameters coreParameters = new CoreParameters();
            coreParameters.setHostname("localhost");
            coreParameters.setUsername("apiadmin");
            coreParameters.setPassword(Utils.getEncryptedPassword());
            apiManagerAdapter = APIManagerAdapter.getInstance();
            apiManagerPoliciesAdapter = apiManagerAdapter.getPoliciesAdapter();
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
    public void getAllPolicies() throws AppException {
        List<Policy> policies = apiManagerPoliciesAdapter.getAllPolicies();
        Assert.assertNotNull(policies);
    }

    @Test(expectedExceptions = AppException.class)
    public void getPolicyForNameNegative() throws AppException {
        Policy policy = apiManagerPoliciesAdapter.getPolicyForName(APIManagerPoliciesAdapter.PolicyType.REQUEST, "test");
        Assert.assertNotNull(policy);
    }

    @Test
    public void getPolicyForName() throws AppException {
        Policy policy = apiManagerPoliciesAdapter.getPolicyForName(APIManagerPoliciesAdapter.PolicyType.REQUEST, "Validate Size & Token");
        Assert.assertNotNull(policy);
    }

    @Test
    public void getOauthTokenStore() throws AppException {
        Assert.assertNotNull(apiManagerPoliciesAdapter.getOauthTokenStore("OAuth Access Token Store"));
    }

    @Test
    public void getEntityStorePolicyFormat() throws AppException {
        String entityStorePolicy = apiManagerPoliciesAdapter.getEntityStorePolicyFormat(APIManagerPoliciesAdapter.PolicyType.AUTHENTICATION, "Inbound security policy 1");
        System.out.println(entityStorePolicy);
        Assert.assertTrue(entityStorePolicy.startsWith("<key"));
    }

    @Test
    public void getEntityStorePolicyFormatInvalid() throws AppException {
        String entityStorePolicy = apiManagerPoliciesAdapter.getEntityStorePolicyFormat(APIManagerPoliciesAdapter.PolicyType.AUTHENTICATION, "Inbound security policy 2");
        Assert.assertNull(entityStorePolicy);
    }

    @Test
    public void updateSecurityProfilesAuthentication() throws JsonProcessingException {
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

        API api = new API();
        api.setName("test");
        api.setSecurityProfiles(securityProfiles);
        apiManagerPoliciesAdapter.updateSecurityProfiles(api);

        Assert.assertTrue(api.getSecurityProfiles().get(0).getDevices().get(0).getProperties().get("authenticationPolicy").startsWith("<key"));
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "Invalid authentication policy : Inbound security policy 2")
    public void updateSecurityProfilesAuthenticationInvalid() throws JsonProcessingException {
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
            "                            \"authenticationPolicy\": \"Inbound security policy 2\",\n" +
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

        API api = new API();
        api.setName("test");
        api.setSecurityProfiles(securityProfiles);
        apiManagerPoliciesAdapter.updateSecurityProfiles(api);
    }


    @Test
    public void updateSecurityProfilesExternalOauth() throws JsonProcessingException {
        String request = "[ {\n" +
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
        List<SecurityProfile> securityProfiles = objectMapper.readValue(request, new TypeReference<List<SecurityProfile>>() {
        });

        API api = new API();
        api.setName("test");
        api.setSecurityProfiles(securityProfiles);
        apiManagerPoliciesAdapter.updateSecurityProfiles(api);
        Assert.assertTrue(api.getSecurityProfiles().get(0).getDevices().get(0).getProperties().get("tokenStore").startsWith("<key"));
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "Invalid Oauth token info policy : Tokeninfo policy 2")
    public void updateSecurityProfilesExternalOauthInvalid() throws JsonProcessingException {
        String request = "[ {\n" +
            "  \"name\" : \"_default\",\n" +
            "  \"isDefault\" : true,\n" +
            "  \"devices\" : [ {\n" +
            "    \"name\" : \"OAuth (External)\",\n" +
            "    \"type\" : \"oauthExternal\",\n" +
            "    \"order\" : 1,\n" +
            "    \"properties\" : {\n" +
            "      \"tokenStore\" : \"Tokeninfo policy 2\",\n" +
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
        List<SecurityProfile> securityProfiles = objectMapper.readValue(request, new TypeReference<List<SecurityProfile>>() {
        });

        API api = new API();
        api.setName("test");
        api.setSecurityProfiles(securityProfiles);
        apiManagerPoliciesAdapter.updateSecurityProfiles(api);
    }
    @Test
    public void updateSecurityProfilesOauth() throws JsonProcessingException {
        String request = "[{\n" +
            "\t\"name\": \"_default\",\n" +
            "\t\"isDefault\": true,\n" +
            "\t\"devices\": [{\n" +
            "\t\t\"type\": \"oauth\",\n" +
            "\t\t\"name\": \"OAuth\",\n" +
            "\t\t\"order\": 1,\n" +
            "\t\t\"properties\": {\n" +
            "\t\t\t\"tokenStore\": \"OAuth Access Token Store\",\n" +
            "\t\t\t\"accessTokenLocation\": \"HEADER\",\n" +
            "\t\t\t\"authorizationHeaderPrefix\": \"Bearer\",\n" +
            "\t\t\t\"accessTokenLocationQueryString\": \"\",\n" +
            "\t\t\t\"scopesMustMatch\": \"Any\",\n" +
            "\t\t\t\"scopes\": \"resource.WRITE, resource.READ, resource.ADMIN\",\n" +
            "\t\t\t\"removeCredentialsOnSuccess\": true,\n" +
            "\t\t\t\"implicitGrantEnabled\": true,\n" +
            "\t\t\t\"implicitGrantLoginEndpointUrl\": \"https://localhost:8089/api/oauth/authorize\",\n" +
            "\t\t\t\"implicitGrantLoginTokenName\": \"access_token\",\n" +
            "\t\t\t\"authCodeGrantTypeEnabled\": true,\n" +
            "\t\t\t\"authCodeGrantTypeRequestEndpointUrl\": \"https://localhost:8089/api/oauth/authorize\",\n" +
            "\t\t\t\"authCodeGrantTypeRequestClientIdName\": \"client_id\",\n" +
            "\t\t\t\"authCodeGrantTypeRequestSecretName\": \"client_secret\",\n" +
            "\t\t\t\"authCodeGrantTypeTokenEndpointUrl\": \"https://localhost:8089/api/oauth/token\",\n" +
            "\t\t\t\"authCodeGrantTypeTokenEndpointTokenName\": \"access_code\"\n" +
            "\t\t}\n" +
            "\t}]\n" +
            "}]";

        ObjectMapper objectMapper = new ObjectMapper();
        List<SecurityProfile> securityProfiles = objectMapper.readValue(request, new TypeReference<List<SecurityProfile>>() {
        });

        API api = new API();
        api.setName("test");
        api.setSecurityProfiles(securityProfiles);
        apiManagerPoliciesAdapter.updateSecurityProfiles(api);
        Assert.assertTrue(api.getSecurityProfiles().get(0).getDevices().get(0).getProperties().get("tokenStore").startsWith("<key"));
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "Oauth auth store is not configured")
    public void updateSecurityProfilesOauthInvalid() throws JsonProcessingException {
        String request = "[{\n" +
            "\t\"name\": \"_default\",\n" +
            "\t\"isDefault\": true,\n" +
            "\t\"devices\": [{\n" +
            "\t\t\"type\": \"oauth\",\n" +
            "\t\t\"name\": \"OAuth\",\n" +
            "\t\t\"order\": 1,\n" +
            "\t\t\"properties\": {\n" +
            "\t\t\t\"tokenStore\": \"OAuth Access Token Store 2\",\n" +
            "\t\t\t\"accessTokenLocation\": \"HEADER\",\n" +
            "\t\t\t\"authorizationHeaderPrefix\": \"Bearer\",\n" +
            "\t\t\t\"accessTokenLocationQueryString\": \"\",\n" +
            "\t\t\t\"scopesMustMatch\": \"Any\",\n" +
            "\t\t\t\"scopes\": \"resource.WRITE, resource.READ, resource.ADMIN\",\n" +
            "\t\t\t\"removeCredentialsOnSuccess\": true,\n" +
            "\t\t\t\"implicitGrantEnabled\": true,\n" +
            "\t\t\t\"implicitGrantLoginEndpointUrl\": \"https://localhost:8089/api/oauth/authorize\",\n" +
            "\t\t\t\"implicitGrantLoginTokenName\": \"access_token\",\n" +
            "\t\t\t\"authCodeGrantTypeEnabled\": true,\n" +
            "\t\t\t\"authCodeGrantTypeRequestEndpointUrl\": \"https://localhost:8089/api/oauth/authorize\",\n" +
            "\t\t\t\"authCodeGrantTypeRequestClientIdName\": \"client_id\",\n" +
            "\t\t\t\"authCodeGrantTypeRequestSecretName\": \"client_secret\",\n" +
            "\t\t\t\"authCodeGrantTypeTokenEndpointUrl\": \"https://localhost:8089/api/oauth/token\",\n" +
            "\t\t\t\"authCodeGrantTypeTokenEndpointTokenName\": \"access_code\"\n" +
            "\t\t}\n" +
            "\t}]\n" +
            "}]";

        ObjectMapper objectMapper = new ObjectMapper();
        List<SecurityProfile> securityProfiles = objectMapper.readValue(request, new TypeReference<List<SecurityProfile>>() {
        });

        API api = new API();
        api.setName("test");
        api.setSecurityProfiles(securityProfiles);
        apiManagerPoliciesAdapter.updateSecurityProfiles(api);
    }


}
