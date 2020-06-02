package com.axway.apim.cli;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class APIManagerCLITest {
	
  @Test
  public void testArgsIsNull() {
	  APIManagerCLI cli = new APIManagerCLI(null);
	  Assert.assertNull(cli.selectedService);
	  Assert.assertNull(cli.selectedMethod);
  }
  
  @Test
  public void testEmptyArgs() {
	  APIManagerCLI cli = new APIManagerCLI(new String[] {});
	  // These tests required to have the app- and api modules available in the classpath
	  Assert.assertTrue(cli.servicesMappedByGroup instanceof Map);
	  Assert.assertTrue(cli.servicesMappedByGroup.containsKey("api"));
	  Assert.assertTrue(cli.servicesMappedByGroup.containsKey("app"));
	  Assert.assertTrue(cli.servicesMappedByGroup.get("api").size()==2);
	  Assert.assertNull(cli.selectedService);
	  Assert.assertNull(cli.selectedMethod);
  }
  
  @Test
  public void testWithServiceIdOnly() {
	  APIManagerCLI cli = new APIManagerCLI(new String[] {"api"});
	  Assert.assertNotNull(cli.selectedServiceGroup);
	  Assert.assertTrue(cli.selectedServiceGroup instanceof List, "cli.service is type: " + cli.selectedServiceGroup.getClass().getName());
	  Assert.assertNull(cli.selectedMethod);
  }
  
  @Test
  public void testWithServiceIdAndMethod() {
	  APIManagerCLI cli = new APIManagerCLI(new String[] {"api", "import"});
	  Assert.assertNotNull(cli.selectedServiceGroup);
	  Assert.assertTrue(cli.selectedServiceGroup instanceof List, "cli.service is type: " + cli.selectedServiceGroup.getClass().getName());
	  Assert.assertNotNull(cli.selectedMethod);
	  Assert.assertTrue(cli.selectedService instanceof APIMCLIServiceProvider, "cli.selectedService is type: " + cli.selectedService.getClass().getName());
	  Assert.assertTrue(cli.selectedMethod instanceof Method);
	  Assert.assertEquals(cli.selectedMethod.getName(), "importAPI");
  }
  
  @Test
  public void simulateChocoExecution() {
	  APIManagerCLI cli = new APIManagerCLI(new String[] {"choco", "api", "import"});
	  Assert.assertNotNull(cli.selectedServiceGroup);
	  Assert.assertTrue(cli.selectedServiceGroup instanceof List, "cli.service is type: " + cli.selectedServiceGroup.getClass().getName());
	  Assert.assertNotNull(cli.selectedMethod);
	  Assert.assertTrue(cli.selectedService instanceof APIMCLIServiceProvider, "cli.selectedService is type: " + cli.selectedService.getClass().getName());
	  Assert.assertEquals(cli.selectedMethod.getName(), "importAPI");
  }
}
