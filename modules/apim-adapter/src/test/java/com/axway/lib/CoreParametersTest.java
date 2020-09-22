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
}
