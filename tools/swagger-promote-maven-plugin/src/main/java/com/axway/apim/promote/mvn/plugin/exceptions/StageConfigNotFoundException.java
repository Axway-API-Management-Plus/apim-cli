package com.axway.apim.promote.mvn.plugin.exceptions;

/**
 * Exception for stage configuration file related error
 * This exception is thrown from this maven plugin itself
 */
public class StageConfigNotFoundException extends AbstractPublishingPluginException {

    public StageConfigNotFoundException(final String message, final Exception cause) {
        super(message, cause);
    }

    public StageConfigNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public StageConfigNotFoundException(final String message) {
        super(message);
    }

}
