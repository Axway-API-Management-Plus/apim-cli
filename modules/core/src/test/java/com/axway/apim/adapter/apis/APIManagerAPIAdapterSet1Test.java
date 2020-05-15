package com.axway.apim.adapter.apis;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import com.axway.apim.api.API;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.api.model.QuotaRestrictiontype;
import com.axway.apim.lib.errorHandling.AppException;

public class APIManagerAPIAdapterSet1Test extends APIManagerMockBase {
	
	private static final String testPackage = "com/axway/apim/adapter/apimanager/testSet1/";
	
	@BeforeClass
	private void initTestIndicator() throws AppException, IOException {
		setupMockData();
	}
	
	@Test
	public void loadActualAPI() throws AppException, IOException {		
		APIFilter filter = new APIFilter.Builder()
				.hasId("72745ed9-f75b-428c-959c-b483eea497a1")
				.build();
		API api = apiAdapter.getAPI(filter, true);
		
		Assert.assertNotNull(api.getOrganization(), "API should have an organization");
	}
	
	@Test
	public void testTranslateMethodToName() throws AppException, IOException {		
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
		// Get the API to test with
		APIFilter filter = new APIFilter.Builder()
				.translatePolicies(APIFilter.TO_EXTERNAL_POLICY_NAME)
				.hasId("72745ed9-f75b-428c-959c-b483eea497a1")
				.build();
		
		API api = apiAdapter.getAPI(filter, true);
		
		Assert.assertEquals(api.getOutboundProfiles().get("_default").getRequestPolicy().getName(), "Amazon V2 - DescribeInstances");
		Assert.assertEquals(api.getOutboundProfiles().get("_default").getRoutePolicy().getName(), "Client App Registry Static Content Protection Policy");
		Assert.assertEquals(api.getOutboundProfiles().get("_default").getResponsePolicy().getName(), "Routing");
		Assert.assertEquals(api.getOutboundProfiles().get("_default").getFaultHandlerPolicy().getName(), "Default Fault Handler");
	}
	
	@Test
	public void loadAPIIncludingQuota() throws AppException, IOException {		
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
		APIFilter filter = new APIFilter.Builder()
				.includeClientOrganizations(true)
				.hasId("d9ea6280-8811-4baf-8b5b-011a97142840")
				.build();
		
		API api = apiAdapter.getAPI(filter, true);
		
		Assert.assertNotNull(api.getClientOrganizations(), "Should have a some client organizations");
		Assert.assertEquals(api.getClientOrganizations().size(), 1, "Expected client organization");
	}
	
	@Test
	public void loadAPIIncludingClientApps() throws AppException, IOException {		
		APIFilter filter = new APIFilter.Builder()
				.includeClientApplications(true)
				.includeQuotas(true)
				.hasId("72745ed9-f75b-428c-959c-b483eea497a1")
				.build();
		
		API api = apiAdapter.getAPI(filter, true);
		
		Assert.assertNotNull(api.getApplications(), "Should have a some client applications");
		Assert.assertEquals(api.getApplications().size(), 1, "Should have a some client applications");
		Assert.assertEquals(api.getApplications().get(0).getId(), "ecf109cd-d012-4c57-897a-b3e8b041889b", "We should have a an API-Access for the test api");
		
		Assert.assertNotNull(api.getApplications(), "should have a subscribed application");
		Assert.assertNotNull(api.getApplications().get(0).getAppQuota(), "Subscribed application should have a quota");
	}
	
	@Test
	public void testGetAllAPIMethods() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiMethods.json"));
		assertNotNull(apiManagerResponse);
		APIManagerAPIMethodAdapter methodAdapter = new APIManagerAPIMethodAdapter();
		methodAdapter.setAPIManagerTestResponse("1234567890", apiManagerResponse);
		
		List<APIMethod> methods = methodAdapter.getAllMethodsForAPI("1234567890");

		// We must find two APIs, as we not limited the search to the VHost
		Assert.assertEquals(methods.size(), 20, "Expected 20 APIMethods");
		APIMethod method = methods.get(0);
		
		Assert.assertEquals(method.getName(), "updatePet");
		Assert.assertEquals(method.getSummary(), "Update an existing pet");
	}
	
	@Test
	public void testGetMethodIdPerName() throws AppException, IOException {
		String apiManagerResponse = Files.readFile(this.getClass().getClassLoader().getResourceAsStream(testPackage + "apiMethods.json"));
		assertNotNull(apiManagerResponse);
		APIManagerAPIMethodAdapter methodAdapter = new APIManagerAPIMethodAdapter();
		methodAdapter.setAPIManagerTestResponse("1234567890", apiManagerResponse);
		
		String methodId = methodAdapter.getMethodIdPerName("1234567890", "deletePet");

		Assert.assertEquals(methodId, "132712e4-c132-4298-bfa0-f6f5c811ad65");
	}
}
