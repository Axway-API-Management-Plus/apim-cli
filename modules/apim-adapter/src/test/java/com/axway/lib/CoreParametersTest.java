package com.axway.lib;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter.CacheType;
import com.axway.apim.lib.CoreParameters;

public class CoreParametersTest {

	@Test
	public void testclearCacheAll() {
		CoreParameters params = new CoreParameters();
		params.setClearCache("ALL");

		Assert.assertEquals(params.clearCaches().size(), CacheType.values().length);
	}
	
	@Test
	public void testCleanSpecificCache() {
		CoreParameters params = new CoreParameters();
		params.setClearCache(CacheType.applicationsQuotaCache.name());

		Assert.assertEquals(params.clearCaches().size(), 1);
		Assert.assertEquals(params.clearCaches().get(0), CacheType.applicationsQuotaCache);
	}
	
	@Test
	public void testCleanOneWildcardCache() {
		CoreParameters params = new CoreParameters();
		params.setClearCache("*App*");

		Assert.assertEquals(params.clearCaches().size(), 5);
		Assert.assertTrue(params.clearCaches().contains(CacheType.applicationsQuotaCache));
		Assert.assertTrue(params.clearCaches().contains(CacheType.applicationsCache));
		Assert.assertTrue(params.clearCaches().contains(CacheType.applicationsSubscriptionCache));
		Assert.assertTrue(params.clearCaches().contains(CacheType.applicationAPIAccessCache));
		Assert.assertTrue(params.clearCaches().contains(CacheType.applicationsCredentialCache));
	}
	
	@Test
	public void testclearCacheCombined() {
		CoreParameters params = new CoreParameters();
		params.setClearCache("*Quota*, applicationAPIAccessCache");

		Assert.assertEquals(params.clearCaches().size(), 2);
		Assert.assertTrue(params.clearCaches().contains(CacheType.applicationsQuotaCache));
		Assert.assertTrue(params.clearCaches().contains(CacheType.applicationAPIAccessCache));
	}
	
	@Test
	public void testDefaultQuotaModeStays() {
		CoreParameters params = new CoreParameters();
		params.setQuotaMode(null);
		
		Assert.assertEquals(params.getQuotaMode(), CoreParameters.Mode.add);
	}
	
	@Test
	public void testAPIBasepath() {
		CoreParameters params = new CoreParameters();
		
		params.setHostname("manager.host");
		Assert.assertEquals(params.getApiBasepath(), CoreParameters.DEFAULT_API_BASEPATH);
		
		params.setPort(443);
		Assert.assertEquals(params.getAPIManagerURL(), "https://manager.host:443"+CoreParameters.DEFAULT_API_BASEPATH);
		
		params.setApiBasepath("/fr/apim/v13/portal");
		Assert.assertEquals(params.getAPIManagerURL(), "https://manager.host:443/fr/apim/v13/portal");
	}
}
