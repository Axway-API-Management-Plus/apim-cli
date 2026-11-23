package com.axway.apim.lib.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Creates a machine-readable, one-line JSON summary of an error, which is easy to parse in a pipeline (issue #594):
 * <pre>{"errorCode":15,"error":"BREAKING_CHANGE_DETECTED","mappedErrorCode":15,"message":"...","details":["..."]}</pre>
 * <ul>
 *     <li>errorCode: the code of the error</li>
 *     <li>error: the name of the error</li>
 *     <li>mappedErrorCode: the code returned by the CLI, which is different if the error is mapped using returnCodeMapping</li>
 *     <li>message: the message of the error</li>
 *     <li>details: the messages of the underlying causes</li>
 * </ul>
 */
public final class ErrorDetails {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_CAUSES = 10;

    private ErrorDetails() {
    }

    public static String toJson(AppException exception, ErrorCode mappedErrorCode) {
        ObjectNode node = MAPPER.createObjectNode();
        ErrorCode error = exception.getError();
        node.put("errorCode", error.getCode());
        node.put("error", error.name());
        node.put("mappedErrorCode", mappedErrorCode.getCode());
        node.put("message", exception.getMessage());
        ArrayNode details = node.putArray("details");
        for (String detail : getDetails(exception)) {
            details.add(detail);
        }
        try {
            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            // Cannot happen for the types used
            return "{\"errorCode\":" + error.getCode() + ",\"error\":\"" + error.name() + "\"}";
        }
    }

    private static Set<String> getDetails(AppException exception) {
        Set<String> details = new LinkedHashSet<>();
        Throwable current = exception;
        for (int i = 0; current != null && i < MAX_CAUSES; i++) {
            if (current != exception && current.getMessage() != null) {
                details.add(current.getMessage());
            }
            if (current instanceof AppException && ((AppException) current).getSecondMessage() != null) {
                details.add(((AppException) current).getSecondMessage());
            }
            current = current.getCause() == current ? null : current.getCause();
        }
        details.remove(exception.getMessage());
        return details;
    }
}
