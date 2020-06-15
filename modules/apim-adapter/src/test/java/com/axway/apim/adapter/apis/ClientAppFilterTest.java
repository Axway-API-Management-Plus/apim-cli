package com.axway.apim.adapter.apis;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.adapter.clientApps.ClientAppFilter;
import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.TestIndicator;

public class ClientAppFilterTest {
	
	@BeforeClass
	public void setupTestIndicator() {
		TestIndicator.getInstance().setTestRunning(true);
	}
	
	@Test
	public void hasFullWildCardName() throws AppException {
		ClientAppFilter filter = new ClientAppFilter.Builder()
				.hasName("*")
				.build();
		Assert.assertEquals(filter.getFilters().size(), 0);
	}	
}
