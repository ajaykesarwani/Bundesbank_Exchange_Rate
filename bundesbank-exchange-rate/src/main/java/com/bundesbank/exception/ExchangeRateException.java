package com.bundesbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ExchangeRateException extends RuntimeException {
    private final String errorCode;

    public ExchangeRateException(String message) {
        super(message);
        this.errorCode = "EXCHANGE_RATE_ERROR";
    }

    public ExchangeRateException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}