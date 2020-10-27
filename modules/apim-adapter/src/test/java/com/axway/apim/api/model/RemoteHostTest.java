package com.axway.apim.api.model;

import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.axway.apim.adapter.apis.APIManagerMockBase;
import com.axway.apim.lib.errorHandling.AppException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

public class RemoteHostTest extends APIManagerMockBase {
	
	private static final String testPackage = "com/axway/apim/api/model/";
	
	@BeforeClass
	private void initTestIndicator() throws AppException, IOException {
		setupMockData();
	}
	
	@Test
	public void createRemoteHost() throws JsonParseException, JsonMappingException, IOException {
		List<RemoteHost> remoteHosts = mapper.readValue(this.getClass().getClassLoader().getResourceAsStream(testPackage + "RemoteHostWithWatchDog.json"), new TypeReference<List<RemoteHost>>(){});
		Assert.assertEquals(remoteHosts.size(), 1, "Expected one remote host");
	}
}