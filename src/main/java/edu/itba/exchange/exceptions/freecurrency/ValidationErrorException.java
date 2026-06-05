package edu.itba.exchange.exceptions.freecurrency;

import java.util.Map;

import edu.itba.exchange.exceptions.ApiError;
import edu.itba.exchange.exceptions.CurrencyException;
import edu.itba.exchange.exceptions.freecurrency.validation.InvalidBaseCurrencyException;
import edu.itba.exchange.exceptions.freecurrency.validation.InvalidCurrenciesException;
import edu.itba.exchange.exceptions.freecurrency.validation.InvalidDateException;
import edu.itba.exchange.interfaces.HttpStatus;

public class ValidationErrorException extends CurrencyException {
    private static final String MESSAGE = "Validation error, please check the list of validation errors.";

    public ValidationErrorException() {
        this(MESSAGE);
    }

    public ValidationErrorException(String message) {
        final var apiError = ApiError.fromHttpStatus(HttpStatus.UNPROCESSABLE_ENTITY, message);
        super(apiError);
    }

    public static ValidationErrorException fromErrors(Map<String, String[]> errors) {
        if (errors == null)
            return new ValidationErrorException();
        if (errors.containsKey("base_currency"))
            return new InvalidBaseCurrencyException();
        if (errors.containsKey("currencies"))
            return new InvalidCurrenciesException();
        if (errors.containsKey("date"))
            return new InvalidDateException();
        return new ValidationErrorException();
    }
}
