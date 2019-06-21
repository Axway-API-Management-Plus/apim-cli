package com.axway.apim.promote.mvn.plugin.exceptions;

import com.axway.apim.lib.ErrorCode;

/**
 * Exception for api specification related errors.
 * This should be thrown from api-axway-promote tool
 */
public class ApiSpecificationFileException extends AbstractPublishingPluginException {

    public ApiSpecificationFileException(final String message, final Exception cause, final ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public ApiSpecificationFileException(final String message, final Throwable cause, final ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public ApiSpecificationFileException(final String message, final ErrorCode errorCode) {
        super(message, errorCode);
    }
}
