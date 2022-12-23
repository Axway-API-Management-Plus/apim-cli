package com.axway.lib;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.adapter.CacheType;
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
		params.setClearCache(CacheType.APPLICATIONS_QUOTA_CACHE.name());

		Assert.assertEquals(params.clearCaches().size(), 1);
		Assert.assertEquals(params.clearCaches().get(0), CacheType.APPLICATIONS_QUOTA_CACHE);
	}
	
	@Test
	public void testCleanOneWildcardCache() {
		CoreParameters params = new CoreParameters();
		params.setClearCache("*App*");

		Assert.assertEquals(params.clearCaches().size(), 5);
		Assert.assertTrue(params.clearCaches().contains(CacheType.APPLICATIONS_QUOTA_CACHE));
		Assert.assertTrue(params.clearCaches().contains(CacheType.APPLICATIONS_CACHE));
		Assert.assertTrue(params.clearCaches().contains(CacheType.APPLICATIONS_SUBSCRIPTION_CACHE));
		Assert.assertTrue(params.clearCaches().contains(CacheType.APPLICATION_API_ACCESS_CACHE));
		Assert.assertTrue(params.clearCaches().contains(CacheType.APPLICATIONS_CREDENTIAL_CACHE));
	}
	
	@Test
	public void testClearCacheCombined() {
		CoreParameters params = new CoreParameters();
		params.setClearCache(CacheType.APPLICATIONS_QUOTA_CACHE.name() + "," + CacheType.APPLICATION_API_ACCESS_CACHE.name());
		Assert.assertEquals(params.clearCaches().size(), 2);
		Assert.assertTrue(params.clearCaches().contains(CacheType.APPLICATIONS_QUOTA_CACHE));
		Assert.assertTrue(params.clearCaches().contains(CacheType.APPLICATION_API_ACCESS_CACHE));
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
		
		params.setApiBasepath("/fr/apim/v13/portal");
		Assert.assertEquals(params.getApiBasepath(), "/fr/apim/v13/portal");
	}
}
