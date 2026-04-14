package edu.itba.exchange.exceptions.freecurrency;

import edu.itba.exchange.exceptions.ApiError;
import edu.itba.exchange.exceptions.CurrencyException;
import edu.itba.exchange.interfaces.HttpStatus;

public class InternalServerErrorException extends CurrencyException {
    private static final String MESSAGE = "Internal Server Error.";

    public InternalServerErrorException() {
        final var apiError = ApiError.fromHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR, MESSAGE);
        super(apiError);
    }
}
