package com.axway.apim.test.changestate;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.AppException;
import com.axway.apim.swagger.api.state.DesiredAPI;

public class RetirementDateParserTest {
	
	// Test the supported formats: "dd.MM.yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "dd-MM-yyyy"
	
	SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
	
	@Test
	public void testValidRetirementDates() throws AppException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setRetirementDate("2050-12-31");
		Date retirementDate = new Date(testAPI.getRetirementDate());
		
		Assert.assertEquals(df.format(retirementDate), "31.12.2050");
		
		testAPI.setRetirementDate("2051-01-01");
		retirementDate = new Date(testAPI.getRetirementDate());
		Assert.assertEquals(df.format(retirementDate), "01.01.2051");

		testAPI.setRetirementDate("31-12-2050");
		retirementDate = new Date(testAPI.getRetirementDate());
		Assert.assertEquals(df.format(retirementDate), "31.12.2050");
		
		testAPI.setRetirementDate("01-01-2049");
		retirementDate = new Date(testAPI.getRetirementDate());
		Assert.assertEquals(df.format(retirementDate), "01.01.2049");
		
		testAPI.setRetirementDate("01.09.2050");
		retirementDate = new Date(testAPI.getRetirementDate());
		Assert.assertEquals(df.format(retirementDate), "01.09.2050");
		
		testAPI.setRetirementDate("01/12/2050");
		retirementDate = new Date(testAPI.getRetirementDate());
		Assert.assertEquals(df.format(retirementDate), "01.12.2050");
	}
	
	@Test(expectedExceptions = {AppException.class})
	public void testInvalidRetirementDate1() throws AppException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setRetirementDate("1976-12-31");		
	}
	
	@Test(expectedExceptions = {AppException.class})
	public void testInvalidRetirementDate2() throws AppException {
		DesiredAPI testAPI = new DesiredAPI();
		testAPI.setRetirementDate("50-12-31");		
	}
}
