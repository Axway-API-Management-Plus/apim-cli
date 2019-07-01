package com.axway.apim.promote.mvn.plugin.exceptions;

import org.apache.maven.plugin.MojoExecutionException;

import com.axway.apim.lib.ErrorCode;

/**
 * The abstract exception class for this plugin
 *
 */
public abstract class AbstractPublishingPluginException extends MojoExecutionException implements AppExceptionServerityIntf {
    /**
     * Error code from the axway-swagger-promote tool
     */
    private final ErrorCode errorCode;

    public AbstractPublishingPluginException(final String message, final Exception cause) {
        super(message, cause);
        errorCode = null;
    }

    public AbstractPublishingPluginException(final String message, final Exception cause, final  ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public AbstractPublishingPluginException(final String message, final Throwable cause) {
        super(message, cause);
        errorCode = null;
    }

    public AbstractPublishingPluginException(final String message, final Throwable cause, final  ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public AbstractPublishingPluginException(final String message) {
        super(message);
        errorCode = null;
    }

    public AbstractPublishingPluginException(final String message, final  ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
