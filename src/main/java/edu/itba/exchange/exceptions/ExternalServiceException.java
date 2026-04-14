package edu.itba.exchange.exceptions;

public class ExternalServiceException extends CurrencyException {
    public ExternalServiceException(final ApiError apiError, final Throwable cause) {
        super(apiError, cause);
    }
}
