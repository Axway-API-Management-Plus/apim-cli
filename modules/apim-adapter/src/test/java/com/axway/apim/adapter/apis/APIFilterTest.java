package com.axway.apim.adapter.apis;

import com.axway.apim.adapter.APIManagerAdapter;
import com.axway.apim.adapter.apis.APIFilter.Builder.APIType;
import com.axway.apim.adapter.apis.APIManagerPoliciesAdapter.PolicyType;
import com.axway.apim.api.API;
import com.axway.apim.api.model.*;
import com.axway.apim.lib.error.AppException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class APIFilterTest {
	
	@BeforeClass
	public void setupTestIndicator() {
		APIManagerAdapter.apiManagerVersion = "7.7";
	}
	
	@AfterClass
	public void removeTestIndicator() {
		APIManagerAdapter.apiManagerVersion = null;
	}
	
	@Test
	public void testStandardActualAPI() {
		APIFilter filter = new APIFilter.Builder(APIType.ACTUAL_API).build();
		assertTrue(filter.isIncludeClientApplications());
		assertTrue(filter.isIncludeClientOrganizations());
		assertTrue(filter.isIncludeQuotas());
		assertTrue(filter.isIncludeOriginalAPIDefinition());
	}
	
	@Test
	public void testCustomActualAPI() {
		APIFilter filter = new APIFilter.Builder(APIType.ACTUAL_API)
				.includeClientApplications(false)
				.includeClientOrganizations(false)
				.includeQuotas(false)
				.build();
		assertFalse(filter.isIncludeClientApplications());
		assertFalse(filter.isIncludeClientOrganizations());
		assertFalse(filter.isIncludeQuotas());
		assertTrue(filter.isIncludeOriginalAPIDefinition());
	}
	
	@Test
	public void filterWithId() {
		APIFilter filter = new APIFilter.Builder()
				.hasId("9878973123")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 0);
		Assert.assertEquals(filter.getId(), "9878973123");
	}
	
	@Test
	public void filterWithPath() {
		// For this test, we must simulate API-Manager version >7.7
		APIManagerAdapter.apiManagerVersion = "7.7.20200130";
		APIFilter filter = new APIFilter.Builder()
				.hasApiPath("/v1/api")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "path");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "/v1/api");
	}

	
	@Test
	public void filterWithName() {
		APIFilter filter = new APIFilter.Builder()
				.hasName("The name I want")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "name");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "The name I want");
	}
	
	@Test
	public void hasFullWildCardName() {
		APIFilter filter = new APIFilter.Builder()
				.hasName("*")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 0);
	}
	
	@Test
	public void filterWithBackendApiID() {
		APIFilter filter = new APIFilter.Builder()
				.hasApiId("7868768768")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "apiid");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "7868768768");
	}
	
	@Test
	public void filterWithDeprecated() {
		APIFilter filter = new APIFilter.Builder()
				.isDeprecated(true)
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "deprecated");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "true");
	}
	
	@Test
	public void filterWithRetired() {
		APIFilter filter = new APIFilter.Builder()
				.isRetired(true)
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "retired");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "true");
	}
	
	@Test
	public void filterWithState() {
		APIFilter filter = new APIFilter.Builder()
				.hasState("unpublished")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 3);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "state");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "eq");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "unpublished");
	}
	
	@Test
	public void filterWithCreatedOn() {
		APIFilter filter = new APIFilter.Builder()
				.isCreatedOnAfter("1623915264000")	// 17.06.2021
				.isCreatedOnBefore("1624865664000") // 28.06.2021
				.build();
		Assert.assertEquals(filter.getFilters().size(), 6);
		Assert.assertEquals(filter.getFilters().get(0).getValue(), "createdOn");
		Assert.assertEquals(filter.getFilters().get(1).getValue(), "gt");
		Assert.assertEquals(filter.getFilters().get(2).getValue(), "1623915264000");
		Assert.assertEquals(filter.getFilters().get(3).getValue(), "createdOn");
		Assert.assertEquals(filter.getFilters().get(4).getValue(), "lt");
		Assert.assertEquals(filter.getFilters().get(5).getValue(), "1624865664000");
	}
	
	@Test
	public void apiFilterEqualTest() {
		APIFilter filter1 = new APIFilter.Builder()
				.hasId("12345")
				.build();
		
		APIFilter filter2 = new APIFilter.Builder()
				.hasId("12345")
				.build();
		
		Assert.assertEquals(filter1, filter2, "Both filters should be equal");
	}
	
	@Test
	public void testBackendBasepathFilter() {
		APIFilter filter = new APIFilter.Builder()
				.hasBackendBasepath("*emr-system*")
				.build();
		API testAPI = getAPIWithBackendBasepath("http://emr-system:8081");
		assertFalse(filter.filter(testAPI), "API with base-path: http://emr-system:8081 should match to filter: *emr-system*");
		testAPI = getAPIWithBackendBasepath("http://sec.hipaa:8086");
		assertTrue(filter.filter(testAPI), "API with base-path: http://sec.hipaa:8086 should NOT match to filter: *emr-system*");
		
		filter = new APIFilter.Builder()
				.hasBackendBasepath("http://emr-system:8081")
				.build();
		
		testAPI = getAPIWithBackendBasepath("http://emr-system:8081");
		assertFalse(filter.filter(testAPI), "API with base-path: http://emr-system:8081 should match to filter: http://emr-system:8081");
		
		testAPI = getAPIWithBackendBasepath("http://fhir3.healthintersections.com.au");
		assertTrue(filter.filter(testAPI), "API with base-path: http://fhir3.healthintersections.com.au should NOT match to filter: http://emr-system:8081");
	}
	
	@Test
	public void filterAPIsWithOrganization() {
		API testAPI = new API();
		Organization testOrg = new Organization();
		testOrg.setName("Another Org");
		testAPI.setOrganization(testOrg);
		
		APIFilter filter = new APIFilter.Builder().hasOrganization("Test-Org").build();
		assertTrue(filter.filter(testAPI), "API with organization: Another Org should not match to filter Test-org");
		
		filter = new APIFilter.Builder().hasOrganization("*Org").build();
		assertFalse(filter.filter(testAPI), "API with organization Another Org should match to filter *Org");
		
		filter = new APIFilter.Builder().hasOrganization("*XXX").build();
		assertTrue(filter.filter(testAPI), "API with organization Another Org should match to filter *XXX");
	}
	
	@Test
	public void testPolicyFilter() throws AppException {
		APIFilter filter = new APIFilter.Builder()
				.hasPolicyName("*Policy*")
				.build();
		API testAPI = new API();
		addPolicy(testAPI, "Request Policy 1", PolicyType.REQUEST);
		addPolicy(testAPI, "Routing Policy 1", PolicyType.ROUTING);
		assertFalse(filter.filter(testAPI), "API must match to pattern '*Policy*'");
		
		filter = new APIFilter.Builder()
				.hasPolicyName("*Response*")
				.build();
		testAPI = new API();
		addPolicy(testAPI, "Request Policy 1", PolicyType.REQUEST);
		addPolicy(testAPI, "Response Policy 1", PolicyType.RESPONSE);
		assertFalse(filter.filter(testAPI), "API must match to pattern '*Response*'");
		
		filter = new APIFilter.Builder()
				.hasPolicyName("*Routing*")
				.build();
		testAPI = new API();
		addPolicy(testAPI, "Request Policy 1", PolicyType.REQUEST);
		addPolicy(testAPI, "Response Policy 1", PolicyType.RESPONSE);
		assertTrue(filter.filter(testAPI), "API must NOT match to pattern '*Response*'");
		
		filter = new APIFilter.Builder()
				.hasPolicyName("Response Policy 1")
				.build();
		testAPI = new API();
		addPolicy(testAPI, "Response Policy 1", PolicyType.RESPONSE);
		assertFalse(filter.filter(testAPI), "API must match to pattern 'Response Policy 1'");
		
		filter = new APIFilter.Builder()
				.hasPolicyName("Response Policy 2")
				.build();
		testAPI = new API();
		addPolicy(testAPI, "Response Policy 1", PolicyType.RESPONSE);
		assertTrue(filter.filter(testAPI), "API must NOT match to pattern 'Response Policy 2' as it is using 'Response Policy 1'");
		
		filter = new APIFilter.Builder()
				.hasPolicyName("Not used policy")
				.build();
		testAPI = new API();
		addPolicy(testAPI, "Response Policy 1", PolicyType.RESPONSE);
		assertTrue(filter.filter(testAPI), "API must NOT match to pattern 'Not used policy' as it is using 'Response Policy 1'");
	}

	@Test
	public void testPolicyFilterWithSpecialCaracter() throws AppException {
		APIFilter filter = new APIFilter.Builder()
			.hasPolicyName("*(Policy)*")
			.build();
		API testAPI = new API();
		addPolicy(testAPI, "Request (Policy) 1", PolicyType.REQUEST);
		assertFalse(filter.filter(testAPI), "API must match to pattern '*(Policy)*'");

		filter = new APIFilter.Builder(){}
			.hasPolicyName("()*{}()[].+?^$|")
			.build();
		testAPI = new API();
		addPolicy(testAPI, "(){}()[].+?^$|", PolicyType.REQUEST);
		assertFalse(filter.filter(testAPI), "API must match to pattern '*(Policy)*'");

		filter = new APIFilter.Builder(){}
			.hasPolicyName("*(policy)*")
			.build();
		testAPI = new API();
		addPolicy(testAPI, "Request policy", PolicyType.REQUEST);
		assertTrue(filter.filter(testAPI), "API must not match to pattern '*(Policy)*'");
	}
	
	@Test
	public void testTagFilter() {
		API testAPI = new API();
		TagMap tags = new TagMap();
		tags.put("group1", new String[] {"tagValue1", "tagValue2"});
		tags.put("group2", new String[] {"tagValue3", "tagValue4"});
		testAPI.setTags(tags);

		APIFilter filter = new APIFilter.Builder().hasTag("*tagValue3*").build();
		assertFalse(filter.filter(testAPI), "API must match to pattern '*TAGValue3*'");
		
		filter = new APIFilter.Builder().hasTag("*unknownTag*").build();
		assertTrue(filter.filter(testAPI), "API must NOT match to pattern '*unknownTag*'");
		
		filter = new APIFilter.Builder().hasTag("GROUP1").build();
		assertFalse(filter.filter(testAPI), "API must match to pattern 'GROUP1'");
		
		filter = new APIFilter.Builder().hasTag("*OUP*").build();
		assertFalse(filter.filter(testAPI), "API must match to pattern '*OUP2'");
		
		// Testing Group specific tag filtering
		filter = new APIFilter.Builder().hasTag("GROUP1=*VALUE2").build();
		assertFalse(filter.filter(testAPI), "API must match to pattern 'GROUP1=*VALUE2'");
		
		filter = new APIFilter.Builder().hasTag("GROUP2=*VALUE2").build();
		assertTrue(filter.filter(testAPI), "API must NOT match to pattern 'GROUP2=*VALUE2'");
		
		filter = new APIFilter.Builder().hasTag("GROUP1=*VALUE3").build();
		assertTrue(filter.filter(testAPI), "API must NOT match to pattern 'GROUP1=*VALUE3'");
	}
	
	@Test
	public void testInboundSecurityPolicyFilter() {
		API testAPI = new API();
		addInboundSecurityPolicy(testAPI, "Inbound Security Policy 1");
		
		APIFilter filter = new APIFilter.Builder()
				.hasPolicyName("Inbound Security*")
				.build();
		assertFalse(filter.filter(testAPI), "API must match to pattern 'Inbound Security*'");
	}
	
	@Test
	public void testInboundSecurity() {
		API testAPI = new API();
		addInboundSecurityPolicy(testAPI, "Inbound Security Policy 1");
		
		APIFilter filter = new APIFilter.Builder().hasInboundSecurity("inbound security*").build();
		assertFalse(filter.filter(testAPI), "API must match to pattern 'Inbound Security*'");
		
		filter = new APIFilter.Builder().hasInboundSecurity("*Test-Policy*").build();
		assertTrue(filter.filter(testAPI), "API must match to pattern 'Inbound Security*'");
		
		testAPI = new API();
		addInboundSecurityToAPI(testAPI, DeviceType.apiKey);
		
		filter = new APIFilter.Builder().hasInboundSecurity("apikey").build();
		assertFalse(filter.filter(testAPI), "API has API-Key secured");
		filter = new APIFilter.Builder().hasInboundSecurity("api-KEY").build();
		assertFalse(filter.filter(testAPI), "API has API-Key secured");
		
		testAPI = new API();
		addInboundSecurityToAPI(testAPI, DeviceType.oauthExternal);
		Map<String, String> properties = new HashMap<>();
		properties.put("tokenStore", "My Token information policy");

		filter = new APIFilter.Builder().hasInboundSecurity("oauth-ext").build();
		assertFalse(filter.filter(testAPI), "API is OAUth external secured");
		// Cannot be UNIT-Tested for now, as SecurityDevice.getProperties requires a running API-Manager
		// Needs to be re-worked to an adapter
		/*filter = new APIFilter.Builder().hasInboundSecurity("oauth").build();
		assertTrue(filter.filter(testAPI), "API is not OAuth secured");
		filter = new APIFilter.Builder().hasInboundSecurity("*TOKEN information*").build();
		assertFalse(filter.filter(testAPI), "Should match, as the policy i");*/
	}
	
	@Test
	public void testOutboundSecurity() throws AppException {
		API testAPI = new API();
		addOutboundSecurityToAPI(testAPI, AuthType.http_basic);
		
		APIFilter filter = new APIFilter.Builder().hasOutboundAuthentication("HTTP-basic").build();
		assertFalse(filter.filter(testAPI), "API must match as outbound AuthN is HTTP-Basic");
		
		testAPI = new API();
		addOutboundSecurityToAPI(testAPI, AuthType.oauth);
		
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("providerProfile", "<key type='AuthProfilesGroup'><id field='name' value='Auth Profiles'/><key type='OAuthGroup'><id field='name' value='OAuth2'/><key type='OAuthProviderProfile'><id field='name' value='API Gateway'/><key type='OAuthAppProfile'><id field='name' value='Sample Client Authzcode App'/></key></key></key></key>");
		testAPI.getAuthenticationProfiles().get(0).setParameters(parameters);
		filter = new APIFilter.Builder().hasOutboundAuthentication("oauth").build();
		assertFalse(filter.filter(testAPI), "API must match as outbound AuthN is OAuth");
		filter = new APIFilter.Builder().hasOutboundAuthentication("Sample*").build();
		assertFalse(filter.filter(testAPI), "API must match also match based on the selected OAuth-Client App");
		parameters.put("providerProfile", "Sample Client Authzcode App");
		filter = new APIFilter.Builder().hasOutboundAuthentication("Sample*").build();
		assertFalse(filter.filter(testAPI), "API must match also match based on the selected OAuth-Client App");
	}
	
	private API getAPIWithBackendBasepath(String basePath) {
		API api = new API();
		ServiceProfile serviceProfile = new ServiceProfile();
		serviceProfile.setBasePath(basePath);
		Map<String, ServiceProfile> serviceProfiles = new HashMap<>();
		serviceProfiles.put("_default", serviceProfile);
		api.setServiceProfiles(serviceProfiles);
		return api;
	}
	
	private void addPolicy(API api, String policyName, PolicyType type) throws AppException {
		OutboundProfile outboundProfile = new OutboundProfile();
		Policy policy = new Policy(policyName);
		switch(type) {
		case REQUEST:
			outboundProfile.setRequestPolicy(policy);
			break;
		case ROUTING:
			outboundProfile.setRoutePolicy(policy);
			break;
		case RESPONSE:
			outboundProfile.setResponsePolicy(policy);
			break;
		case FAULT_HANDLER:
			outboundProfile.setFaultHandlerPolicy(policy);
			break;
		default:
			break;			
		}
		Map<String, OutboundProfile> outboundProfiles = new HashMap<>();
		outboundProfiles.put("_default", outboundProfile);
		api.setOutboundProfiles(outboundProfiles);
	}
	
	private void addInboundSecurityPolicy(API api, String policyName) {
		Map<String, String> properties = new HashMap<>();
		properties.put("authenticationPolicy", "<key type='CircuitContainer'><id field='name' value='API Keys'/><key type='FilterCircuit'><id field='name' value='"+policyName+"'/></key></key>");
		SecurityDevice securityDevice = new SecurityDevice();
		securityDevice.setType(DeviceType.authPolicy);
		securityDevice.setConvertPolicies(false);
		securityDevice.setProperties(properties);
		List<SecurityDevice> devices = new ArrayList<>();
		devices.add(securityDevice);
		SecurityProfile securityProfile = new SecurityProfile();
		securityProfile.setName("_default");
		securityProfile.setDevices(devices);
		List<SecurityProfile> securityProfiles = new ArrayList<>();
		securityProfiles.add(securityProfile);
		api.setSecurityProfiles(securityProfiles);
		
		InboundProfile inboundProfile = new InboundProfile();
		inboundProfile.setSecurityProfile("_default");
		Map<String, InboundProfile> inboundProfiles = new HashMap<>();
		inboundProfiles.put("_default", inboundProfile);
		api.setInboundProfiles(inboundProfiles);
	}
	
	private void addInboundSecurityToAPI(API api, DeviceType deviceType) {
		SecurityDevice securityDevice = new SecurityDevice();
		securityDevice.setType(deviceType);
		List<SecurityDevice> devices = new ArrayList<>();
		devices.add(securityDevice);
		SecurityProfile securityProfile = new SecurityProfile();
		securityProfile.setName("_default");
		securityProfile.setDevices(devices);
		List<SecurityProfile> securityProfiles = new ArrayList<>();
		securityProfiles.add(securityProfile);
		api.setSecurityProfiles(securityProfiles);
		
		InboundProfile inboundProfile = new InboundProfile();
		inboundProfile.setSecurityProfile("_default");
		Map<String, InboundProfile> inboundProfiles = new HashMap<>();
		inboundProfiles.put("_default", inboundProfile);
		api.setInboundProfiles(inboundProfiles);
	}
	
	private void addOutboundSecurityToAPI(API api, AuthType authType) throws AppException {
		List<AuthenticationProfile> authnProfiles = new ArrayList<>();
		AuthenticationProfile authNProfile = new AuthenticationProfile();
		authNProfile.setName("_default");
		authNProfile.setType(authType);
		authnProfiles.add(authNProfile);
		
		Map<String, OutboundProfile> outboundProfiles = new HashMap<>();
		OutboundProfile outboundProfile = new OutboundProfile();
		outboundProfile.setAuthenticationProfile("_default");
		
		outboundProfiles.put("_default", outboundProfile);
		api.setAuthenticationProfiles(authnProfiles);
		api.setOutboundProfiles(outboundProfiles);
	}
}

