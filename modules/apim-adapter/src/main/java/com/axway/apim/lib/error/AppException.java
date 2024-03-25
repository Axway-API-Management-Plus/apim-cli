package com.axway.apim.lib.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;

// Must extend JsonProcessingException to avoid wrapping in Jackson-Databind
public class AppException extends JsonProcessingException {

    private static final long serialVersionUID = 7718828512143293558L;

    private final ErrorCode error;

    private String secondMessage;

    public enum LogLevel {
        INFO,
        WARN,
        ERROR,
        DEBUG
    }

    public AppException(String message, String secondMessage, ErrorCode errorCode, Throwable throwable) {
        super(message, throwable);
        this.error = errorCode;
        this.secondMessage = secondMessage;
    }

    public AppException(String message, ErrorCode errorCode, Throwable throwable) {
        super(message, throwable);
        this.error = errorCode;
    }

    public AppException(String message, ErrorCode errorCode) {
        super(message);
        this.error = errorCode;
    }

    public ErrorCode getError() {
        if (this.getCause() instanceof AppException) {
            return ((AppException) this.getCause()).getError();
        } else {
            if (this.getCause() != null && this.getCause().getCause() != null && this.getCause().getCause() instanceof AppException) {
                return ((AppException) this.getCause().getCause()).getError();
            }
        }
        return error;
    }

    public void logException(Logger logger) {
        if (error == ErrorCode.SUCCESS || error == ErrorCode.CLI_PARSING)
            return;
        Throwable cause = null;
        if (error.getPrintStackTrace().booleanValue() || logger.isDebugEnabled()) {
            cause = this;
        } else {
            logger.info("You may enable debug to get more details. See: https://github.com/Axway-API-Management-Plus/apim-cli/wiki/9.1.-Enable-Debug");
        }
        switch (error.getLogLevel()) {
            case INFO:
                logger.info(getAllMessages(), cause);
                break;
            case WARN:
                logger.warn(getAllMessages(), cause);
                break;
            case DEBUG:
                logger.debug(getAllMessages(), cause);
                break;
            default:
                logger.error(getAllMessages(), cause);
        }
    }

    public String getSecondMessage() {
        return secondMessage;
    }

    public String getAllMessages() {
        String message = getMessage();
        String secondMessageLocal = getSecondMessage();

        if (this.getCause() instanceof AppException) {
            message += "\n                                 | " + ((AppException) this.getCause()).getAllMessages();
        }
        if (secondMessageLocal != null) {
            message += "\n                                 | " + secondMessageLocal;
        }
        return message;
    }
}
