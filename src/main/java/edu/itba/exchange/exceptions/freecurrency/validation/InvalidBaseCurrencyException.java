package edu.itba.exchange.exceptions.freecurrency.validation;

import edu.itba.exchange.exceptions.freecurrency.ValidationErrorException;

public class InvalidBaseCurrencyException extends ValidationErrorException {

    private static final String MESSAGE = "The selected base currency is invalid, to get a full list of all currencies you can use the currency endpoint.";

    public InvalidBaseCurrencyException() {
        super(MESSAGE);
    }
}
