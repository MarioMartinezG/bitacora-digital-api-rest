package com.diginexa.bitacora.exceptions.domain;

public class TutorResourceNotFoundException extends RuntimeException {

    private final String detail;

    public TutorResourceNotFoundException(String detail) {
        super(detail);
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }
}
