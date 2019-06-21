package com.axway.apim.promote.mvn.plugin.exceptions;

import com.axway.apim.lib.ErrorCode;

/**
 * Exception for breaking changes detected error code
 * This should be thrown from api-axway-promote tool
 */
public class BreakingChangeException extends AbstractPublishingPluginException {

    public BreakingChangeException(final String message, final Exception cause, final ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public BreakingChangeException(final String message, final Throwable cause, final ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    public BreakingChangeException(final String message, final ErrorCode errorCode) {
        super(message, errorCode);
    }
}
