package edu.itba.exchange.exceptions.freecurrency;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.Map;

import edu.itba.exchange.exceptions.freecurrency.validation.InvalidBaseCurrencyException;
import edu.itba.exchange.exceptions.freecurrency.validation.InvalidCurrenciesException;
import edu.itba.exchange.exceptions.freecurrency.validation.InvalidDateException;
import org.junit.jupiter.api.Test;

class ValidationErrorExceptionTest {

    @Test
    void shouldReturnValidationErrorExceptionWhenErrorsNull() {
        // When
        final var result = ValidationErrorException.fromErrors(null);

        // Then
        assertThat(result.getClass(), is(ValidationErrorException.class));
    }

    @Test
    void shouldReturnInvalidBaseCurrencyException() {
        // Given
        final var errors = Map.of("base_currency", new String[]{"is invalid"});

        // When
        final var result = ValidationErrorException.fromErrors(errors);

        // Then
        assertThat(result, is(instanceOf(InvalidBaseCurrencyException.class)));
    }

    @Test
    void shouldReturnInvalidCurrenciesException() {
        // Given
        final var errors = Map.of("currencies", new String[]{"is invalid"});

        // When
        final var result = ValidationErrorException.fromErrors(errors);

        // Then
        assertThat(result, is(instanceOf(InvalidCurrenciesException.class)));
    }

    @Test
    void shouldReturnInvalidDateException() {
        // Given
        final var errors = Map.of("date", new String[]{"is invalid"});

        // When
        final var result = ValidationErrorException.fromErrors(errors);

        // Then
        assertThat(result, is(instanceOf(InvalidDateException.class)));
    }

    @Test
    void shouldReturnValidationErrorExceptionForUnknownKey() {
        // Given
        final var errors = Map.of("unknown_field", new String[]{"some error"});

        // When
        final var result = ValidationErrorException.fromErrors(errors);

        // Then
        assertThat(result.getClass(), is(ValidationErrorException.class));
    }

    @Test
    void shouldReturnValidationErrorExceptionWhenErrorsEmpty() {
        // Given
        final var errors = Map.<String, String[]>of();

        // When
        final var result = ValidationErrorException.fromErrors(errors);

        // Then
        assertThat(result.getClass(), is(ValidationErrorException.class));
    }

    @Test
    void shouldCreateWithDefaultMessage() {
        // When
        final var exception = new ValidationErrorException();

        // Then
        assertThat(exception.getMessage(), containsString("Validation error"));
    }

    @Test
    void shouldCreateWithCustomMessage() {
        // Given
        final var customMessage = "Custom validation failure";

        // When
        final var exception = new ValidationErrorException(customMessage);

        // Then
        assertThat(exception.getMessage(), containsString(customMessage));
    }
}
