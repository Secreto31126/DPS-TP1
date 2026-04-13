package edu.itba.exchange.models;

import edu.itba.exchange.exceptions.ApiError;

import java.util.Currency;
import java.util.List;

public sealed interface AvailableCurrenciesResult permits AvailableCurrenciesResult.Success, AvailableCurrenciesResult.Failure {
    record Success(List<Currency> currencies) implements AvailableCurrenciesResult {}

    record Failure(ApiError error) implements AvailableCurrenciesResult {}
}
