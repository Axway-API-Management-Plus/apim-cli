package com.axway.apim.lib.errorHandling;

import org.slf4j.Logger;

public class AppException extends Exception {
	
	private static final long serialVersionUID = 7718828512143293558L;
	
	private final ErrorCode error;
	
	public enum LogLevel {
		INFO,
		WARN, 
		ERROR, 
		DEBUG
	}

	public AppException(String message, ErrorCode errorCode, Throwable throwable) {
		super(message, throwable);
		this.error = errorCode;
	}

	public AppException(String message, ErrorCode errorCode) {
		super(message);
		this.error = errorCode;
	}

	public ErrorCode getError() {
		return error;
	}
	
	public void logException(Logger LOG) {
		Throwable cause = null;
		if(error.getPrintStackTrace()) {
			cause = this;
		}
		switch (error.getLogLevel()) {
		case INFO: 
			LOG.info(getAllMessages(), cause);
		case WARN: 
			LOG.warn(getAllMessages(), cause);
		case DEBUG: 
			LOG.debug(getAllMessages(), cause);
		default:
			LOG.error(getAllMessages(), cause);
		}
	}

	public String getAllMessages() {
		String message = getMessage();
		if(this.getCause()!=null && this.getCause() instanceof AppException) {
			message += "\n                                 | " + ((AppException)this.getCause()).getAllMessages();
		}
		return message;
	}
}
