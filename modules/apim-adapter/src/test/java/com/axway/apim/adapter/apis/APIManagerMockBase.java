package com.axway.apim.adapter.apis;

import java.io.IOException;

import com.axway.apim.adapter.APIManagerAdapter;
import org.testng.reporters.Files;

import com.axway.apim.adapter.APIManagerAdapterTest;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.adapter.user.UserFilter;
import com.axway.apim.api.model.Image;
import com.axway.apim.lib.utils.TestIndicator;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class APIManagerMockBase {

    protected static final String testPackage = "com/axway/apim/adapter/apimanager/testSet1/";

    protected APIManagerAPIAdapter apiAdapter;

    protected ObjectMapper mapper = new ObjectMapper();

    public APIManagerMockBase() {
    }

    protected void setupMockData() throws IOException {
        TestIndicator.getInstance().setTestRunning(true);
        APIManagerAdapter.apiManagerVersion = "7.7.20200130";
        APIManagerAdapter apim = APIManagerAdapter.getInstance();
        APIManagerAdapter.getInstance().configAdapter.setAPIManagerTestResponse(Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "config/configAsAdmin.json")), true);
        APIManagerAdapter.getInstance().configAdapter.setAPIManagerTestResponse(Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "config/configAsOrgAdmin.json")), false);
        apiAdapter = APIManagerAdapter.getInstance().apiAdapter;
        apim.configAdapter.setAPIManagerTestResponse("{ \"productVersion\": \"7.7.20200130\" }", false);
        apim.methodAdapter.setAPIManagerTestResponse("72745ed9-f75b-428c-959c-b483eea497a1", Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiMethods.json")));
        apim.methodAdapter.setAPIManagerTestResponse("72745ed9-f75b-428c-959c-99999999", Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiMethodsUsedWithMethodNames.json")));
        String testAPI1 = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiHavingMethods.json"));
        String testAPI2 = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiHavingMethodsWithMethodsNames.json"));
        String systemQuotas = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "quotas/systemAPIQuota.json"));
        String applicationDefaultQuotas = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "quotas/applicationDefaultQuota.json"));
        String testApplications = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "applications/allApplications.json"));
        String grantedAppsForAPI = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "applications/grantedAppsToAPI.json"));
        String testAppAPIAccess = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiaccess/applicationAPIAccess.json"));
        String applicationQuota = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "quotas/applicationQuota.json"));
        String routingPolicies = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "policies/routingPolicies.json"));
        String requestPolicies = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "policies/requestPolicies.json"));
        String responsePolicies = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "policies/responsePolicies.json"));
        String faultHandlerPolicies = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "policies/faultHandlerPolicies.json"));
        String testOrganizations = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "organizations/organizations.json"));
        String fhirOrganization = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "organizations/fhir-organization.json"));
        String singleOrganization = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "organizations/singleOrg.json"));
        String testOrgsAPIAccess = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiaccess/organizationAPIAccess.json"));

        String oauthClientProfile = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "clientProfiles/OAuthClientProfile.json"));
        String noCustomPropertiesConfig = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "customProperties/noCustomPropertiesConfig.json"));


        apiAdapter.setAPIManagerResponse(new APIFilter.Builder().hasId("72745ed9-f75b-428c-959c-b483eea497a1").build(), testAPI1);
        apiAdapter.setAPIManagerResponse(new APIFilter.Builder().hasName("apiName-routeKeyD").build(), testAPI1); // Register the same API also based on the API-Name
        apiAdapter.setAPIManagerResponse(new APIFilter.Builder().hasApiPath("/query-string-api-oadmin-839").build(), testAPI1); // Register the same API also based on the API-Path
        apiAdapter.setAPIManagerResponse(new APIFilter.Builder().hasId("72745ed9-f75b-428c-959c-99999999").build(), testAPI2);
        apiAdapter.setAPIManagerResponse("72745ed9-f75b-428c-959c-b483eea497a1", new Image());
        apiAdapter.setAPIManagerResponse("72745ed9-f75b-428c-959c-99999999", new Image());
        apim.policiesAdapter.apiManagerResponse.put(APIManagerPoliciesAdapter.PolicyType.REQUEST, requestPolicies);
        apim.policiesAdapter.apiManagerResponse.put(APIManagerPoliciesAdapter.PolicyType.ROUTING, routingPolicies);
        apim.policiesAdapter.apiManagerResponse.put(APIManagerPoliciesAdapter.PolicyType.RESPONSE, responsePolicies);
        apim.policiesAdapter.apiManagerResponse.put(APIManagerPoliciesAdapter.PolicyType.FAULT_HANDLER, faultHandlerPolicies);
        apim.policiesAdapter.apiManagerResponse.put(APIManagerPoliciesAdapter.PolicyType.GLOBAL_FAULT_HANDLER, faultHandlerPolicies);
        apim.quotaAdapter.apiManagerResponse.put(APIManagerQuotaAdapter.Quota.SYSTEM_DEFAULT.getQuotaId(), systemQuotas);
        apim.quotaAdapter.apiManagerResponse.put(APIManagerQuotaAdapter.Quota.APPLICATION_DEFAULT.getQuotaId(), applicationDefaultQuotas);
        apim.quotaAdapter.apiManagerResponse.put("ecf109cd-d012-4c57-897a-b3e8b041889b", applicationQuota);
        apim.accessAdapter.setAPIManagerTestResponse(APIManagerAPIAccessAdapter.Type.applications, "ecf109cd-d012-4c57-897a-b3e8b041889b", testAppAPIAccess);
        apim.accessAdapter.setAPIManagerTestResponse(APIManagerAPIAccessAdapter.Type.organizations, "d9ea6280-8811-4baf-8b5b-011a97142840", testOrgsAPIAccess);
        apim.appAdapter.setTestApiManagerResponse(new ClientAppFilter.Builder().build(), testApplications);
        apim.appAdapter.setTestSubscribedAppAPIManagerResponse("72745ed9-f75b-428c-959c-b483eea497a1", grantedAppsForAPI);
        // Org: API-Development
        apim.orgAdapter.setAPIManagerTestResponse(new OrgFilter.Builder().hasId("d9ea6280-8811-4baf-8b5b-011a97142840").build(), testOrganizations);
        apim.orgAdapter.setAPIManagerTestResponse(new OrgFilter.Builder().hasName("API Development").build(), testOrganizations);
        // Org: Singe Result org
        apim.orgAdapter.setAPIManagerTestResponse(new OrgFilter.Builder().build(), testOrganizations); // Used for all orgs query
        apim.orgAdapter.setAPIManagerTestResponse(new OrgFilter.Builder().hasName("API Development 1").build(), "[" + singleOrganization + "]");
        // Org: FHIR
        apim.orgAdapter.setAPIManagerTestResponse(new OrgFilter.Builder().hasId("2efca39a-2572-4b62-8d0f-53241d93d362").build(), fhirOrganization);
        apim.orgAdapter.setAPIManagerTestResponse(new OrgFilter.Builder().hasName("FHIR").build(), fhirOrganization);
        // OAuth Client Profile: Sample OAuth Client Profile
        apim.oauthClientAdapter.setAPIManagerTestResponse(oauthClientProfile);

        apim.customPropertiesAdapter.setAPIManagerTestResponse(noCustomPropertiesConfig);
    }

}
