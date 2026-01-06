package com.diginexa.bitacora.exceptions.domain;

public class TutorServiceException extends RuntimeException {

    private final String errorCode;

    public TutorServiceException(String message) {
        super(message);
        this.errorCode = "TUTOR_SERVICE_ERROR";
    }

    public TutorServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public TutorServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "TUTOR_SERVICE_ERROR";
    }

    public TutorServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
