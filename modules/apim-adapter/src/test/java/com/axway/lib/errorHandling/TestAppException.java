package com.axway.lib.errorHandling;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;

public class TestAppException {
	@Test
	public void testGivenErrorIsReturned() {
		AppException someError = new AppException("Some root cause error", ErrorCode.API_OPERATION_NOT_FOUND);
		Assert.assertEquals(someError.getError(), ErrorCode.API_OPERATION_NOT_FOUND);
	}
	
	@Test
	public void testRootCauseErrorIsReturned() {
		AppException rootCause = new AppException("Some root cause error", ErrorCode.ACCESS_ORGANIZATION_ERR);
		AppException intermediateCause = new AppException("Some root cause error", ErrorCode.CANT_CREATE_HTTP_CLIENT, rootCause);
		AppException e2 = new AppException("Some root cause error", ErrorCode.CANT_CREATE_BE_API, intermediateCause);
		
		Assert.assertEquals(e2.getError(), ErrorCode.ACCESS_ORGANIZATION_ERR);
	}
	
	@Test
	public void testRootCauseErrorIsReturnedWithIntermediate() {
		AppException rootCause = new AppException("Some root cause error", ErrorCode.ACCESS_ORGANIZATION_ERR);
		IOException intermediateCause = new IOException("Some IO Error", rootCause);
		AppException e2 = new AppException("Some root cause error", ErrorCode.CANT_CREATE_BE_API, intermediateCause);
		
		Assert.assertEquals(e2.getError(), ErrorCode.ACCESS_ORGANIZATION_ERR);
	}
}
