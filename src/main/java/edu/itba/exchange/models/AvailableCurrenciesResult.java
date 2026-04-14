package edu.itba.exchange.models;

import java.util.Currency;
import java.util.List;

import edu.itba.exchange.exceptions.ApiError;

public sealed interface AvailableCurrenciesResult
        permits AvailableCurrenciesResult.Success, AvailableCurrenciesResult.Failure,
        AvailableCurrenciesResult.ConnectionAbort {

    record Success(List<Currency> currencies) implements AvailableCurrenciesResult {
    }

    record Failure(ApiError error) implements AvailableCurrenciesResult {
    }

    record ConnectionAbort() implements AvailableCurrenciesResult {
    }
}
