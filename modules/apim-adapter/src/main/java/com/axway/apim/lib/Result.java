package com.axway.apim.lib;

import java.util.ArrayList;
import java.util.List;

import com.axway.apim.lib.error.ErrorCode;

public class Result {

    private boolean hasError = false;

    private Object resultDetails;

    private final List<ErrorCode> errorCodes = new ArrayList<>();

    public ErrorCode getErrorCode() {
        if (!errorCodes.isEmpty()) {
            return errorCodes.get(0);
        } else {
            return ErrorCode.SUCCESS;
        }
    }

    public void setError(ErrorCode errorCode) {
        this.errorCodes.add(errorCode);
        this.hasError = true;
    }

    public int getRc() {
        return getErrorCode().getCode();
    }

    public boolean hasError() {
        return hasError;
    }

    public Object getResultDetails() {
        return resultDetails;
    }

    public void setResultDetails(Object resultDetails) {
        this.resultDetails = resultDetails;
    }

    @Override
    public String toString() {
        return "Result{" +
            "hasError=" + hasError +
            ", resultDetails=" + resultDetails +
            ", errorCodes=" + errorCodes +
            '}';
    }
}
