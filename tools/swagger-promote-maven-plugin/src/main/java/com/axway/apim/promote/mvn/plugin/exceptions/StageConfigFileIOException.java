package com.axway.apim.promote.mvn.plugin.exceptions;

/**
 * Error for stage config file related errors.
 * This exception is thrown from this plugin itself
 */
public class StageConfigFileIOException extends AbstractPublishingPluginException {

    public StageConfigFileIOException(final String message, final Exception cause) {
        super(message, cause);
    }

    public StageConfigFileIOException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public StageConfigFileIOException(final String message) {
        super(message);
    }

}
