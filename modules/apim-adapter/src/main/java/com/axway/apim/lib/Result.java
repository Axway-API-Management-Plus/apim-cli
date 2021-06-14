package com.axway.apim.lib;

import java.util.ArrayList;
import java.util.List;

import com.axway.apim.lib.errorHandling.ErrorCode;

public class Result {
	
	private boolean hasError = false;
	
	private List<ErrorCode> errorCodes = new ArrayList<ErrorCode>();
	
	public ErrorCode getErrorCode() {
		if(errorCodes.size()>0) {
			return errorCodes.get(0);
		} else {
			return ErrorCode.SUCCESS;
		}
	}
	
	public void setError(ErrorCode errorCode) {
		this.errorCodes.add(errorCode);
		this.hasError = true;
	}

	public int getRc() {
		return getErrorCode().getCode();
	}
	
	public boolean hasError() {
		return hasError;
	}
}
