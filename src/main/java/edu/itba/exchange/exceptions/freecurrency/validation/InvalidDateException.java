package edu.itba.exchange.exceptions.freecurrency.validation;

import edu.itba.exchange.exceptions.freecurrency.ValidationErrorException;

public class InvalidDateException extends ValidationErrorException {
    private static final String MESSAGE = "The date is not a valid date. Please use the following format YYYY-MM-DD between 1999-01-01 and today.";

    public InvalidDateException() {
        super(MESSAGE);
    }
}
