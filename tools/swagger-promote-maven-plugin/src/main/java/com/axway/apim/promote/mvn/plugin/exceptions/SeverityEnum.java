package com.axway.apim.promote.mvn.plugin.exceptions;

import com.axway.apim.lib.ErrorCode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A mapper class for ErrorCode to severity type.
 * This class needed to be specify deeper.
 * The plugin should only be thrown an exception if the severity type is error.
 */
public enum SeverityEnum {
    WARN,
    INFO,
    ERROR;

    // The set of ErrorCodes which are considered as WARN only
    private final static Set<ErrorCode> warnErrorCodes = new HashSet<>(Arrays.asList(ErrorCode.NO_CHANGE));
    // The set of ErrorCodes which are considered as INFO only
    private final static Set<ErrorCode> infoErrorCodes = new HashSet<>(Arrays.asList());
    // The set of ErrorCodes which are considered as ERROR
    private final static Set<ErrorCode> errorErrorCode = new HashSet<>(Arrays.asList());

    /**
     * Map severity type from errorcode.
     *
     * @param e
     *
     * @return
     */
    public static SeverityEnum getSeverityFromErrorCode(ErrorCode e) {
        if (e == null || infoErrorCodes.contains(e)) {
            return INFO;
        } else if (warnErrorCodes.contains(e)) {
            return WARN;
        } else {
            return ERROR;
        }
    }

}
