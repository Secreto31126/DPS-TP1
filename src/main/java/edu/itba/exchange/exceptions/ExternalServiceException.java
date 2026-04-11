package edu.itba.exchange.exceptions;

import edu.itba.exchange.ApiError;

public class ExternalServiceException extends CurrencyException {
    public ExternalServiceException(final ApiError apiError, final Throwable cause) {
        super(apiError, cause);
    }
}