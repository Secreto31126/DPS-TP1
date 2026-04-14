package edu.itba.exchange.exceptions;

public class CurrencyNotFoundException extends CurrencyException {
    public CurrencyNotFoundException(final ApiError apiError) {
        super(apiError);
    }
}
