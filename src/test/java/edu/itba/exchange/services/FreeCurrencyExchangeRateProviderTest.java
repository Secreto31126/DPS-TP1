package edu.itba.exchange.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import edu.itba.exchange.exceptions.CurrencyNotFoundException;
import edu.itba.exchange.exceptions.ExternalServiceException;
import edu.itba.exchange.exceptions.FetchException;
import edu.itba.exchange.exceptions.freecurrency.InternalServerErrorException;
import edu.itba.exchange.interfaces.Fetch;
import edu.itba.exchange.interfaces.JSON;
import edu.itba.exchange.interfaces.PropertiesProvider;
import edu.itba.exchange.models.Rate;
import edu.itba.exchange.services.dto.ExchangeCurrenciesResponse;
import edu.itba.exchange.services.dto.ExchangeRateResponse;
import edu.itba.exchange.services.dto.HistoricalExchangeRateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FreeCurrencyExchangeRateProviderTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final LocalDate DATE = LocalDate.parse("2024-01-01");
    private static final String BASE_URL = "http://localhost";
    private static final String API_TOKEN = "test-token";

    @Mock
    private Fetch fetch;

    @Mock
    private PropertiesProvider props;

    @Mock
    private JSON json;

    private void stubPropsAndOptions() {
        when(props.get(eq("FREE_CURRENCY_EXCHANGE_API_BASE_URL"))).thenReturn(BASE_URL);
        when(props.get(eq("FREE_CURRENCY_EXCHANGE_API_TOKEN"))).thenReturn(API_TOKEN);

        final var optionsMock = mock(Fetch.Options.class);
        when(optionsMock.addHeader(eq("apikey"), eq(API_TOKEN))).thenReturn(optionsMock);
        when(fetch.getOptions()).thenReturn(optionsMock);
    }

    // --- getAvailableCurrencies ---

    @Test
    void shouldGetAvailableCurrencies() throws FetchException {
        // Given
        stubPropsAndOptions();

        final var response = new ExchangeCurrenciesResponse();
        response.setData(Map.of("USD", new ExchangeCurrenciesResponse.ExchangeCurrencyData(
                "$", "Dollar", "$", 2, 0, "USD", "Dollars")));
        when(fetch.getJson(argThat(url -> url.getPath().contains("/v1/currencies")), any(), eq(ExchangeCurrenciesResponse.class))).thenReturn(response);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);

        // When
        final var result = provider.getAvailableCurrencies();

        // Then
        assertThat(result, is(List.of(USD)));
    }

    @Test
    void shouldGetAvailableCurrenciesWithFilter() throws FetchException {
        // Given
        stubPropsAndOptions();

        final var response = new ExchangeCurrenciesResponse();
        response.setData(Map.of("USD", new ExchangeCurrenciesResponse.ExchangeCurrencyData(
                "$", "Dollar", "$", 2, 0, "USD", "Dollars")));
        when(fetch.getJson(argThat(url -> url.getPath().contains("/v1/currencies")), any(), eq(ExchangeCurrenciesResponse.class))).thenReturn(response);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);

        // When
        final var result = provider.getAvailableCurrencies(List.of("USD"));

        // Then
        assertThat(result, is(List.of(USD)));
    }

    @Test
    void shouldReturnEmptyListWhenNoCurrenciesInResponse() throws FetchException {
        // Given
        stubPropsAndOptions();

        final var response = new ExchangeCurrenciesResponse();
        response.setData(Map.of());
        when(fetch.getJson(argThat(url -> url.getPath().contains("/v1/currencies")), any(), eq(ExchangeCurrenciesResponse.class))).thenReturn(response);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);

        // When
        final var result = provider.getAvailableCurrencies();

        // Then
        assertThat(result, is(empty()));
    }

    // --- getRate ---

    @Test
    void shouldGetRateForSingleCurrency() throws FetchException {
        // Given
        stubPropsAndOptions();

        final var response = new ExchangeRateResponse();
        response.setData(Map.of("EUR", BigDecimal.TEN));
        when(fetch.getJson(argThat(url -> url.getPath().contains("/v1/latest")), any(), eq(ExchangeRateResponse.class))).thenReturn(response);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);

        // When
        final var rate = provider.getRate(USD, EUR);

        // Then
        assertThat(rate.from(), is(USD));
        assertThat(rate.to(), is(EUR));
        assertThat(rate.value(), is(BigDecimal.TEN));
    }

    @Test
    void shouldGetRateForMultipleCurrencies() throws FetchException {
        // Given
        stubPropsAndOptions();

        final var response = new ExchangeRateResponse();
        response.setData(Map.of("EUR", BigDecimal.TEN, "GBP", BigDecimal.TWO));
        when(fetch.getJson(argThat(url -> url.getPath().contains("/v1/latest")), any(), eq(ExchangeRateResponse.class))).thenReturn(response);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);

        // When
        final var rates = provider.getRate(USD, List.of(EUR, GBP));

        // Then
        assertThat(rates, hasSize(2));

        final var currencies = rates.stream().map(Rate::to).toList();
        assertThat(currencies, containsInAnyOrder(EUR, GBP));

        rates.forEach(rate -> {
            assertThat(rate.from(), is(USD));
            if (rate.to().equals(EUR)) assertThat(rate.value(), is(BigDecimal.TEN));
            else assertThat(rate.value(), is(BigDecimal.TWO));
        });
    }

    @Test
    void shouldReturnEmptyListWhenNoRatesInResponse() throws FetchException {
        // Given
        stubPropsAndOptions();

        final var response = new ExchangeRateResponse();
        response.setData(Map.of());
        when(fetch.getJson(argThat(url -> url.getPath().contains("/v1/latest")), any(), eq(ExchangeRateResponse.class))).thenReturn(response);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);

        // When
        final var rates = provider.getRate(USD, List.of(EUR));

        // Then
        assertThat(rates, is(empty()));
    }

    // --- getRate historical ---

    @Test
    void shouldGetHistoricalRate() throws FetchException {
        // Given
        stubPropsAndOptions();

        final var response = new HistoricalExchangeRateResponse();
        response.setData(Map.of(DATE, Map.of(EUR, BigDecimal.TEN)));
        when(fetch.getJson(argThat(url -> url.getPath().contains("/v1/historical")), any(), eq(HistoricalExchangeRateResponse.class))).thenReturn(response);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);

        // When
        final var rates = provider.getRate(USD, List.of(EUR), DATE);

        // Then
        assertThat(rates, hasSize(1));
        assertThat(rates.getFirst().from(), is(USD));
        assertThat(rates.getFirst().to(), is(EUR));
        assertThat(rates.getFirst().rateDate(), is(DATE));
        assertThat(rates.getFirst().value(), is(BigDecimal.TEN));
    }

    @Test
    void shouldGetHistoricalRateForMultipleCurrencies() throws FetchException {
        // Given
        stubPropsAndOptions();

        final var response = new HistoricalExchangeRateResponse();
        response.setData(Map.of(DATE, Map.of(EUR, BigDecimal.TEN, GBP, BigDecimal.TWO)));
        when(fetch.getJson(argThat(url -> url.getPath().contains("/v1/historical")), any(), eq(HistoricalExchangeRateResponse.class))).thenReturn(response);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);

        // When
        final var rates = provider.getRate(USD, List.of(EUR, GBP), DATE);

        // Then
        assertThat(rates, hasSize(2));

        final var currencies = rates.stream().map(Rate::to).toList();
        assertThat(currencies, containsInAnyOrder(EUR, GBP));

        rates.forEach(rate -> assertThat(rate.from(), is(USD)));
    }

    @Test
    void shouldThrowCurrencyNotFoundWhenHistoricalDateMissing() throws FetchException {
        // Given
        stubPropsAndOptions();

        final var response = new HistoricalExchangeRateResponse();
        response.setData(Map.of());
        when(fetch.getJson(argThat(url -> url.getPath().contains("/v1/historical")), any(), eq(HistoricalExchangeRateResponse.class))).thenReturn(response);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);

        // When / Then
        assertThrows(CurrencyNotFoundException.class, () -> provider.getRate(USD, List.of(EUR), DATE));
    }

    // --- Error handling ---

    @Test
    void shouldThrowExternalServiceExceptionWhenUrlIsMalformed() {
        // Given
        when(props.get(eq("FREE_CURRENCY_EXCHANGE_API_BASE_URL"))).thenReturn("http://invalid url.com");

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);

        // When / Then
        assertThrows(ExternalServiceException.class, () -> provider.getRate(USD, List.of(EUR)));
    }

    @Test
    void shouldTranslateFetchExceptionToCurrencyException() throws FetchException {
        // Given
        stubPropsAndOptions();
        when(fetch.getJson(argThat(url -> url.getPath().contains("/v1/latest")), any(), eq(ExchangeRateResponse.class)))
                .thenThrow(new FetchException(500, "Server Error"));

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);

        // When / Then
        assertThrows(InternalServerErrorException.class, () -> provider.getRate(USD, List.of(EUR)));
    }
}
