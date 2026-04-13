package edu.itba.exchange.exceptions.freecurrency;

import edu.itba.exchange.exceptions.ApiError;
import edu.itba.exchange.interfaces.HttpStatus;
import edu.itba.exchange.exceptions.CurrencyException;

public class InvalidCredentialsException extends CurrencyException {

    private static final String MESSAGE = "Invalid authentication credentials.";

    public InvalidCredentialsException() {
        final var apiError = ApiError.fromHttpStatus(HttpStatus.UNAUTHORIZED, MESSAGE);
        super(apiError);
    }
}
