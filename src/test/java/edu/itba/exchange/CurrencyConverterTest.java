package edu.itba.exchange;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import edu.itba.exchange.exceptions.ApiError;
import edu.itba.exchange.exceptions.freecurrency.ValidationErrorException;
import edu.itba.exchange.exceptions.freecurrency.validation.InvalidCurrenciesException;
import edu.itba.exchange.exceptions.freecurrency.validation.InvalidDateException;
import edu.itba.exchange.models.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.itba.exchange.interfaces.ExchangeRateProvider;

@ExtendWith(MockitoExtension.class)
class CurrencyConverterTest {
    @Mock
    private ExchangeRateProvider provider;

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final Currency ARS = Currency.getInstance("ARS");
    private static final Rate EUR_USD_RATE = new Rate(EUR, USD, "1.05");
    private static final Rate EUR_GBP_RATE = new Rate(EUR, GBP, "0.85");
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 1);
    private static final Rate EUR_USD_RATE_HISTORICAL = new Rate(EUR, USD, "1.10", FIXED_DATE);
    private static final ApiError NETWORK_ERROR = ApiError.networkError("fail");
    private static final ApiError CLIENT_INPUT_ERROR = ApiError.fromHttpStatus(422, "");
    private static final ApiError CLIENT_DATE_ERROR = ApiError.fromHttpStatus(422, "");

    // --- convert(Money, Currency) ---

    @Test
    void shouldConvertSingleCurrency() {
        // Given
        final var euros = new Money("100", EUR);
        final var expectedDollars = new Money("105.00", USD);
        when(provider.getRate(EUR, List.of(USD))).thenReturn(List.of(EUR_USD_RATE));
        final var converter = new CurrencyConverter(provider);

        // When
        final var result = converter.convert(euros, USD);

        // Then
        assertThat(result, is(new ConversionResult.Success(expectedDollars, EUR_USD_RATE)));
        verify(provider).getRate(EUR, List.of(USD));
    }

    // --- convert(Money, List<Currency>) ---

    @Test
    void shouldConvertMultipleCurrencies() {
        // Given
        final var euros = new Money("100", EUR);
        when(provider.getRate(EUR, List.of(USD, GBP))).thenReturn(List.of(EUR_USD_RATE, EUR_GBP_RATE));
        final var converter = new CurrencyConverter(provider);

        // When
        final var results = converter.convert(euros, List.of(USD, GBP));

        // Then
        assertThat(results, hasSize(2));
        assertThat(results.get(0), is(new ConversionResult.Success(new Money("105.00", USD), EUR_USD_RATE)));
        assertThat(results.get(1), is(new ConversionResult.Success(new Money("85.00", GBP), EUR_GBP_RATE)));
        verify(provider).getRate(EUR, List.of(USD, GBP));
    }

    @Test
    void shouldReturnFailureWhenConvertThrows() {
        // Given
        final var euros = new Money("100", EUR);
        when(provider.getRate(EUR, List.of(ARS))).thenThrow(new InvalidCurrenciesException());
        final var converter = new CurrencyConverter(provider);

        // When
        final var results = converter.convert(euros, List.of(ARS));

        // Then
        assertThat(results.getFirst(), is(instanceOf(ConversionResult.Failure.class)));
    }

    // --- convert(Money, Currency, LocalDate) ---

    @Test
    void shouldConvertSingleCurrencyWithDate() {
        // Given
        final var euros = new Money("100", EUR);
        final var expectedDollars = new Money("110.00", USD);
        when(provider.getRate(EUR, List.of(USD), FIXED_DATE)).thenReturn(List.of(EUR_USD_RATE_HISTORICAL));
        final var converter = new CurrencyConverter(provider);

        // When
        final var result = converter.convert(euros, USD, FIXED_DATE);

        // Then
        assertThat(result, is(new ConversionResult.Success(expectedDollars, EUR_USD_RATE_HISTORICAL)));
    }

    // --- convert(Money, List<Currency>, LocalDate) ---

    @Test
    void shouldConvertMultipleCurrenciesWithDate() {
        // Given
        final var euros = new Money("100", EUR);
        when(provider.getRate(EUR, List.of(USD), FIXED_DATE)).thenReturn(List.of(EUR_USD_RATE_HISTORICAL));
        final var converter = new CurrencyConverter(provider);

        // When
        final var results = converter.convert(euros, List.of(USD), FIXED_DATE);

        // Then
        assertThat(results.size(), is(1));
        assertThat(results.getFirst(),
                is(new ConversionResult.Success(new Money("110.00", USD), EUR_USD_RATE_HISTORICAL)));
    }

    @Test
    void shouldReturnFailureWhenConvertWithDateThrows() {
        // Given
        final var euros = new Money("100", EUR);
        when(provider.getRate(EUR, List.of(USD), FIXED_DATE)).thenThrow(new InvalidDateException());
        final var converter = new CurrencyConverter(provider);

        // When
        final var results = converter.convert(euros, List.of(USD), FIXED_DATE);

        // Then
        assertThat(results.getFirst(), is(instanceOf(ConversionResult.Failure.class)));
    }

    // --- getExchangeRate(Currency, Currency) ---

    @Test
    void shouldGetExchangeRate() {
        // Given
        when(provider.getRate(EUR, List.of(USD))).thenReturn(List.of(EUR_USD_RATE));
        final var converter = new CurrencyConverter(provider);

        // When
        final var result = converter.getExchangeRate(EUR, USD);

        // Then
        assertThat(result, is(new ExchangeRateResult.Success(EUR_USD_RATE)));
        verify(provider).getRate(EUR, List.of(USD));
    }

    // --- getExchangeRate(Currency, List<Currency>) ---

    @Test
    void shouldGetExchangeRateList() {
        // Given
        when(provider.getRate(EUR, List.of(USD))).thenReturn(List.of(EUR_USD_RATE));
        final var converter = new CurrencyConverter(provider);

        // When
        final var results = converter.getExchangeRate(EUR, List.of(USD));

        // Then
        assertThat(results, is(List.of(new ExchangeRateResult.Success(EUR_USD_RATE))));
    }

    @Test
    void shouldReturnFailureWhenGetExchangeRateThrows() {
        // Given
        when(provider.getRate(EUR, List.of(ARS))).thenThrow(new InvalidCurrenciesException());
        final var converter = new CurrencyConverter(provider);

        // When
        final var results = converter.getExchangeRate(EUR, List.of(ARS));

        // Then
        assertThat(results.getFirst(), is(instanceOf(ExchangeRateResult.Failure.class)));
    }

    // --- getExchangeRate(Currency, Currency, LocalDate) ---

    @Test
    void shouldGetExchangeRateWithDate() {
        // Given
        when(provider.getRate(EUR, List.of(USD), FIXED_DATE)).thenReturn(List.of(EUR_USD_RATE_HISTORICAL));
        final var converter = new CurrencyConverter(provider);

        // When
        final var result = converter.getExchangeRate(EUR, USD, FIXED_DATE);

        // Then
        assertThat(result, is(new ExchangeRateResult.Success(EUR_USD_RATE_HISTORICAL)));
    }

    // --- getExchangeRate(Currency, List<Currency>, LocalDate) ---

    @Test
    void shouldGetExchangeRateListWithDate() {
        // Given
        when(provider.getRate(EUR, List.of(USD), FIXED_DATE)).thenReturn(List.of(EUR_USD_RATE_HISTORICAL));
        final var converter = new CurrencyConverter(provider);

        // When
        final var results = converter.getExchangeRate(EUR, List.of(USD), FIXED_DATE);

        // Then
        assertThat(results, is(List.of(new ExchangeRateResult.Success(EUR_USD_RATE_HISTORICAL))));
    }

    @Test
    void shouldReturnFailureWhenGetExchangeRateWithDateThrows() {
        // Given
        when(provider.getRate(EUR, List.of(ARS), FIXED_DATE)).thenThrow(new InvalidCurrenciesException());
        final var converter = new CurrencyConverter(provider);

        // When
        final var results = converter.getExchangeRate(EUR, List.of(ARS), FIXED_DATE);

        // Then
        assertThat(results.getFirst(), is(instanceOf(ExchangeRateResult.Failure.class)));
    }

    // --- getAvailableCurrencies() ---

    @Test
    void shouldGetAvailableCurrencies() {
        // Given
        when(provider.getAvailableCurrencies(List.of())).thenReturn(List.of(USD));
        final var converter = new CurrencyConverter(provider);

        // When
        final var result = converter.getAvailableCurrencies();

        // Then
        assertThat(result, is(new AvailableCurrenciesResult.Success(List.of(USD))));
        verify(provider).getAvailableCurrencies(List.of());
    }

    // --- getAvailableCurrencies(List<String>) ---

    @Test
    void shouldGetAvailableCurrenciesWithFilter() {
        // Given
        final var filter = List.of(USD);
        when(provider.getAvailableCurrencies(filter)).thenReturn(List.of(USD));
        final var converter = new CurrencyConverter(provider);

        // When
        final var result = converter.getAvailableCurrencies(filter);

        // Then
        assertThat(result, is(new AvailableCurrenciesResult.Success(List.of(USD))));
    }

    @Test
    void shouldReturnFailureWhenGetAvailableCurrenciesThrows() {
        // Given
        when(provider.getAvailableCurrencies(List.of())).thenThrow(new ValidationErrorException());
        final var converter = new CurrencyConverter(provider);

        // When
        final var result = converter.getAvailableCurrencies();

        // Then
        assertThat(result, is(instanceOf(AvailableCurrenciesResult.Failure.class)));
    }

    @Test
    void shouldReturnFailureWhenGetAvailableCurrenciesWithFilterThrows() {
        // Given
        final var filter = List.of(ARS);
        when(provider.getAvailableCurrencies(filter)).thenThrow(new InvalidCurrenciesException());
        final var converter = new CurrencyConverter(provider);

        // When
        final var result = converter.getAvailableCurrencies(filter);

        // Then
        assertThat(result, is(instanceOf(AvailableCurrenciesResult.Failure.class)));
    }
}
