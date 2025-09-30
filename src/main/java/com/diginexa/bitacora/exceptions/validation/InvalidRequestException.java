package com.diginexa.bitacora.exceptions.validation;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
