package com.axway.apim.adapter.apis;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.api.model.RemoteHost;
import com.axway.apim.lib.errorHandling.AppException;

public class RemoteHostFilterTest {
	
	@Test
	public void filterRemoteHostOnName() throws IOException, AppException {
		RemoteHostFilter filter1 = new RemoteHostFilter.Builder().hasName("XXX").build();
		RemoteHostFilter filter2 = new RemoteHostFilter.Builder().hasName("ABC").build();
		
		RemoteHost remoteHost1 = new RemoteHost();
		remoteHost1.setName("ABC");

		Assert.assertTrue(filter1.filter(remoteHost1), "Remote should be filtered, as the name is different.");
		Assert.assertFalse(filter2.filter(remoteHost1), "Remote should NOT be filtered, as the name is the same.");
	}
	
	@Test
	public void filterRemoteHostOnNameAndPort() throws IOException, AppException {
		RemoteHostFilter filter1 = new RemoteHostFilter.Builder().hasName("XXX").hasPort(7878).build();
		RemoteHostFilter filter2 = new RemoteHostFilter.Builder().hasName("ABC").hasPort(1234).build();
		RemoteHostFilter filter3 = new RemoteHostFilter.Builder().hasName("ABC").hasPort(7878).build();
		RemoteHostFilter filter4 = new RemoteHostFilter.Builder().hasName("ABC").hasPort(7878).hasAlias("OtherAlias").build();
		RemoteHostFilter filter5 = new RemoteHostFilter.Builder().hasName("ABC").hasPort(7878).hasAlias("SomeAlias").build();
		
		RemoteHost remoteHost1 = new RemoteHost();
		remoteHost1.setName("ABC");
		remoteHost1.setPort(7878);
		remoteHost1.setAlias("SomeAlias");

		Assert.assertTrue(filter1.filter(remoteHost1), "Remote host should be filtered, as the name is different.");
		Assert.assertTrue(filter2.filter(remoteHost1), "Remote host should be filtered, as the port is different.");
		Assert.assertFalse(filter3.filter(remoteHost1), "Remote host should NOT be filtered, as name and port are equal.");
		Assert.assertTrue(filter4.filter(remoteHost1), "Remote host should be filtered, as the alias is different.");
		Assert.assertFalse(filter5.filter(remoteHost1), "Remote host should NOT be filtered, as everything equals.");
	}
}