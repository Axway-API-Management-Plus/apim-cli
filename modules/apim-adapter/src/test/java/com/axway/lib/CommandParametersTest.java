package com.axway.lib;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.adapter.APIManagerAdapter.CacheType;
import com.axway.apim.lib.CommandParameters;

public class CommandParametersTest {

	@Test
	public void testclearCacheAll() {
		Map<String, String> manualParams = new HashMap<String, String>();
		manualParams.put("clearCache", "ALL");
		CommandParameters params = new CommandParameters(manualParams);

		Assert.assertEquals(params.clearCaches().size(), CacheType.values().length);
	}
	
	@Test
	public void testCleanSpecificCache() {
		Map<String, String> manualParams = new HashMap<String, String>();
		manualParams.put("clearCache", CacheType.applicationsQuotaCache.name());
		CommandParameters params = new CommandParameters(manualParams);

		Assert.assertEquals(params.clearCaches().size(), 1);
		Assert.assertEquals(params.clearCaches().get(0), CacheType.applicationsQuotaCache);
	}
	
	@Test
	public void testCleanOneWildcardCache() {
		Map<String, String> manualParams = new HashMap<String, String>();
		manualParams.put("clearCache", "*App*");
		CommandParameters params = new CommandParameters(manualParams);

		Assert.assertEquals(params.clearCaches().size(), 5);
		Assert.assertTrue(params.clearCaches().contains(CacheType.applicationsQuotaCache));
		Assert.assertTrue(params.clearCaches().contains(CacheType.applicationsCache));
		Assert.assertTrue(params.clearCaches().contains(CacheType.applicationsSubscriptionCache));
		Assert.assertTrue(params.clearCaches().contains(CacheType.applicationAPIAccessCache));
		Assert.assertTrue(params.clearCaches().contains(CacheType.applicationsCredentialCache));
	}
	
	@Test
	public void testclearCacheCombined() {
		Map<String, String> manualParams = new HashMap<String, String>();
		manualParams.put("clearCache", "*Quota*, applicationAPIAccessCache");
		CommandParameters params = new CommandParameters(manualParams);

		Assert.assertEquals(params.clearCaches().size(), 2);
		Assert.assertTrue(params.clearCaches().contains(CacheType.applicationsQuotaCache));
		Assert.assertTrue(params.clearCaches().contains(CacheType.applicationAPIAccessCache));
	}
}
