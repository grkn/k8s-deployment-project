package com.k8s.challenge.constant;

import org.springframework.http.HttpStatus;

public enum ExceptionResponse {
    GENERAL("k8s-0000", HttpStatus.INTERNAL_SERVER_ERROR, "Unknown exception occured."),
    UNAUTHORIZED("k8s-1000", HttpStatus.UNAUTHORIZED, "The request is unauthorized."),
    BAD_REQUEST("k8s-1001", HttpStatus.BAD_REQUEST, "%s"),
    NOT_FOUND("k8s-1002", HttpStatus.NOT_FOUND, "%s"),
    SERVER_ERROR("k8s-1003", HttpStatus.INTERNAL_SERVER_ERROR, "Server error. Please contact your administrator");

    private String code;
    private HttpStatus status;
    private String message;

    ExceptionResponse(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
