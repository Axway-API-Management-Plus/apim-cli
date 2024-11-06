package com.axway.apim.lib.error;

public class InternalException extends RuntimeException{

    public InternalException(String errorMessage){
        super(errorMessage);
    }
    public InternalException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    public InternalException(Throwable err) {
        super(err);
    }
}
