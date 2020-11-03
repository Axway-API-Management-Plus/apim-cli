package com.axway.apim.test.basic;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.apiimport.APIImportConfigAdapter;
import com.axway.apim.lib.errorHandling.AppException;

public class APIImportConfigTest {

  @Test
  public void testHttpUriCheck() throws AppException, IOException {
	  String httpUri1 = "https://petstore.swagger.io/v2/swagger.json";
	  String httpUri2 = "user/password@https://petstore.swagger.io/v2/swagger.json";
	  String httpUri3 = "user/password";
	  String httpUri4 = "user/password@";
	  
	  Assert.assertEquals(APIImportConfigAdapter.isHttpUri(httpUri1), true);
	  Assert.assertEquals(APIImportConfigAdapter.isHttpUri(httpUri2), true); 
	  Assert.assertEquals(APIImportConfigAdapter.isHttpUri(httpUri3), false); 
	  Assert.assertEquals(APIImportConfigAdapter.isHttpUri(httpUri4), false);
  }
}
