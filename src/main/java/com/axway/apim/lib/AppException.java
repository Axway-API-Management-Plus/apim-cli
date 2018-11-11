package com.axway.apim.lib;

public class AppException extends Exception {
	
	private static final long serialVersionUID = 7718828512143293558L;
	
	private final ErrorCode errorCode;
	
	private boolean logStackStrace = true;

	public AppException(String message, ErrorCode errorCode, Throwable throwable) {
		super(message, throwable);
		this.errorCode = errorCode;
	}

	public AppException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public AppException(String message, ErrorCode errorCode, boolean logStackTrace) {
		super(message);
		this.errorCode = errorCode;
		this.logStackStrace = logStackTrace;
	}
	
	public AppException(String message, ErrorCode errorCode, Throwable throwable, boolean logStackTrace) {
		super(message, throwable);
		this.errorCode = errorCode;
		this.logStackStrace = logStackTrace;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public boolean isLogStackStrace() {
		return logStackStrace;
	}
}
