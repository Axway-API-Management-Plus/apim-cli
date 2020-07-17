package com.axway.apim.lib.errorHandling;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

public class ErrorState {
	private static ErrorState instance = null;
	
	private List<ErrorCode> errorCodes = new ArrayList<ErrorCode>();
	
	private List<String[]> errorMessages = new ArrayList<String[]>();
	
	private List<Boolean> logStackTraces = new ArrayList<Boolean>();
	
	boolean hasError = false;
	
	public static synchronized ErrorState getInstance () {
		if (ErrorState.instance == null) {
			ErrorState.instance = new ErrorState ();
		}
		return ErrorState.instance;
	}
	
	public static synchronized void deleteInstance () {
		ErrorState.instance = null;
	}

	private ErrorState() {
		super();
	}

	public ErrorCode getErrorCode() {
		if(errorCodes.size()>0) {
			return errorCodes.get(0);
		} else {
			return ErrorCode.SUCCESS;
		}
	}
	
	public void setError(String errorMessage, ErrorCode errorCode) {
		setError(errorMessage, errorCode, true);
	}

	public void setError(String errorMessage, ErrorCode errorCode, boolean logStackTrace) {
		this.errorMessages.add(new String[] {errorMessage, "ERROR"});
		this.errorCodes.add(errorCode);
		this.logStackTraces.add(new Boolean(logStackTrace));
		this.hasError = true;
	}
	
	public void setWarning(String warnMessage, ErrorCode errorCode, boolean logStackTrace) {
		this.errorMessages.add(new String[] {warnMessage, "WARN"});
		this.errorCodes.add(errorCode);
		this.logStackTraces.add(new Boolean(logStackTrace));
		this.hasError = true;
	}

	public void logErrorMessages(Logger LOG) {
		String intend = "";
		for(String[] message : errorMessages) {
			if(message[1].equals("WARN")) {
				LOG.warn(intend + message[0]);
			} else {
				LOG.error(intend + message[0]);
			}
			intend = intend + "";
		}
	}

	public boolean isLogStackTrace() {
		if(logStackTraces.size()>0) {
			return logStackTraces.get(0);
		} else {
			return true;
		}
	}

	public boolean hasError() {
		return hasError;
	}
}
