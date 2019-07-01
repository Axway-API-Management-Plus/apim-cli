package com.axway.apim.promote.mvn.plugin.exceptions;

import com.axway.apim.lib.ErrorCode;

/**
 * Interface for mapping severity error from axway-swagger-promote tool.
 */
public interface AppExceptionServerityIntf {

    ErrorCode getErrorCode();
}
