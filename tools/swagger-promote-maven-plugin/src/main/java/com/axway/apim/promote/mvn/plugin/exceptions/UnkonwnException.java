package com.axway.apim.promote.mvn.plugin.exceptions;

import com.axway.apim.lib.ErrorCode;

/**
 * Exception for all unknown exception
 * This can be thrown from the api-axway-promote tool or the plugin itself
 */
public class UnkonwnException extends AbstractPublishingPluginException {

    public UnkonwnException(final String message, final Exception cause, final ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public UnkonwnException(final String message, final Exception cause) {
        super(message, cause);
    }

    public UnkonwnException(final String message, final Throwable cause, final ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public UnkonwnException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UnkonwnException(final String message, final ErrorCode errorCode) {
        super(message, errorCode);
    }

    public UnkonwnException(final String message) {
        super(message);
    }
}
