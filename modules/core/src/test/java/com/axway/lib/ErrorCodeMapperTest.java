package com.axway.lib;

import org.apache.commons.cli.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.axway.apim.lib.errorHandling.AppException;
import com.axway.apim.lib.errorHandling.ErrorCode;
import com.axway.apim.lib.errorHandling.ErrorCodeMapper;

public class ErrorCodeMapperTest {
	
	@Test
	public void testMapper1() throws AppException, ParseException {
		ErrorCodeMapper mapper = new ErrorCodeMapper();
		mapper.setMapConfiguration("12:0, 10:0");
		
		ErrorCode code = mapper.getMapedErrorCode(ErrorCode.EXPORT_FOLDER_EXISTS);
		Assert.assertEquals(code, ErrorCode.SUCCESS);
	}
	
	@Test
	public void testMapper2() throws AppException, ParseException {
		ErrorCodeMapper mapper = new ErrorCodeMapper();
		mapper.setMapConfiguration(" 12 : 0,  10 : 0 ");
		
		ErrorCode code = mapper.getMapedErrorCode(ErrorCode.NO_CHANGE);
		Assert.assertEquals(code, ErrorCode.SUCCESS);
	}
	
	@Test
	public void testMapper3() throws AppException, ParseException {
		ErrorCodeMapper mapper = new ErrorCodeMapper();
		mapper.setMapConfiguration("12:0, 10:0");

		ErrorCode code = mapper.getMapedErrorCode(ErrorCode.API_MANAGER_COMMUNICATION);
		Assert.assertEquals(code, ErrorCode.API_MANAGER_COMMUNICATION);
	}
	
	@Test
	public void testMapper4() throws AppException, ParseException {
		ErrorCodeMapper mapper = new ErrorCodeMapper();
		mapper.setMapConfiguration("177:0");

		ErrorCode code = mapper.getMapedErrorCode(ErrorCode.API_MANAGER_COMMUNICATION);
		Assert.assertEquals(code, ErrorCode.API_MANAGER_COMMUNICATION);
	}
	
	@Test
	public void testMapper5() throws AppException, ParseException {
		ErrorCodeMapper mapper = new ErrorCodeMapper();
		mapper.setMapConfiguration("177:198");

		ErrorCode code = mapper.getMapedErrorCode(ErrorCode.API_MANAGER_COMMUNICATION);
		Assert.assertEquals(code, ErrorCode.API_MANAGER_COMMUNICATION);
	}
}
