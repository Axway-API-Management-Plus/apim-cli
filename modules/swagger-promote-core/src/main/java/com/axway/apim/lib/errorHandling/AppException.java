package com.axway.apim.lib.errorHandling;

import com.axway.apim.lib.errorHandling.ErrorCode;

public class AppException extends Exception {
	
	private static final long serialVersionUID = 7718828512143293558L;
	
	private final ErrorCode errorCode;

	public AppException(String message, ErrorCode errorCode, Throwable throwable) {
		super(message, throwable);
		this.errorCode = errorCode;
	}

	public AppException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
