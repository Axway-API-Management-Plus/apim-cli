package com.axway.apim.cli;

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.APIMCLIServiceProvider;

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
	  Assert.assertTrue(cli.servicesMappedByGroup instanceof Map);
	  Assert.assertTrue(cli.servicesMappedByGroup.containsKey("api"));
	  Assert.assertTrue(cli.servicesMappedByGroup.get("api").size()==2);
	  Assert.assertNull(cli.selectedService);
	  Assert.assertNull(cli.selectedMethod);
  }
  
  @Test
  public void testWithServiceIdOnly() {
	  APIManagerCLI cli = new APIManagerCLI(new String[] {"api"});
	  Assert.assertTrue(cli.selectedServiceGroup instanceof List, "cli.service is type: " + cli.selectedServiceGroup.getClass().getName());
	  Assert.assertNull(cli.selectedMethod);
  }
  
  @Test
  public void testWithServiceIdAndMethod() {
	  APIManagerCLI cli = new APIManagerCLI(new String[] {"api", "import"});
	  Assert.assertTrue(cli.selectedServiceGroup instanceof List, "cli.service is type: " + cli.selectedServiceGroup.getClass().getName());
	  Assert.assertNotNull(cli.selectedMethod);
	  Assert.assertTrue(cli.selectedService instanceof APIMCLIServiceProvider, "cli.selectedService is type: " + cli.selectedService.getClass().getName());
	  Assert.assertEquals(cli.selectedMethod, "import");
  }
}
