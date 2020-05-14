package com.axway.apim.adapter.apis;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.api.API;
import com.axway.apim.api.model.QuotaRestrictiontype;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIManagerAPIAdapterTest {
	
	private static final String testPackage = "com/axway/apim/adapter/apis/";
	
	ObjectMapper mapper = new ObjectMapper();
	APIManagerAPIAdapter existingApis;
	
	@BeforeClass
	private void initTestIndicator() {
		TestIndicator.getInstance().setTestRunning(true);
	}
	
	@Test
	public void duplicateVHost() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
			.hasApiPath("/api/test/DifferentVHostExportTestIT-531").build();

		List<API> apis = apiAdapter.getAPIs(filter, true);
		// We must find two APIs, as we not limited the search to the VHost
		Assert.assertEquals(apis.size(), 2, "Expected 2 APIs exposed on the same path with a different V-Host");
		
		Assert.assertEquals(apis.get(0).getPath(), "/api/test/DifferentVHostExportTestIT-531", "Found API not exposed on expected path");
		Assert.assertEquals(apis.get(0).getName(), "DifferentVHostExportTestIT-531", "Found API doesn't have the expected name.");
		Assert.assertEquals(apis.get(0).getVhost(), "vhost2.customer.com", "First API must be exposed on vhost2.customer.com");
		
		Assert.assertEquals(apis.get(1).getPath(), "/api/test/DifferentVHostExportTestIT-531", "Found API not exposed on expected path");
		Assert.assertEquals(apis.get(1).getName(), "DifferentVHostExportTestIT-531", "Found API doesn't have the expected name.");
		Assert.assertEquals(apis.get(1).getVhost(), "vhost1.customer.com", "First API must be exposed on vhost1.customer.com");
	}
	
	@Test
	public void restrictedOnVHost() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasApiPath("/api/test/DifferentVHostExportTestIT-531")
				.hasVHost("vhost2.customer.com")
				.build();
		List<API> apis = apiAdapter.getAPIs(filter, true);
		
		// We must find two APIs, as we not limited the search to the VHost
		Assert.assertEquals(apis.size(), 1, "Expected 1 APIs with requested V-Host");
		Assert.assertEquals(apis.get(0).getPath(), "/api/test/DifferentVHostExportTestIT-531", "Found API not exposed on expected path");
		Assert.assertEquals(apis.get(0).getName(), "DifferentVHostExportTestIT-531", "Found API doesn't have the expected name.");
		Assert.assertEquals(apis.get(0).getVhost(), "vhost2.customer.com", "First API must be exposed on vhost2.customer.com");
	}
	
	@Test
	public void nonExistingAPI() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasApiPath("/api/test/Not-ExistingAPI")
				.build();
		List<API> apis = apiAdapter.getAPIs(filter, true);

		Assert.assertEquals(apis.size(), 0, "It was not expected to find an API on path /api/test/Not-ExistingAPI");
	}
	
	@Test
	public void nonExistingUniqueAPI() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasApiPath("/api/test/Not-ExistingAPI")
				.build();
		API api = apiAdapter.getAPI(filter, true);

		Assert.assertNull(api);
	}
	
	@Test(expectedExceptions = AppException.class)
	public void resultMustBeUniqueButIsNot() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/ProxiesWithVHostDuplicates.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasApiPath("/api/test/DifferentVHostExportTestIT-531")
				.build();
		
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void reponseContainsOneAPIOnly() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/incompleteProxyAPI.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.build();
		List<API> apis = apiAdapter.getAPIs(filter, true);

		Assert.assertEquals(apis.size(), 1, "We expect one API to get back.");
	}	
	
	@Test
	public void nothingGivenToFilterTest() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/allProxies.json"));
		assertNotNull(apiManagerResponse);
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.build();
		List<API> apis = apiAdapter.getAPIs(filter, true);
		
		int numberOfAPIs = mapper.readTree(apiManagerResponse).size();

		Assert.assertEquals(apis.size(), numberOfAPIs, "We expect all APIs to get back in the list.");
	}
	
	@Test(expectedExceptions = AppException.class)
	public void getUniqueWithRoutingKeyNotOkay() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
		
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasQueryStringVersion("1.0")
				.build();
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void getUniqueWithRoutingKeyOK() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
		
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasQueryStringVersion("1.0")
				.hasApiPath("/api/emr/catalog")
				.build();
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void getUniqueWithRoutingKeyOK2() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
		
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasQueryStringVersion("1.1")
				.build();
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void getUniqueWithRoutingKeyVHostOK() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
			
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasQueryStringVersion("1.1")
				.hasVHost("api.customer.com")
				.build();
		apiAdapter.getAPI(filter, true);
	}
	
	@Test
	public void getUniqueWithRoutingKeyVHostOK2() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream("com/axway/apim/adapter/proxiesWithAPIRoutingKey.json"));
		assertNotNull(apiManagerResponse);
		
		APIAdapter apiAdapter = APIAdapter.create(APIManagerAdapter.getInstance());
		((APIManagerAPIAdapter)apiAdapter).setAPIManagerResponse(apiManagerResponse);
		
		APIFilter filter = new APIFilter.Builder()
				.hasQueryStringVersion("2.0")
				.hasVHost("api2.customer.com")
				.build();
		List<API> apis = apiAdapter.getAPIs(filter, true);

		Assert.assertEquals(apis.size(), 2);
	}
	
	@Test
	public void testTranslateMethodToName() throws AppException, IOException {
		String testMethods = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiMethods.json"));
		String testAPI = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiHavingMethods.json"));
		
		APIManagerAPIAdapter apiAdapter = new APIManagerAPIAdapter();
		apiAdapter.setAPIManagerResponse(testAPI);
		apiAdapter.methodAdapter.setAPIManagerTestResponse("72745ed9-f75b-428c-959c-b483eea497a1", testMethods);
		
		APIFilter filter = new APIFilter.Builder()
				.translateMethods(APIFilter.METHODS_AS_NAME)
				.hasId("72745ed9-f75b-428c-959c-b483eea497a1")
				.build();
		
		API api = apiAdapter.getAPI(filter, true);
		
		Assert.assertEquals(api.getOutboundProfiles().get("deletePet").getApiMethodId(), "deletePet");
		Assert.assertNotNull(api.getInboundProfiles().get("createUser"), "Expected to find Inbound profile named createdUser");
	}
	
	@Test
	public void testTranslatePolicyToExternalName() throws AppException, IOException {
		String testAPI = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiHavingMethods.json"));
		String routingPolicies = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "policies/routingPolicies.json"));
		String requestPolicies = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "policies/requestPolicies.json"));
		String responsePolicies = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "policies/responsePolicies.json"));
		String faultHandlerPolicies = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "policies/faultHandlerPolicies.json"));
		
		APIManagerAPIAdapter apiAdapter = new APIManagerAPIAdapter();
		apiAdapter.setAPIManagerResponse(testAPI);
		apiAdapter.policiesAdapter.apiManagerResponse.put(APIManagerPoliciesAdapter.REQUEST, requestPolicies);
		apiAdapter.policiesAdapter.apiManagerResponse.put(APIManagerPoliciesAdapter.ROUTING, routingPolicies);
		apiAdapter.policiesAdapter.apiManagerResponse.put(APIManagerPoliciesAdapter.RESPONSE, responsePolicies);
		apiAdapter.policiesAdapter.apiManagerResponse.put(APIManagerPoliciesAdapter.FAULT_HANDLER, faultHandlerPolicies);
		
		// Get the API to test with
		APIFilter filter = new APIFilter.Builder()
				.translatePolicies(APIFilter.TO_EXTERNAL_POLICY_NAME)
				.hasId("72745ed9-f75b-428c-959c-b483eea497a1")
				.build();
		
		API api = apiAdapter.getAPI(filter, true);
		
		Assert.assertEquals(api.getOutboundProfiles().get("_default").getRequestPolicy(), "Amazon V2 - DescribeInstances");
		Assert.assertEquals(api.getOutboundProfiles().get("_default").getRoutePolicy(), "Client App Registry Static Content Protection Policy");
		Assert.assertEquals(api.getOutboundProfiles().get("_default").getResponsePolicy(), "Routing");
		Assert.assertEquals(api.getOutboundProfiles().get("_default").getFaultHandlerPolicy(), "Default Fault Handler");
	}
	
	@Test
	public void loadAPIIncludingQuotaTest() throws AppException, IOException {
		String testAPI = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiHavingMethods.json"));
		String systemQuotas = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "quotas/systemAPIQuota.json"));
		String applicationDefaultQuotas = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "quotas/applicationDefaultQuota.json"));
		String testApplications = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "applications/allApplications.json"));
		String testAppAPIAccess = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiaccess/applicationAPIAccess.json"));
		String applicationQuota = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "quotas/applicationQuota.json"));
		
		APIManagerAPIAdapter apiAdapter = new APIManagerAPIAdapter();
		apiAdapter.setAPIManagerResponse(testAPI);
		apiAdapter.quotaAdapter.apiManagerResponse.put(APIManagerQuotaAdapter.SYSTEM_API_QUOTA, systemQuotas);
		apiAdapter.quotaAdapter.apiManagerResponse.put(APIManagerQuotaAdapter.APPLICATION_DEFAULT_QUOTA, applicationDefaultQuotas);
		apiAdapter.quotaAdapter.apiManagerResponse.put("ecf109cd-d012-4c57-897a-b3e8b041889b", applicationQuota);
		apiAdapter.accessAdapter.setAPIManagerTestResponse(APIManagerAPIAccessAdapter.Type.applications, "ecf109cd-d012-4c57-897a-b3e8b041889b", testAppAPIAccess);
		apiAdapter.appAdapter.setTestApiManagerResponse(new ClientAppFilter.Builder().build(), testApplications);
		
		
		APIFilter filter = new APIFilter.Builder()
				.includeQuotas(true)
				.includeClientApplications(true)
				.hasId("72745ed9-f75b-428c-959c-b483eea497a1")
				.build();
		
		API api = apiAdapter.getAPI(filter, true);
		
		Assert.assertNotNull(api.getSystemQuota(), "Should have a system quota");
		Assert.assertEquals(api.getSystemQuota().getRestrictions().size(), 1, "Expected one system quota restrictions");
		Assert.assertEquals(api.getSystemQuota().getRestrictions().get(0).getType(), QuotaRestrictiontype.throttle);
		Assert.assertEquals(api.getSystemQuota().getRestrictions().get(0).getConfig().get("messages"), "888");
		Assert.assertEquals(api.getSystemQuota().getRestrictions().get(0).getConfig().get("period"), "hour");
		Assert.assertEquals(api.getSystemQuota().getRestrictions().get(0).getConfig().get("per"), "1");
		
		Assert.assertNotNull(api.getApplicationQuota(), "Should have an application default quota");
		Assert.assertEquals(api.getApplicationQuota().getRestrictions().size(), 1, "Expected one system quota restrictions");
	}
	
	@Test
	public void loadAPIIncludingClientOrgs() throws AppException, IOException {
		String testAPI = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiHavingMethods.json"));
		String testOrganizations = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "organizations/organizations.json"));
		String testOrgsAPIAccess = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiaccess/organizationAPIAccess.json"));
		
		
		APIManagerAPIAdapter apiAdapter = new APIManagerAPIAdapter();
		apiAdapter.setAPIManagerResponse(testAPI);
		apiAdapter.orgAdapter.apiManagerResponse = testOrganizations;
		apiAdapter.accessAdapter.setAPIManagerTestResponse(APIManagerAPIAccessAdapter.Type.organizations, "d9ea6280-8811-4baf-8b5b-011a97142840", testOrgsAPIAccess);
		
		APIFilter filter = new APIFilter.Builder()
				.includeClientOrganizations(true)
				.hasId("72745ed9-f75b-428c-959c-b483eea497a1")
				.build();
		
		API api = apiAdapter.getAPI(filter, true);
		
		Assert.assertNotNull(api.getClientOrganizations(), "Should have a some client organizations");
		Assert.assertEquals(api.getClientOrganizations().size(), 1, "Expected client organization");
	}
	
	@Test
	public void loadAPIIncludingClientApps() throws AppException, IOException {
		String testAPI = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiHavingMethods.json"));
		//String testOrganizations = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "organizations/organizations.json"));
		String testApplications = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "applications/allApplications.json"));
		String testAppAPIAccess = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiaccess/applicationAPIAccess.json"));
		String applicationQuota = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "quotas/applicationQuota.json"));
		String systemQuotas = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "quotas/systemAPIQuota.json"));
		String applicationDefaultQuotas = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "quotas/applicationDefaultQuota.json"));		

		APIManagerAPIAdapter apiAdapter = new APIManagerAPIAdapter();
		apiAdapter.setAPIManagerResponse(testAPI);
		//apiAdapter.orgAdapter.apiManagerResponse = testOrganizations;
		
		apiAdapter.accessAdapter.setAPIManagerTestResponse(APIManagerAPIAccessAdapter.Type.applications, "ecf109cd-d012-4c57-897a-b3e8b041889b", testAppAPIAccess);
		apiAdapter.appAdapter.setTestApiManagerResponse(new ClientAppFilter.Builder().build(), testApplications);
		apiAdapter.quotaAdapter.apiManagerResponse.put(APIManagerQuotaAdapter.SYSTEM_API_QUOTA, systemQuotas);
		apiAdapter.quotaAdapter.apiManagerResponse.put(APIManagerQuotaAdapter.APPLICATION_DEFAULT_QUOTA, applicationDefaultQuotas);
		apiAdapter.quotaAdapter.apiManagerResponse.put("ecf109cd-d012-4c57-897a-b3e8b041889b", applicationQuota);
		
		APIFilter filter = new APIFilter.Builder()
				.includeClientApplications(true)
				.includeQuotas(true)
				.hasId("72745ed9-f75b-428c-959c-b483eea497a1")
				.build();
		
		API api = apiAdapter.getAPI(filter, true);
		
		Assert.assertNotNull(api.getApplications(), "Should have a some client applications");
		Assert.assertEquals(api.getApplications().get(0).getApiAccess().get(2).getApiId(), "72745ed9-f75b-428c-959c-b483eea497a1", "We should have a an API-Access for the test api");
		
		Assert.assertNotNull(api.getApplications(), "should have a subscribed application");
		Assert.assertNotNull(api.getApplications().get(0).getAppQuota(), "Subscribed application should have a quota");
	}
}
