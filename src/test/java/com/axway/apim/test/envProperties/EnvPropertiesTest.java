package com.axway.apim.test.envProperties;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.EnvironmentProperties;

public class EnvPropertiesTest {

  @Test
  public void testNoStage() throws AppException, IOException {
	  EnvironmentProperties properties = new EnvironmentProperties("NOT_SET");
	  
	  Assert.assertEquals(properties.containsKey("doesnExists"), false);
	  Assert.assertEquals(properties.containsKey("admin_username"), true);
	  
	  Assert.assertEquals(properties.get("admin_username"), "apiadmin");
	  Assert.assertEquals(properties.get("admin_password"), "changeme");
  }
  
  @Test
  public void testAnyOtherStage() throws AppException, IOException {
	  EnvironmentProperties properties = new EnvironmentProperties("anyOtherStage");
	  
	  Assert.assertEquals(properties.containsKey("thisKeyExists"), true);
	  
	  Assert.assertEquals(properties.get("admin_username"), "anyOtherUser");
	  Assert.assertEquals(properties.get("admin_password"), "anyOtherPassword");
  }
}
