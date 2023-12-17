package com.axway.apim.apiimport;

import com.axway.apim.lib.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class APIImportConfigTest {

  @Test
  public void testHttpUriCheck() {
	  String httpUri1 = "https://petstore.swagger.io/v2/swagger.json";
	  String httpUri2 = "user/password@https://petstore.swagger.io/v2/swagger.json";
	  String httpUri3 = "user/password";
	  String httpUri4 = "user/password@";

      Assert.assertTrue(Utils.isHttpUri(httpUri1));
      Assert.assertTrue(Utils.isHttpUri(httpUri2));
      Assert.assertFalse(Utils.isHttpUri(httpUri3));
      Assert.assertFalse(Utils.isHttpUri(httpUri4));
  }
}
