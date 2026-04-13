package edu.itba.exchange.exceptions.freecurrency.validation;

import edu.itba.exchange.exceptions.freecurrency.ValidationErrorException;

public class InvalidDateException extends ValidationErrorException {

    private static final String MESSAGE = "The date is not a valid date. Please use the following format: YYYY-MM-DD.";

    public InvalidDateException() {
        super(MESSAGE);
    }
}
