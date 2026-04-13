package edu.itba.exchange.exceptions;

import lombok.Getter;

@Getter
public abstract class CurrencyException extends RuntimeException {
    private final ApiError apiError;

    protected CurrencyException(final ApiError apiError) {
        super(apiError.message());
        this.apiError = apiError;
    }

    protected CurrencyException(final ApiError apiError, final Throwable cause) {
        super(apiError.message(), cause);
        this.apiError = apiError;
    }

}
