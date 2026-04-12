package edu.itba.exchange.exceptions.freecurrency;

import edu.itba.exchange.exceptions.ApiError;
import edu.itba.exchange.interfaces.HttpStatus;
import edu.itba.exchange.exceptions.CurrencyException;

public class ForbiddenException extends CurrencyException {

    private static final String MESSAGE = "You are not allowed to use this endpoint, please upgrade your plan.";

    public ForbiddenException() {
        var apiError = ApiError.fromHttpStatus(HttpStatus.FORBIDDEN, MESSAGE);
        super(apiError);
    }
}
