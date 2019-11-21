package com.axway.apim.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

public class APIManagerAdapterTest {
	
	private static final int WIREMOCK_PORT = 9999;
	
	
	public static WireMockClassRule WIREMOCK;
	
	@BeforeClass
	public void before() {
		WIREMOCK = new WireMockClassRule(WIREMOCK_PORT);
	}
	
  @Test
  public void f() {
  }
}
