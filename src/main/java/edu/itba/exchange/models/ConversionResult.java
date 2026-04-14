package edu.itba.exchange.models;

import edu.itba.exchange.exceptions.ApiError;

public sealed interface ConversionResult
        permits ConversionResult.Success, ConversionResult.Failure, ConversionResult.ConnectionAbort {
    record Success(Money money, Rate rate) implements ConversionResult {
    }

    record Failure(ApiError error) implements ConversionResult {
    }

    record ConnectionAbort() implements ConversionResult {
    }
}
