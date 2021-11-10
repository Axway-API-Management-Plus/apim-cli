package com.axway.apim.lib.errorHandling;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

// Must extend JsonProcessingException to avoid wrapping in Jackson-Databind
public class AppException extends JsonProcessingException {
	
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
		if(this.getCause()!=null && this.getCause() instanceof AppException) {
			return ((AppException)this.getCause()).getError();
		} else {
			if(this.getCause() !=null && this.getCause().getCause()!=null && this.getCause().getCause() instanceof AppException) {
				return ((AppException)this.getCause().getCause()).getError();
			}
		}
		return error;
	}
	
	public void logException(Logger LOG) {
		Throwable cause = null;
		if(error.getPrintStackTrace() || LOG.isDebugEnabled()) {
			cause = this;
		}
		switch (error.getLogLevel()) {
		case INFO: 
			LOG.info(getAllMessages(), cause);
			break;
		case WARN: 
			LOG.warn(getAllMessages(), cause);
			break;
		case DEBUG: 
			LOG.debug(getAllMessages(), cause);
			break;
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
