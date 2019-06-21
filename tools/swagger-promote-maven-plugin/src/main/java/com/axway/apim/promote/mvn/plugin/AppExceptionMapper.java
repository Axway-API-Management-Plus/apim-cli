package com.axway.apim.promote.mvn.plugin;

import com.axway.apim.lib.AppException;
import com.axway.apim.lib.ErrorCode;
import com.axway.apim.promote.mvn.plugin.exceptions.AbstractPublishingPluginException;
import com.axway.apim.promote.mvn.plugin.exceptions.ApiConfigurationFileException;
import com.axway.apim.promote.mvn.plugin.exceptions.ApiGatewayCommunicationException;
import com.axway.apim.promote.mvn.plugin.exceptions.ApiGatewayPublishingException;
import com.axway.apim.promote.mvn.plugin.exceptions.ApiSpecificationFileException;
import com.axway.apim.promote.mvn.plugin.exceptions.ArgumentParseException;
import com.axway.apim.promote.mvn.plugin.exceptions.BreakingChangeException;
import com.axway.apim.promote.mvn.plugin.exceptions.NoChangesInApiForPublishException;
import com.axway.apim.promote.mvn.plugin.exceptions.UnkonwnException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Factory class for mapping axway-swagger-promote tool exception
 */
public final class AppExceptionMapper {

    /**
     * Set of errors which are considered as communication errors with api gateway
     */
    private static final Set<ErrorCode> apiGatewayCommunicationErrors = new HashSet<>(
            Arrays.asList(ErrorCode.API_MANAGER_COMMUNICATION, ErrorCode.CANT_PARSE_HTTP_RESPONSE,
                    ErrorCode.CANT_READ_JSON_PAYLOAD, ErrorCode.CANT_SEND_HTTP_REQUEST));

    /**
     * Set of erros which are considered as axway internal errors
     */
    private static final Set<ErrorCode> apiGatewayInternalErrors = new HashSet<>(
            Arrays.asList(ErrorCode.CANT_CREATE_API_PROXY, ErrorCode.CANT_CREATE_STATE_CHANGE,
                    ErrorCode.CANT_SETUP_VHOST, ErrorCode.CANT_UPDATE_API_PROXY, ErrorCode.CANT_CREATE_BE_API,
                    ErrorCode.CANT_UPDATE_API_STATUS, ErrorCode.CANT_UPDATE_API_STATUS,
                    ErrorCode.CANT_UPDATE_QUOTA_CONFIG, ErrorCode.CANT_UPGRADE_API_ACCESS));

    /**
     * private constructor to hide implicit public one
     */
    private AppExceptionMapper() {
        throw new IllegalStateException("Utility final class");
    }

    /**
     * Map the most useful error code to real exception
     * Note: this is not a complete list yet
     *
     * @param appException
     *
     * @return
     */
    public static AbstractPublishingPluginException map(final AppException appException) {
        if (appException == null || appException.getErrorCode() == null)
            return new UnkonwnException("Unknown Exception");
        final String message = appException.getMessage();
        final ErrorCode errorCode = appException.getErrorCode();
        AbstractPublishingPluginException mappedException;

        if (apiGatewayCommunicationErrors.contains(errorCode)) {
            mappedException = new ApiGatewayCommunicationException(message, appException, errorCode);
        } else if (apiGatewayInternalErrors.contains(errorCode)) {
            mappedException = new ApiGatewayPublishingException(message, appException, errorCode);
        } else {

            switch (errorCode) {
            case NO_CHANGE:
                mappedException = new NoChangesInApiForPublishException(message, appException, errorCode);
                break;
            case BREAKING_CHANGE_DETECTED:
                mappedException = new BreakingChangeException(message, appException, errorCode);
                break;
            case MISSING_PARAMETER:
                mappedException = new ArgumentParseException(message, appException, errorCode);
                break;
            case CANT_READ_CONFIG_FILE:
                return new ApiConfigurationFileException(message, appException, errorCode);
            case CANT_READ_API_DEFINITION_FILE:
                mappedException = new ApiSpecificationFileException(message, appException, errorCode);
                break;
            default:
                mappedException = new UnkonwnException("Unkonwn Error", appException);
            }
        }

        return mappedException;

    }
}
