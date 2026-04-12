package edu.itba.exchange;

import static org.junit.jupiter.api.Assertions.*;

import edu.itba.exchange.models.Money;
import edu.itba.exchange.models.Rate;
import java.util.Currency;
import java.util.List;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ResultCoverageTest {
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Rate RATE = new Rate(EUR, USD, new BigDecimal("1.1"));
    private static final ApiError ERROR = ApiError.networkError("error");

    @Test
    void testExchangeRateResult() {
        var success = new ExchangeRateResult.Success(RATE);
        assertEquals(RATE, success.rate());
        
        var failure = new ExchangeRateResult.Failure(ERROR);
        assertEquals(ERROR, failure.error());
    }

    @Test
    void testConversionResult() {
        var money = new Money("100", USD);
        var success = new ConversionResult.Success(money, RATE);
        assertEquals(money, success.money());
        assertEquals(RATE, success.rate());

        var failure = new ConversionResult.Failure(ERROR);
        assertEquals(ERROR, failure.error());
    }

    @Test
    void testAvailableCurrenciesResult() {
        var currencies = List.of(USD);
        var success = new AvailableCurrenciesResult.Success(currencies);
        assertEquals(currencies, success.currencies());

        var failure = new AvailableCurrenciesResult.Failure(ERROR);
        assertEquals(ERROR, failure.error());
    }
}