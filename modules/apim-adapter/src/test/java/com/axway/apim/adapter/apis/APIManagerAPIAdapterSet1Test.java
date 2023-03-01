package com.axway.apim.adapter.apis;

import com.axway.apim.WiremockWrapper;
import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter.METHOD_TRANSLATION;
import com.axway.apim.adapter.apis.APIFilter.POLICY_TRANSLATION;
import com.axway.apim.api.API;
import com.axway.apim.api.model.APIMethod;
import com.axway.apim.api.model.InboundProfile;
import com.axway.apim.api.model.OutboundProfile;
import com.axway.apim.api.model.QuotaRestrictiontype;
import com.axway.apim.lib.CoreParameters;
import com.axway.apim.lib.error.AppException;
import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class APIManagerAPIAdapterSet1Test extends WiremockWrapper {

	private APIManagerAdapter apiManagerAdapter;

	@BeforeClass
	public void init() {
		try {
			super.initWiremock();
			CoreParameters coreParameters = new CoreParameters();
			coreParameters.setHostname("localhost");
			coreParameters.setUsername("apiadmin");
			coreParameters.setPassword(Utils.getEncryptedPassword());
			APIManagerAdapter.deleteInstance();
			apiManagerAdapter = APIManagerAdapter.getInstance();
		} catch (AppException e) {
			throw new RuntimeException(e);
		}
	}

	@AfterClass
	public void close() {
		super.close();
	}
	

	@Test
	public void loadActualAPI() throws IOException {
		APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.apiAdapter;
		APIFilter filter = new APIFilter.Builder()
				.hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
				.build();
		API api = apiManagerAPIAdapter.getAPI(filter, true);
		Assert.assertNotNull(api.getOrganization(), "API should have an organization");
	}
	
	@Test
	public void testTranslateMethodToName() throws IOException {
		APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.apiAdapter;
		APIFilter filter = new APIFilter.Builder()
				.translateMethods(METHOD_TRANSLATION.AS_NAME)
				.hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
				.build();
		API api = apiManagerAPIAdapter.getAPI(filter, true);
		
		Assert.assertEquals(api.getOutboundProfiles().size(), 2);
		Map<String, OutboundProfile> outboundProfiles = api.getOutboundProfiles();
		Assert.assertNotNull(outboundProfiles.get("_default"), "Expected to find default outbound profile");
		Assert.assertNotNull(outboundProfiles.get("deletePet").getApiMethodId(), "deletePet");
		Assert.assertEquals(outboundProfiles.get("deletePet").getApiId(), "1f4263ca-7f03-41d9-9d34-9eff79d29bd8");
		Assert.assertEquals(outboundProfiles.get("deletePet").getApiMethodId(), "7a0c6ce1-187b-47bc-b4dc-cfa1f12aa042");
		Assert.assertEquals(outboundProfiles.get("deletePet").getApiMethodName(), "deletePet");
		
		Assert.assertEquals(api.getInboundProfiles().size(), 2);
		Map<String, InboundProfile> inboundProfiles = api.getInboundProfiles();
		Assert.assertNotNull(inboundProfiles.get("_default"), "Expected to find default outbound profile");
		Assert.assertNotNull(inboundProfiles.get("createUser"), "Expected to find Inbound profile named createdUser");
	}
	
	@Test
	public void testTranslateMethodToId() throws IOException {
		APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.apiAdapter;
		APIFilter filter = new APIFilter.Builder()
				.hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
				.build();
		API api = apiManagerAPIAdapter.getAPI(filter, true);
		
		Assert.assertEquals(api.getOutboundProfiles().size(), 2);
		Map<String, OutboundProfile> outboundProfiles = api.getOutboundProfiles();
		Assert.assertNotNull(outboundProfiles.get("_default"), "Expected to find default outbound profile");
		Assert.assertNotNull(outboundProfiles.get("bf742930-5fdd-4e49-b9ae-b8fdecd37ffa"), "Expected to get deletePet based on the method per ID");
		
		Assert.assertEquals(api.getInboundProfiles().size(), 2);
		Map<String, InboundProfile> inboundProfiles = api.getInboundProfiles();
		Assert.assertNotNull(inboundProfiles.get("_default"), "Expected to find default outbound profile");
		Assert.assertNotNull(inboundProfiles.get("a544b66c-6f2b-4c00-999c-6816d482bcde"), "Expected to get createUser based on the method per ID");
	}
	
	@Test
	public void testTranslatePolicyToExternalName() throws IOException {
		APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.apiAdapter;
		// Get the API to test with
		APIFilter filter = new APIFilter.Builder()
				.translatePolicies(POLICY_TRANSLATION.TO_NAME)
				.hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
				.build();
		
		API api = apiManagerAPIAdapter.getAPI(filter, true);
		
		Assert.assertEquals(api.getOutboundProfiles().get("_default").getRequestPolicy().getName(), "Validate Size & Token");
		Assert.assertEquals(api.getOutboundProfiles().get("_default").getRoutePolicy().getName(), "Http Proxy Router");
		Assert.assertEquals(api.getOutboundProfiles().get("_default").getResponsePolicy().getName(), "Remove Header & Audit data");
		Assert.assertEquals(api.getOutboundProfiles().get("_default").getFaultHandlerPolicy().getName(), "Default Fault Handler");
	}
	
	@Test
	public void loadAPIIncludingQuota() throws IOException {
		APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.apiAdapter;
		APIFilter filter = new APIFilter.Builder()
				.includeQuotas(true)
				.includeClientApplications(true)
				.hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
				.build();
		
		API api = apiManagerAPIAdapter.getAPI(filter, true);
		
		Assert.assertNotNull(api.getSystemQuota(), "Should have a system quota");
		Assert.assertEquals(api.getSystemQuota().getRestrictions().size(), 1, "Expected one system quota restrictions");
		Assert.assertEquals(api.getSystemQuota().getRestrictions().get(0).getType(), QuotaRestrictiontype.throttle);
		Assert.assertEquals(api.getSystemQuota().getRestrictions().get(0).getConfig().get("messages"), "1000");
		Assert.assertEquals(api.getSystemQuota().getRestrictions().get(0).getConfig().get("period"), "second");
		Assert.assertEquals(api.getSystemQuota().getRestrictions().get(0).getConfig().get("per"), "2");
		
		Assert.assertNotNull(api.getApplicationQuota(), "Should have an application default quota");
		Assert.assertEquals(api.getApplicationQuota().getRestrictions().size(), 1, "Expected one system quota restrictions");
	}
	
	@Test
	public void loadAPIIncludingClientOrgs() throws IOException {
		APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.apiAdapter;
		APIFilter filter = new APIFilter.Builder()
				.includeClientOrganizations(true)
				.hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
				.build();
		
		API api = apiManagerAPIAdapter.getAPI(filter, true);
		
		Assert.assertNotNull(api.getClientOrganizations(), "Should have a some client organizations");
		Assert.assertEquals(api.getClientOrganizations().size(), 2, "Expected client organization");
	}
	
	@Test
	public void loadAPIIncludingClientApps() throws IOException {
		APIManagerAPIAdapter apiManagerAPIAdapter = apiManagerAdapter.apiAdapter;
		APIFilter filter = new APIFilter.Builder()
				.includeClientApplications(true)
				.includeQuotas(true)
				.hasId("e4ded8c8-0a40-4b50-bc13-552fb7209150")
				.build();
		API api = apiManagerAPIAdapter.getAPI(filter, true);
		
		Assert.assertNotNull(api.getApplications(), "Should have a some client applications");
		Assert.assertEquals(api.getApplications().size(), 1, "Should have a some client applications");
		Assert.assertEquals(api.getApplications().get(0).getId(), "5feb31fe-fb8b-4c21-8132-39f01e2c6440", "We should have a an API-Access for the test api");
		
		Assert.assertNotNull(api.getApplications(), "should have a subscribed application");
		Assert.assertNotNull(api.getApplications().get(0).getAppQuota(), "Subscribed application should have a quota");
	}
	
	@Test
	public void testGetAllAPIMethods() throws IOException {
		APIManagerAPIMethodAdapter methodAdapter = apiManagerAdapter.methodAdapter;
		List<APIMethod> methods = methodAdapter.getAllMethodsForAPI("e4ded8c8-0a40-4b50-bc13-552fb7209150");

		// We must find two APIs, as we not limited the search to the VHost
		Assert.assertEquals(methods.size(), 19, "Expected 19 APIMethods");
		APIMethod method = methods.get(0);
		
		Assert.assertEquals(method.getName(), "logoutUser");
		Assert.assertEquals(method.getSummary(), "Logs out current logged in user session");
	}
	
	@Test
	public void testGetMethodForName() throws IOException {
		APIManagerAPIMethodAdapter methodAdapter = apiManagerAdapter.methodAdapter;
		APIMethod method = methodAdapter.getMethodForName("e4ded8c8-0a40-4b50-bc13-552fb7209150", "deletePet");
		Assert.assertEquals(method.getName(), "deletePet");
	}
}
