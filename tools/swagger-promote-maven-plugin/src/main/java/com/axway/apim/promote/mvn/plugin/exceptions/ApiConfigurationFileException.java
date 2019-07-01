package com.axway.apim.promote.mvn.plugin.exceptions;

import com.axway.apim.lib.ErrorCode;

/**
 * Exception for ErrorCode.CANT_READ_CONFIG_FILE
 * This is be thrown from the axway-swagger-promote tool
 */
public class ApiConfigurationFileException extends AbstractPublishingPluginException {

    public ApiConfigurationFileException(final String message, final Exception cause, final ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public ApiConfigurationFileException(final String message, final Throwable cause, final ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public ApiConfigurationFileException(final String message, final ErrorCode errorCode) {
        super(message, errorCode);
    }
}
