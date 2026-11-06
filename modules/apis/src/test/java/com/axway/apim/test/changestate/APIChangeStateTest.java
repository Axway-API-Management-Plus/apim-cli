package com.axway.apim.test.changestate;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.CaCert;
import com.axway.apim.api.model.InboundProfile;
import com.axway.apim.api.model.TagMap;
import com.axway.apim.api.specification.APISpecification;
import com.axway.apim.api.specification.Swagger2xSpecification;
import com.axway.apim.apiimport.APIChangeState;
import com.axway.apim.apiimport.lib.params.APIImportParams;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class APIChangeStateTest extends WiremockWrapper {

    @BeforeClass
    public void initWiremock() {
        super.initWiremock();
        CoreParameters coreParameters = new APIImportParams();
        coreParameters.setHostname("localhost");
        coreParameters.setUsername("test");
        coreParameters.setPassword(Utils.getEncryptedPassword());    }

    @AfterClass
    public void close() {
        super.close();
    }


    private final ObjectMapper mapper = new ObjectMapper();
    API testAPI1;
    API testAPI2;

    @BeforeMethod
    private void setupTestAPIs() throws IOException {
        APIManagerAdapter.getInstance();
        testAPI1 = getTestAPI("ChangeStateTestAPI.json");
        testAPI2 = getTestAPI("ChangeStateTestAPI.json");
    }

    private static final String TEST_PACKAGE = "com/axway/apim/api/state/";

    @Test
    public void testHasNoChange() throws IOException {
        APIChangeState changeState = new APIChangeState(testAPI1, testAPI2);
        Assert.assertTrue(!changeState.hasAnyChanges(), "APIs are equal");
        Assert.assertEquals(changeState.getAllChanges().size(), 0, "No changes properties");
        Assert.assertFalse(changeState.isRecreateAPI(), "No need to Re-Create API");
    }

    @Test
    public void testCaCertsAreChanged() throws IOException {
        CaCert caCert = new CaCert();
        caCert.setAlias("ABC");
        caCert.setExpired("Expired");
        caCert.setCertBlob("Something");
        caCert.setInbound("true");
        caCert.setOutbound("true");
        testAPI2.getCaCerts().add(caCert);
        APIChangeState changeState = new APIChangeState(testAPI1, testAPI2);
        Assert.assertTrue(changeState.hasAnyChanges(), "APIs should not be eqaul");
        Assert.assertEquals(changeState.getAllChanges().size(), 1, "CaCerts is changed");
        Assert.assertTrue(changeState.getAllChanges().contains("caCerts"), "Expect the caCert as a changed prop");
        Assert.assertFalse(changeState.isRecreateAPI(), "No need to Re-Create API");
        APIChangeState.copyChangedProps(testAPI1, testAPI2, changeState.getAllChanges());
        APIChangeState validatePropsAreCopied = new APIChangeState(testAPI1, testAPI2);
        Assert.assertTrue(!validatePropsAreCopied.hasAnyChanges(), "APIs are NOW equal");
        Assert.assertFalse(changeState.isRecreateAPI(), "No need to Re-Create API");
    }

    @Test
    public void testAPINameHasChanged() throws IOException {
        testAPI2.setName("New Name for API");
        APIImportParams.getInstance().setChangeOrganization(true);
        APIChangeState changeState = new APIChangeState(testAPI1, testAPI2);
        Assert.assertTrue(changeState.hasAnyChanges(), "There must be a change");
        Assert.assertEquals(changeState.getAllChanges().size(), 1);
        Assert.assertEquals(changeState.getBreakingChanges().size(), 0, "Name should not be a breaking change");
        Assert.assertEquals(changeState.getNonBreakingChanges().size(), 1, "Name is a breaking change");
        Assert.assertTrue(changeState.getAllChanges().contains("name"), "Expect the name as a changed prop");
        //Assert.assertFalse(changeState.isRecreateAPI(), "No need to Re-Create API");
        APIChangeState.copyChangedProps(testAPI1, testAPI2, changeState.getAllChanges());
        APIChangeState validatePropsAreCopied = new APIChangeState(testAPI1, testAPI2);
        Assert.assertTrue(!validatePropsAreCopied.hasAnyChanges(), "APIs are NOW equal");
       // Assert.assertFalse(changeState.isRecreateAPI(), "No need to Re-Create API");
    }

    @Test
    public void testTagsAreChanged() throws IOException {
        TagMap newTags = new TagMap();
        newTags.put("Group A", new String[]{"Value 1", "Value 2"});
        testAPI2.setTags(newTags);
        APIChangeState changeState = new APIChangeState(testAPI1, testAPI2);
        Assert.assertTrue(changeState.hasAnyChanges(), "APIs should not be eqaul");
        Assert.assertEquals(changeState.getAllChanges().size(), 1, "TagMaps is changed");
        Assert.assertTrue(changeState.getAllChanges().contains("tags"), "Expect the tags as a changed prop");
        Assert.assertFalse(changeState.isRecreateAPI(), "No need to Re-Create API");
        APIChangeState.copyChangedProps(testAPI1, testAPI2, changeState.getAllChanges());
        APIChangeState validatePropsAreCopied = new APIChangeState(testAPI1, testAPI2);
        Assert.assertTrue(!validatePropsAreCopied.hasAnyChanges(), "APIs are NOW equal");
        Assert.assertFalse(changeState.isRecreateAPI(), "No need to Re-Create API");
    }

    @Test
    public void testInboundProfilesAreChanged() throws IOException {
        Map<String, InboundProfile> inboundProfiles = new HashMap<>();
        InboundProfile profile = new InboundProfile();
        profile.setCorsProfile("_default");
        profile.setSecurityProfile("_somethingElse");
        profile.setMonitorAPI(false);
        profile.setMonitorSubject("This is my monitor");
        inboundProfiles.put("_default", profile);
        testAPI2.setInboundProfiles(inboundProfiles);
        APIChangeState changeState = new APIChangeState(testAPI1, testAPI2);
        Assert.assertTrue(changeState.hasAnyChanges(), "APIs should not be eqaul");
        Assert.assertEquals(changeState.getAllChanges().size(), 1, "InboundProfiles are changed");
        Assert.assertEquals(changeState.getBreakingChanges().size(), 1, "InboundProfiles are breaking");
        Assert.assertTrue(changeState.getAllChanges().contains("inboundProfiles"), "Expect the inboundProfile as a changed prop");
        Assert.assertFalse(changeState.isRecreateAPI(), "No need to Re-Create API");
        APIChangeState.copyChangedProps(testAPI1, testAPI2, changeState.getAllChanges());
        APIChangeState validatePropsAreCopied = new APIChangeState(testAPI1, testAPI2);
        Assert.assertTrue(!validatePropsAreCopied.hasAnyChanges(), "APIs are NOW equal");
        Assert.assertFalse(changeState.isRecreateAPI(), "No need to Re-Create API");
    }

    @Test
    public void testChangedAPIDefinition() throws IOException {
        APISpecification spec1 = new Swagger2xSpecification();
        spec1.parse("TEST API 1111111".getBytes());
        testAPI1.setApiDefinition(spec1);
        APISpecification spec2 = new Swagger2xSpecification();
        spec2.parse("TEST API 2222222".getBytes());
        testAPI2.setApiDefinition(spec2);
        APIChangeState changeState = new APIChangeState(testAPI1, testAPI2);
        Assert.assertTrue(changeState.isRecreateAPI(), "API needs to be recreated");
    }

    @Test
    public void testUnchangedAPIDefinition() throws IOException {
        APISpecification spec1 = new Swagger2xSpecification();
        spec1.parse("TEST API 3333333".getBytes());
        testAPI1.setApiDefinition(spec1);
        APISpecification spec2 = new Swagger2xSpecification();
        spec2.parse("TEST API 3333333".getBytes());
        testAPI2.setApiDefinition(spec2);
        APIChangeState changeState = new APIChangeState(testAPI1, testAPI2);
        Assert.assertFalse(changeState.isRecreateAPI(), "API-Definition is unchanged.");
    }

    @Test
    public void compareQuotasWithChanges() throws IOException{
        API existingAPI = getTestAPI("quota/existing.json");
        API newAPI = getTestAPI("quota/new.json");
        APIChangeState changeState = new APIChangeState(existingAPI, newAPI);
        Assert.assertTrue(changeState.hasAnyChanges());

    }

    @Test
    public void compareQuotasWithoutChanges() throws IOException{
        API existingAPI = getTestAPI("quota/existing.json");
        API newAPI = getTestAPI("quota/existing.json");
        APIChangeState changeState = new APIChangeState(existingAPI, newAPI);
        Assert.assertFalse(changeState.hasAnyChanges());

    }

    @Test
    public void compareQuotasWithEmptyAndNull() throws IOException{
        API existingAPI = getTestAPI("quota/empty_quota.json");
        API newAPI = getTestAPI("quota/null_quota.json");
        APIChangeState changeState = new APIChangeState(existingAPI, newAPI);
        Assert.assertFalse(changeState.hasAnyChanges());

    }



    @Test
    public void compareDesiredApplicationQuotaNullWithActualQuotaHavingValues() throws IOException {
        API actualAPI = getTestAPI("quota/existing.json");
        API desiredAPI = getTestAPI("quota/existing.json");
        desiredAPI.setApplicationQuota(null);
        Assert.assertNotNull(actualAPI.getApplicationQuota(), "Actual API must have an application quota");
        Assert.assertFalse(actualAPI.getApplicationQuota().getRestrictions().isEmpty(), "Actual application quota must have restrictions");

        APIChangeState changeState = new APIChangeState(actualAPI, desiredAPI);

        // Nothing is declared for the application quota, which means: keep what is configured in the API-Manager
        Assert.assertFalse(changeState.hasAnyChanges(), "A null desired application quota must not be treated as a change");
        Assert.assertFalse(changeState.getAllChanges().contains("applicationQuota"), "applicationQuota must not be reported as changed");
        Assert.assertFalse(changeState.isRecreateAPI(), "No need to Re-Create API");
        Assert.assertNotNull(actualAPI.getApplicationQuota(), "Actual application quota must be untouched");
        Assert.assertEquals(actualAPI.getApplicationQuota().getRestrictions().size(), 2, "Actual application quota restrictions must be untouched");
    }

    @Test
    public void compareDesiredApplicationQuotaNullWithChangedSystemQuota() throws IOException {
        API actualAPI = getTestAPI("quota/existing.json");
        API desiredAPI = getTestAPI("quota/new.json");
        desiredAPI.setApplicationQuota(null);

        APIChangeState changeState = new APIChangeState(actualAPI, desiredAPI);

        // Only the system quota differs, the null application quota must not show up as a change
        Assert.assertTrue(changeState.hasAnyChanges(), "System quota has changed");
        Assert.assertEquals(changeState.getAllChanges().size(), 1, "Only the system quota is changed");
        Assert.assertTrue(changeState.getAllChanges().contains("systemQuota"), "Expect the systemQuota as a changed prop");
        Assert.assertFalse(changeState.getAllChanges().contains("applicationQuota"), "applicationQuota must not be reported as changed");
    }

    @Test
    public void compareDesiredApplicationQuotaNullWithQuotaModeReplace() throws IOException {
        CoreParameters.Mode originalMode = CoreParameters.getInstance().getQuotaMode();
        CoreParameters.getInstance().setQuotaMode(CoreParameters.Mode.replace);
        try {
            API actualAPI = getTestAPI("quota/existing.json");
            API desiredAPI = getTestAPI("quota/existing.json");
            desiredAPI.setApplicationQuota(null);

            APIChangeState changeState = new APIChangeState(actualAPI, desiredAPI);

            // quotaMode replace: No application quota is desired, but one is set. It must be removed, hence it is a change.
            Assert.assertTrue(changeState.hasAnyChanges(), "A null desired application quota must be detected as change with quotaMode replace");
            Assert.assertEquals(changeState.getAllChanges().size(), 1, "Only the application quota is changed");
            Assert.assertTrue(changeState.getAllChanges().contains("applicationQuota"), "Expect the applicationQuota as a changed prop");
            Assert.assertFalse(changeState.isRecreateAPI(), "No need to Re-Create API");
        } finally {
            CoreParameters.getInstance().setQuotaMode(originalMode);
        }
    }

    @Test
    public void compareDesiredSystemQuotaNullWithQuotaModeReplace() throws IOException {
        CoreParameters.Mode originalMode = CoreParameters.getInstance().getQuotaMode();
        CoreParameters.getInstance().setQuotaMode(CoreParameters.Mode.replace);
        try {
            API actualAPI = getTestAPI("quota/existing.json");
            API desiredAPI = getTestAPI("quota/existing.json");
            desiredAPI.setSystemQuota(null);

            APIChangeState changeState = new APIChangeState(actualAPI, desiredAPI);

            Assert.assertEquals(changeState.getAllChanges().size(), 1, "Only the system quota is changed");
            Assert.assertTrue(changeState.getAllChanges().contains("systemQuota"), "Expect the systemQuota as a changed prop");
        } finally {
            CoreParameters.getInstance().setQuotaMode(originalMode);
        }
    }

    @Test
    public void compareDesiredQuotaNullWithQuotaModeAdd() throws IOException {
        CoreParameters.Mode originalMode = CoreParameters.getInstance().getQuotaMode();
        CoreParameters.getInstance().setQuotaMode(CoreParameters.Mode.add);
        try {
            API actualAPI = getTestAPI("quota/existing.json");
            API desiredAPI = getTestAPI("quota/existing.json");
            desiredAPI.setApplicationQuota(null);
            desiredAPI.setSystemQuota(null);

            APIChangeState changeState = new APIChangeState(actualAPI, desiredAPI);

            Assert.assertFalse(changeState.hasAnyChanges(), "With quotaMode add a null desired quota keeps the actual quota");
        } finally {
            CoreParameters.getInstance().setQuotaMode(originalMode);
        }
    }

    @Test
    public void compareDesiredQuotaNullWithQuotaModeReplaceAndNoActualQuota() throws IOException {
        CoreParameters.Mode originalMode = CoreParameters.getInstance().getQuotaMode();
        CoreParameters.getInstance().setQuotaMode(CoreParameters.Mode.replace);
        try {
            // Actual quotas are empty / without restrictions: Nothing to remove, and no failure for a quota without restrictions
            API actualAPI = getTestAPI("quota/empty_quota.json");
            API desiredAPI = getTestAPI("quota/null_quota.json");

            APIChangeState changeState = new APIChangeState(actualAPI, desiredAPI);

            Assert.assertFalse(changeState.hasAnyChanges(), "Nothing to remove, if no restrictions are set");
        } finally {
            CoreParameters.getInstance().setQuotaMode(originalMode);
        }
    }

    private API getTestAPI(String configFile) throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(TEST_PACKAGE + configFile);
        return mapper.readValue(is, API.class);
    }
}
