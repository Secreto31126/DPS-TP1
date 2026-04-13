package edu.itba.exchange.exceptions;

public class CurrencyNotFoundException extends CurrencyException {
    public CurrencyNotFoundException(final ApiError apiError) {
        super(apiError);
    }

    public CurrencyNotFoundException(final ApiError apiError, final Throwable cause) {
        super(apiError, cause);
    }
}
