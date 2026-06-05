package edu.itba.exchange.exceptions.freecurrency;

import edu.itba.exchange.exceptions.ApiError;
import edu.itba.exchange.exceptions.CurrencyException;
import edu.itba.exchange.interfaces.HttpStatus;

public class RateLimitException extends CurrencyException {
    private static final String MESSAGE = "You have hit your rate limit or your monthly limit. For more requests please upgrade your plan.";

    public RateLimitException() {
        final var apiError = ApiError.fromHttpStatus(HttpStatus.TOO_MANY_REQUESTS, MESSAGE);
        super(apiError);
    }
}
