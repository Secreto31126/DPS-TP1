package edu.itba.exchange.exceptions.freecurrency;

import edu.itba.exchange.exceptions.ApiError;
import edu.itba.exchange.interfaces.HttpStatus;
import edu.itba.exchange.exceptions.CurrencyException;

public class EndpointNotFoundException extends CurrencyException {
    private static final String MESSAGE = "A requested endpoint does not exist.";

    public EndpointNotFoundException() {
        final var apiError = ApiError.fromHttpStatus(HttpStatus.NOT_FOUND, MESSAGE);
        super(apiError);
    }
}
