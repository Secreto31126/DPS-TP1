package edu.itba.exchange.models;

import edu.itba.exchange.exceptions.ApiError;

public sealed interface ExchangeRateResult permits ExchangeRateResult.Success, ExchangeRateResult.Failure, ExchangeRateResult.ConnectionAbort {
    record Success(Rate rate) implements ExchangeRateResult {}

    record Failure(ApiError error) implements ExchangeRateResult {}
    record ConnectionAbort() implements ExchangeRateResult {}
}
