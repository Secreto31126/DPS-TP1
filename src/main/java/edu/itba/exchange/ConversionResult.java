package edu.itba.exchange;

import edu.itba.exchange.models.Money;
import edu.itba.exchange.models.Rate;

public sealed interface ConversionResult permits ConversionResult.Success, ConversionResult.Failure {
    record Success(Money money, Rate rate) implements ConversionResult {
    }

    record Failure(String errorMessage) implements ConversionResult {
    }
}
