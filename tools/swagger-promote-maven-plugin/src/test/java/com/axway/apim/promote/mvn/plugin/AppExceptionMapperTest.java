package com.axway.apim.promote.mvn.plugin;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.stream.Stream;

import org.junit.Test;

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

public class AppExceptionMapperTest {
    @Test
    public void map() throws Exception {
        checkTypeOfException(ErrorCode.NO_CHANGE, NoChangesInApiForPublishException.class);
        checkTypeOfException(ErrorCode.BREAKING_CHANGE_DETECTED, BreakingChangeException.class);
        checkTypeOfException(ErrorCode.MISSING_PARAMETER, ArgumentParseException.class);
        checkTypeOfException(ErrorCode.CANT_READ_CONFIG_FILE, ApiConfigurationFileException.class);
        checkTypeOfException(ErrorCode.CANT_READ_API_DEFINITION_FILE, ApiSpecificationFileException.class);
        checkTypeOfException(ErrorCode.CANT_PARSE_HTTP_RESPONSE, ApiGatewayCommunicationException.class);
        checkTypeOfException(ErrorCode.CANT_READ_JSON_PAYLOAD, ApiGatewayCommunicationException.class);
        checkTypeOfException(ErrorCode.CANT_SEND_HTTP_REQUEST, ApiGatewayCommunicationException.class);
        checkTypeOfException(ErrorCode.API_MANAGER_COMMUNICATION, ApiGatewayCommunicationException.class);
        checkTypeOfException(ErrorCode.CANT_CREATE_API_PROXY, ApiGatewayPublishingException.class);
        checkTypeOfException(ErrorCode.CANT_CREATE_STATE_CHANGE, ApiGatewayPublishingException.class);
        checkTypeOfException(ErrorCode.CANT_SETUP_VHOST, ApiGatewayPublishingException.class);
        checkTypeOfException(ErrorCode.CANT_UPDATE_API_PROXY, ApiGatewayPublishingException.class);
        checkTypeOfException(ErrorCode.CANT_CREATE_BE_API, ApiGatewayPublishingException.class);
        checkTypeOfException(ErrorCode.CANT_UPDATE_API_STATUS, ApiGatewayPublishingException.class);
        checkTypeOfException(ErrorCode.CANT_UPDATE_QUOTA_CONFIG, ApiGatewayPublishingException.class);
        checkTypeOfException(ErrorCode.CANT_UPGRADE_API_ACCESS, ApiGatewayPublishingException.class);
    }

    @Test
    public void testMapShouldThrowUnknownException() {
        final AbstractPublishingPluginException exception = AppExceptionMapper.map(null);
        assertThat(exception, instanceOf(UnkonwnException.class));
        assertThat(exception.getErrorCode(), nullValue());
    }
    @Test
    public void testMapShouldCoverAllErrorCodes() {
        Stream.of(ErrorCode.values()).forEach( e -> {
            final AbstractPublishingPluginException exception = AppExceptionMapper.map(new AppException("", e));
            assertThat(exception, notNullValue());
        });
    }

    public void checkTypeOfException(ErrorCode errorCodeToMap, Class mappedExceptionClass) {
        final AbstractPublishingPluginException actual = AppExceptionMapper.map(new AppException("", errorCodeToMap));
        assertThat(actual, instanceOf(mappedExceptionClass));
        assertThat(actual.getErrorCode(), notNullValue());

    }

}