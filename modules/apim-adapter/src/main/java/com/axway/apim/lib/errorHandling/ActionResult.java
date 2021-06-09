package com.axway.apim.lib.errorHandling;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

public class ActionResult {
	private boolean hasError = false;
	
	private List<String[]> errorMessages = new ArrayList<String[]>();
	private List<ErrorCode> errorCodes = new ArrayList<ErrorCode>();
	
	public ErrorCode getErrorCode() {
		if(errorCodes.size()>0) {
			return errorCodes.get(0);
		} else {
			return ErrorCode.SUCCESS;
		}
	}
	
	public void setError(String errorMessage, ErrorCode errorCode) {
		this.errorMessages.add(new String[] {errorMessage, "ERROR"});
		this.errorCodes.add(errorCode);
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
	
	public boolean hasError() {
		return hasError;
	}
}
