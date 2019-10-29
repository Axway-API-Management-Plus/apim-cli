package com.axway.apim.plugins;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;

public class PluginException extends AppException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PluginException(String message, ErrorCode errorCode, Throwable throwable) {
		super(message, errorCode, throwable);
	}
	
	public PluginException(String message, ErrorCode errorCode) {
		super(message, errorCode);
	}
}
