package edu.itba.exchange;

import edu.itba.exchange.models.Rate;

public sealed interface ExchangeRateResult permits ExchangeRateResult.Success, ExchangeRateResult.Failure {

    record Success(Rate rate) implements ExchangeRateResult {}

    record Failure(String errorMessage) implements ExchangeRateResult {}
}
