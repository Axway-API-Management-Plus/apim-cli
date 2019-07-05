package com.axway.apim.promote.mvn.plugin.exceptions;

import com.axway.apim.lib.ErrorCode;

/**
 * Exception for input related errors
 * This should be thrown from api-axway-promote tool and from the plugin itself
 */
public class ArgumentParseException extends AbstractPublishingPluginException {

    public ArgumentParseException(final String message, final Exception cause, final ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public ArgumentParseException(final String message, final Exception cause) {
        super(message, cause);
    }

    public ArgumentParseException(final String message, final Throwable cause, final ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public ArgumentParseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ArgumentParseException(final String message, final ErrorCode errorCode) {
        super(message, errorCode);
    }

    public ArgumentParseException(final String message) {
        super(message);
    }
}
