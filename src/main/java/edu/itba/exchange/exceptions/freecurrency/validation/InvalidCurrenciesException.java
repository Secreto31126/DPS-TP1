package edu.itba.exchange.exceptions.freecurrency.validation;

import edu.itba.exchange.exceptions.freecurrency.ValidationErrorException;

public class InvalidCurrenciesException extends ValidationErrorException {

    private static final String MESSAGE = "One of the selected currencies is invalid.";

    public InvalidCurrenciesException() {
        super(MESSAGE);
    }
}
