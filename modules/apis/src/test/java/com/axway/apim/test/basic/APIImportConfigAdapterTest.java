package com.axway.apim.test.basic;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIManagerMockBase;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.apiimport.APIImportConfigAdapter;
import com.axway.apim.apiimport.DesiredAPI;
import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.EnvironmentProperties;
import com.axway.apim.lib.errorHandling.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import java.io.IOException;
import java.util.Map;

public class APIImportConfigAdapterTest extends APIManagerMockBase {

    private static final Logger LOG = LoggerFactory.getLogger(APIImportConfigAdapterTest.class);
    private String apimCliHome;

    @BeforeClass
    private void initCommandParameters() throws IOException {
        setupMockData();
        apimCliHome = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + "apimcli";
    }

    // Make sure, you don't have configured APIM_CLI_HOME when running this test
    @Test
    public void withoutStage() throws AppException {
        // Create Environment properties without any stage (basically loads env.properties)
        EnvironmentProperties props = new EnvironmentProperties(null, apimCliHome);
        APIImportParams params = new APIImportParams();
        params.setProperties(props);
        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();

        APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", null);
        DesiredAPI apiConfig = (DesiredAPI) adapter.getApiConfig();
        Assert.assertEquals(apiConfig.getBackendBasepath(), "resolvedToSomething");
    }

    // Make sure, you don't have configured APIM_CLI_HOME when running this test
    @Test
    public void withStage() throws AppException {
        // Providing a stage, it should load the env.variabletest.properties
        EnvironmentProperties props = new EnvironmentProperties("variabletest", apimCliHome);
        APIImportParams params = new APIImportParams();
        params.setProperties(props);
        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();

        APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", null);
        DesiredAPI apiConfig = (DesiredAPI) adapter.getApiConfig();
        Assert.assertEquals(apiConfig.getBackendBasepath(), "resolvedToSomethingElse");
    }

    // Make sure, you don't have configured APIM_CLI_HOME when running this test
    @Test
    public void withManualStageConfig() throws AppException {
        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
        APIImportParams params = new APIImportParams();
        params.setConfig(testConfig);
        params.setStageConfig("staged-minimal-config.json");

        APIImportConfigAdapter adapter = new APIImportConfigAdapter(params);
        DesiredAPI apiConfig = (DesiredAPI) adapter.getApiConfig();
        Assert.assertEquals(apiConfig.getName(), "API-Name is different for this stage");
    }

    @Test
    public void usingOSEnvVariable() throws AppException {
        try {
            String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();
            APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", null);
            DesiredAPI apiConfig = (DesiredAPI) adapter.getApiConfig();
            String osEnv = System.getenv().get("OS");
            // To ignore the check on OS as  MAC, Ubuntu does not have environment variable 'OS'
            if (osEnv == null) {
                String osName = System.getProperty("os.name");
                LOG.warn("Ignoring test as OS : " + osName + " does not have environment variable OS");
                return;
            }

            Assert.assertEquals(EnvironmentProperties.resolveValueWithEnvVars(apiConfig.getSummary()), "Operating system: " + osEnv);
        } catch (Exception e) {
            LOG.error("Error running test: usingOSEnvVariable", e);
            throw e;
        }
    }

    @Test
    public void notDeclaredVariable() throws AppException {
        EnvironmentProperties props = new EnvironmentProperties(null, apimCliHome);
        APIImportParams params = new APIImportParams();
        params.setProperties(props);
        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();

        APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", null);
        DesiredAPI apiConfig = (DesiredAPI) adapter.getApiConfig();
        Assert.assertEquals(apiConfig.getVersion(), "${notDeclared}");
    }

    @Test
    public void configFileWithSpaces() throws AppException {
        EnvironmentProperties props = new EnvironmentProperties(null, apimCliHome);
        APIImportParams params = new APIImportParams();
        params.setProperties(props);
        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api config with spaces.json").getFile();

        APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "notRelavantForThis Test", null);
        DesiredAPI apiConfig = (DesiredAPI) adapter.getApiConfig();
        Assert.assertEquals(apiConfig.getVersion(), "${notDeclared}");
    }

    @Test
    public void stageConfigInSubDirectory() throws AppException {
        EnvironmentProperties props = new EnvironmentProperties(null, apimCliHome);
        APIImportParams params = new APIImportParams();
        params.setProperties(props);
        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-variables.json").getFile();

        APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, "testStageProd", "notRelavantForThis Test", null);
        DesiredAPI apiConfig = (DesiredAPI) adapter.getApiConfig();
        Assert.assertEquals(apiConfig.getVersion(), "9.0.0");
        Assert.assertEquals(apiConfig.getName(), "API Config from testStageProd sub folder");
    }

    @Test
    public void outboundOAuthValidConfig() throws AppException {
        EnvironmentProperties props = new EnvironmentProperties(null, apimCliHome);
        props.put("myOAuthProfileName", "Sample OAuth Client Profile");
        APIImportParams params = new APIImportParams();
        params.setProperties(props);
        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/outbound-oauth-config.json").getFile();

        APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, "testStageProd", "petstore.json", null);
        adapter.getDesiredAPI();
        DesiredAPI apiConfig = (DesiredAPI) adapter.getApiConfig();
        Assert.assertEquals(apiConfig.getVersion(), "kk1");
        Assert.assertEquals(apiConfig.getName(), "My OAuth API");
        // Make sure, the OAuth-Provider profiles are translated
        Assert.assertTrue(apiConfig.getAuthenticationProfiles().get(0).getIsDefault(), "First OAuth profile is configured as default");
        Assert.assertFalse(apiConfig.getAuthenticationProfiles().get(1).getIsDefault(), "Second OAuth profile is not default");
        String translatedProfile1 = (String) apiConfig.getAuthenticationProfiles().get(0).getParameters().get("providerProfile");
        String translatedProfile2 = (String) apiConfig.getAuthenticationProfiles().get(1).getParameters().get("providerProfile");
        Assert.assertTrue(translatedProfile1.startsWith("<key type='AuthProfilesGroup'><id field='name' value='Auth Profil"), "OAuth provider profile 1 is not translated.");
        Assert.assertTrue(translatedProfile2.startsWith("<key type='AuthProfilesGroup'><id field='name' value='Auth Profil"), "OAuth provider profile 2 is not translated.");
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "The OAuth provider profile is unkown: 'Invalid profile name'. Known profiles:.*")
    public void outboundOAuthInValidConfig() throws AppException {
        EnvironmentProperties props = new EnvironmentProperties(null, apimCliHome);
        props.put("myOAuthProfileName", "Invalid profile name");
        APIImportParams params = new APIImportParams();
        params.setProperties(props);
        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/outbound-oauth-config.json").getFile();

        APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "petstore.json", null);
        adapter.getDesiredAPI();
        DesiredAPI apiConfig = (DesiredAPI) adapter.getApiConfig();
        Assert.assertEquals(apiConfig.getVersion(), "kk1");
        Assert.assertEquals(apiConfig.getName(), "My OAuth API");
    }

    @Test
    public void emptyVHostTest() throws AppException {
        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/empty-vhost-api-config.json").getFile();

        APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "petstore.json", null);
        adapter.getDesiredAPI();
        DesiredAPI apiConfig = (DesiredAPI) adapter.getApiConfig();
        Assert.assertNull(apiConfig.getVhost(), "Empty VHost should be considered as not set (null), as an empty VHost is logically not possible to have.");
    }

    @Test
    public void outboundProfileWithDefaultAuthOnlyTest() throws AppException {
        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/methodLevel/method-level-outboundprofile-default-authn-only.json").getFile();

        APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "../basic/petstore.json", null);
        adapter.getDesiredAPI();
        DesiredAPI apiConfig = (DesiredAPI) adapter.getApiConfig();
        Map<String, OutboundProfile> outboundProfiles = apiConfig.getOutboundProfiles();
        Assert.assertEquals(outboundProfiles.size(), 2, "Two outbound profiles are expected.");
        OutboundProfile defaultProfile = outboundProfiles.get("_default");
        OutboundProfile getOrderByIdProfile = outboundProfiles.get("getOrderById");
        Assert.assertEquals(defaultProfile.getAuthenticationProfile(), "_default", "Authentication profile should be the default.");
        Assert.assertEquals(getOrderByIdProfile.getAuthenticationProfile(), "_default", "Authentication profile should be the default.");
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "Missing routingPolicy when routeType is set to policy")
    public void outboundProfileTypePolicyWithoutRoutingPolicy() throws AppException {
        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/policies/invalid-RouteType-Policy-NoRoutingPolicy.json").getFile();

        APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "../basic/petstore.json", null);
        adapter.getDesiredAPI();
    }

    @Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "Missing required custom properties : 'customProperty4'")
    public void testMissingMandatoryCustomProperty() throws IOException {
        String customPropertiesConfig = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "customProperties/customPropertiesConfig.json"));
        APIManagerAdapter.getInstance().customPropertiesAdapter.setAPIManagerTestResponse(customPropertiesConfig);

        EnvironmentProperties props = new EnvironmentProperties(null);
        props.put("orgNumber", "1");
        props.put("apiPath", "/api/with/custom/props");
        props.put("status", "unpublished");
        props.put("customProperty1", "public");
        props.put("customProperty3", "true");

        APIImportParams params = new APIImportParams();
        params.setProperties(props);

        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/customproperties/1_custom-properties-config.json").getFile();
        APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, "../basic/petstore.json", null);
        adapter.getDesiredAPI(); // Should fail, as a mandatory customProperty is missing
    }

    @Test
    public void apiSpecificationObject() throws AppException {
        EnvironmentProperties props = new EnvironmentProperties(null, apimCliHome);
        APIImportParams params = new APIImportParams();
        params.setProperties(props);
        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/basic/api-config-with-api-spec-object.json").getFile();

        APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, null, null);
        adapter.getDesiredAPI();
        DesiredAPI apiConfig = (DesiredAPI) adapter.getApiConfig();
        Assert.assertEquals(apiConfig.getName(), "API with API-Specification object");
        Assert.assertEquals(apiConfig.getVersion(), "1.0.0");
    }

    @Test
    public void testMarkdownLocalClassic() throws AppException {
        EnvironmentProperties props = new EnvironmentProperties(null, apimCliHome);
        APIImportParams params = new APIImportParams();
        params.setProperties(props);
        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/description/1_api_with_local_mark_down_classic.json").getFile();

        APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, null, null);
        adapter.getDesiredAPI();
        DesiredAPI apiConfig = (DesiredAPI) adapter.getApiConfig();
        Assert.assertEquals(apiConfig.getName(), "API with classic markdown local");
        Assert.assertEquals(apiConfig.getDescriptionManual(), "THIS IS THE API-DESCRIPTION FROM A LOCAL MARKDOWN!");
    }

    @Test
    public void testMarkdownLocalList() throws AppException {
        EnvironmentProperties props = new EnvironmentProperties(null, apimCliHome);
        APIImportParams params = new APIImportParams();
        params.setProperties(props);
        String testConfig = this.getClass().getResource("/com/axway/apim/test/files/description/1_api_with_local_mark_down_list.json").getFile();

        APIImportConfigAdapter adapter = new APIImportConfigAdapter(testConfig, null, null, null);
        adapter.getDesiredAPI();
        DesiredAPI apiConfig = (DesiredAPI) adapter.getApiConfig();
        Assert.assertEquals(apiConfig.getName(), "API with classic markdown local list");
        Assert.assertEquals(apiConfig.getDescriptionManual(), "THIS IS THE API-DESCRIPTION FROM A LOCAL MARKDOWN!\n"
                + "This is a sample server Petstore server.  You can find out more about Swagger at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, you can use the api key `special-key` to test the authorization filters.\n"
                + "THIS IS THE SECOND API-DESCRIPTION FROM A LOCAL MARKDOWN!");
    }
}
