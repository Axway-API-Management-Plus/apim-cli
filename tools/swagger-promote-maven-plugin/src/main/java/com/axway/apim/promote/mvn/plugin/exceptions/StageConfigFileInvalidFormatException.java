package com.axway.apim.promote.mvn.plugin.exceptions;

/**
 * Exception for API config file related errors
 * This should be thrown from the plugin itself
 */
public class StageConfigFileInvalidFormatException extends AbstractPublishingPluginException {

    public StageConfigFileInvalidFormatException(final String message, final Exception cause) {
        super(message, cause);
    }

    public StageConfigFileInvalidFormatException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public StageConfigFileInvalidFormatException(final String message) {
        super(message);
    }

}
