package com.axway.lib;

import com.axway.apim.lib.error.AppException;
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
	public void testClearCacheCombined() {
		CoreParameters params = new CoreParameters();
		params.setClearCache(CacheType.applicationsQuotaCache.name() + "," + CacheType.applicationAPIAccessCache.name());
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
		Assert.assertEquals(params.getApiBasepath(), params.getApiBasepath());
	}

	@Test
	public void testDefaultTimeoutAndRetryDelay() {
		CoreParameters params = new CoreParameters();
		Assert.assertEquals(params.getTimeout(), 30000);
		Assert.assertEquals(params.getRetryDelay(), 1000);
	}

	@Test
	public void testTimeoutAndRetryDelay() {
		CoreParameters params = new CoreParameters();
		params.setTimeout("40000");
		params.setRetryDelay("4000");

		Assert.assertEquals(params.getTimeout(), 40000);
		Assert.assertEquals(params.getRetryDelay(), 4000);
	}

	@Test(expectedExceptions = AppException.class, expectedExceptionsMessageRegExp = "Missing required parameters.")
	public void testValidateRequiredParameters() throws AppException {
		CoreParameters params = new CoreParameters();
		params.validateRequiredParameters();
	}

	@Test
	public void testZdd() {
		CoreParameters params = new CoreParameters();
		Assert.assertFalse(params.isZeroDowntimeUpdate());
		params.setZeroDowntimeUpdate(true);
		Assert.assertTrue(params.isZeroDowntimeUpdate());
	}

	@Test
	public void testToString() {
		CoreParameters params = new CoreParameters();
		params.setUsername("apiadmin");
		params.setHostname("localhost");
		params.setStage("qa");
		Assert.assertEquals(params.toString(), "[hostname=localhost, username=apiadmin, stage=qa]");
	}

	@Test
	public void testClientOrgsMode(){
		CoreParameters params = new CoreParameters();
		Assert.assertEquals(params.getClientOrgsMode(), CoreParameters.Mode.add);
		params.setClientOrgsMode(CoreParameters.Mode.replace);
		Assert.assertEquals(params.getClientOrgsMode(), CoreParameters.Mode.replace);
		Assert.assertFalse(params.isIgnoreClientOrgs());
		params.setClientOrgsMode(CoreParameters.Mode.ignore);
		Assert.assertTrue(params.isIgnoreClientOrgs());
	}

	@Test
	public void testClientAppsMode(){
		CoreParameters params = new CoreParameters();
		Assert.assertEquals(params.getClientAppsMode(), CoreParameters.Mode.add);
		params.setClientAppsMode(CoreParameters.Mode.replace);
		Assert.assertEquals(params.getClientAppsMode(), CoreParameters.Mode.replace);
		Assert.assertFalse(params.isIgnoreClientApps());
		params.setClientAppsMode(CoreParameters.Mode.ignore);
		Assert.assertTrue(params.isIgnoreClientApps());
	}

	@Test
	public void testQuotaMode(){
		CoreParameters params = new CoreParameters();
		Assert.assertEquals(params.getQuotaMode(), CoreParameters.Mode.add);
		params.setQuotaMode(CoreParameters.Mode.replace);
		Assert.assertEquals(params.getQuotaMode(), CoreParameters.Mode.replace);
	}

	@Test
	public void testIgnoreQuota(){
		CoreParameters params = new CoreParameters();
		Assert.assertFalse(params.isIgnoreQuotas());
		params.setIgnoreQuotas(true);
		Assert.assertTrue(params.isIgnoreQuotas());
	}
}
