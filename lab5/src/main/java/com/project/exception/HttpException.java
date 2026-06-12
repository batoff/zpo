package com.project.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

public class HttpException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final HttpStatusCode statusCode;

    public HttpException(HttpStatusCode httpStatusCode, HttpHeaders httpHeaders) {
        super(String.format("Error: statusCode - %s, headers - %s", httpStatusCode, httpHeaders));
        this.statusCode = httpStatusCode;
    }

    public HttpException(String errorMessage) {
        super(errorMessage);
        this.statusCode = null;
    }

    public HttpException(String errorMessage, Throwable err) {
        super(errorMessage, err);
        this.statusCode = null;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
