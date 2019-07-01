package com.axway.apim.promote.mvn.plugin.exceptions;

import com.axway.apim.lib.ErrorCode;

/**
 * Exception for axway server publish errors
 * This should be thrown from api-axway-promote tool
 */
public class ApiGatewayPublishingException extends AbstractPublishingPluginException {

    public ApiGatewayPublishingException(final String message, final Exception cause, final ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public ApiGatewayPublishingException(final String message, final Throwable cause, final ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public ApiGatewayPublishingException(final String message, final ErrorCode errorCode) {
        super(message, errorCode);
    }
}
