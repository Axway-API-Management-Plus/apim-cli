package com.axway.lib.utils;

import static org.testng.Assert.assertThrows;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.utils.URLParser;

public class URLParserTest {
	
	@Test
	public void testNoUsernamePassword() throws AppException {
		String urlToAPIDefinition = "https://petstore.swagger.io/v2/swagger.json";
		URLParser parser = new URLParser(urlToAPIDefinition);
		Assert.assertEquals(parser.getUsername(), null);
		Assert.assertEquals(parser.getPassword(), null);
	}

	@Test
	public void testStandardUsernamePassword() throws AppException {
		String urlToAPIDefinition = "user/password@https://petstore.swagger.io/v2/swagger.json";
		URLParser parser = new URLParser(urlToAPIDefinition);
		Assert.assertEquals(parser.getUsername(), "user");
		Assert.assertEquals(parser.getPassword(), "password");
	}
	@Test
	public void testUsernameWithAt() throws AppException {
		String urlToAPIDefinition = "user@axway.com/password@https://petstore.swagger.io/v2/swagger.json";
		URLParser parser = new URLParser(urlToAPIDefinition);
		Assert.assertEquals(parser.getUsername(), "user@axway.com");
		Assert.assertEquals(parser.getPassword(), "password");
	}
	
	@Test
	public void testInvalidFormat1() {
		String urlToAPIDefinition = "@https://petstore.swagger.io/v2/swagger.json";
		assertThrows(AppException.class, () -> {new URLParser(urlToAPIDefinition);});
	}
	
	@Test
	public void testInvalidFormat2() {
		String urlToAPIDefinition = "dadasdsa@https://petstore.swagger.io/v2/swagger.json";
		assertThrows(AppException.class, () -> {new URLParser(urlToAPIDefinition);});
	}
}
