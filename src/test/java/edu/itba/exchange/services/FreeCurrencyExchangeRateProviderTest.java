package edu.itba.exchange.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.itba.exchange.exceptions.FetchException;
import edu.itba.exchange.exceptions.freecurrency.CurrencyConnectionException;
import edu.itba.exchange.exceptions.freecurrency.EndpointNotFoundException;
import edu.itba.exchange.exceptions.freecurrency.ForbiddenException;
import edu.itba.exchange.exceptions.freecurrency.InvalidCredentialsException;
import edu.itba.exchange.exceptions.freecurrency.RateLimitException;
import edu.itba.exchange.exceptions.freecurrency.ValidationErrorException;
import edu.itba.exchange.exceptions.freecurrency.validation.InvalidBaseCurrencyException;
import edu.itba.exchange.exceptions.freecurrency.validation.InvalidCurrenciesException;
import edu.itba.exchange.exceptions.freecurrency.validation.InvalidDateException;
import edu.itba.exchange.interfaces.Fetch;
import edu.itba.exchange.interfaces.JSON;
import edu.itba.exchange.interfaces.PropertiesProvider;
import edu.itba.exchange.models.Rate;
import edu.itba.exchange.services.dto.ExchangeCurrenciesResponse;
import edu.itba.exchange.services.dto.ExchangeRateResponse;
import edu.itba.exchange.services.dto.HistoricalExchangeRateResponse;
import edu.itba.exchange.services.dto.ValidationErrorResponse;

@ExtendWith(MockitoExtension.class)
class FreeCurrencyExchangeRateProviderTest {
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final Currency ARS = Currency.getInstance("ARS");

    private static final LocalDate DATE = LocalDate.parse("2024-01-01");

    private static final String BASE_URL = "http://localhost";
    private static final String API_TOKEN = "test-token";

    @Mock
    private Fetch fetch;

    @Mock
    private Fetch.Response responseMock;
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
    void testGetAvailableCurrencySuccess() throws FetchException {
        stubPropsAndOptions();

        final var response = new ExchangeCurrenciesResponse();
        response.setData(Map.of("USD", new ExchangeCurrenciesResponse.ExchangeCurrencyData(
                "$",
                "Dollar",
                "$",
                2,
                0,
                "USD",
                "Dollars")));

        when(responseMock.ok()).thenReturn(true);
        when(responseMock.json(any())).thenReturn(response);
        when(fetch.get(any(), any())).thenReturn(responseMock);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        final var result = provider.getAvailableCurrencies();
        assertThat(result, is(List.of(USD)));
    }

    @Test
    void testGetAvailableCurrencyWithFilter() throws FetchException {
        stubPropsAndOptions();

        final var response = new ExchangeCurrenciesResponse();
        response.setData(Map.of("USD", new ExchangeCurrenciesResponse.ExchangeCurrencyData(
                "$",
                "Dollar",
                "$",
                2,
                0,
                "USD",
                "Dollars")));

        when(responseMock.ok()).thenReturn(true);
        when(responseMock.json(any())).thenReturn(response);
        when(fetch.get(any(), any())).thenReturn(responseMock);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        final var result = provider.getAvailableCurrencies(List.of(USD));
        assertThat(result, is(List.of(USD)));
    }

    @Test
    void testGetRateSuccess() throws FetchException {
        stubPropsAndOptions();

        final var response = new ExchangeRateResponse();
        response.setData(Map.of("EUR", BigDecimal.TEN));

        when(responseMock.ok()).thenReturn(true);
        when(responseMock.json(any())).thenReturn(response);
        when(fetch.get(any(), any())).thenReturn(responseMock);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        final var rate = provider.getRate(USD, EUR);
        assertThat(rate.from(), is(USD));
        assertThat(rate.to(), is(EUR));
        assertThat(rate.value(), is(BigDecimal.TEN));
    }

    @Test
    void testGetRatesSuccess() throws FetchException {
        stubPropsAndOptions();

        final var response = new ExchangeRateResponse();
        response.setData(Map.of("EUR", BigDecimal.TEN, "GBP", BigDecimal.TWO));

        when(responseMock.ok()).thenReturn(true);
        when(responseMock.json(any())).thenReturn(response);
        when(fetch.get(any(), any())).thenReturn(responseMock);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        final var rates = provider.getRate(USD, List.of(EUR, GBP));

        assertThat(rates, hasSize(2));

        final var currencies = rates.stream().map(Rate::to).toList();
        assertThat(currencies, containsInAnyOrder(EUR, GBP));

        rates.forEach(rate -> {
            assertThat(rate.from(), is(USD));
            if (rate.to().equals(EUR))
                assertThat(rate.value(), is(BigDecimal.TEN));
            else
                assertThat(rate.value(), is(BigDecimal.TWO));
        });
    }

    @Test
    void testGetRateForHistoricalDateSuccess() throws FetchException {
        stubPropsAndOptions();

        final var response = new HistoricalExchangeRateResponse();
        response.setData(Map.of(DATE, Map.of(EUR, BigDecimal.TEN)));

        when(responseMock.ok()).thenReturn(true);
        when(responseMock.json(any())).thenReturn(response);
        when(fetch.get(any(), any())).thenReturn(responseMock);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        final var historicalRates = provider.getRate(USD, List.of(EUR), DATE);
        final var historicalRate = historicalRates.getFirst();

        assertThat(historicalRate.rateDate(), is(DATE));
        assertThat(historicalRate.from(), is(USD));
        assertThat(historicalRate.to(), is(EUR));
    }

    @Test
    void testInvalidToken() throws FetchException {
        stubPropsAndOptions();

        when(responseMock.ok()).thenReturn(false);
        when(responseMock.getStatus()).thenReturn(401);
        when(fetch.get(any(), any())).thenReturn(responseMock);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        assertThrows(InvalidCredentialsException.class, () -> provider.getRate(USD, EUR));
    }

    @Test
    void testForbidden() throws FetchException {
        stubPropsAndOptions();

        when(responseMock.ok()).thenReturn(false);
        when(responseMock.getStatus()).thenReturn(403);
        when(fetch.get(any(), any())).thenReturn(responseMock);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        assertThrows(ForbiddenException.class, () -> provider.getRate(USD, EUR));
    }

    @Test
    void testNotFound() throws FetchException {
        stubPropsAndOptions();

        when(responseMock.ok()).thenReturn(false);
        when(responseMock.getStatus()).thenReturn(404);
        when(fetch.get(any(), any())).thenReturn(responseMock);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        assertThrows(EndpointNotFoundException.class, () -> provider.getRate(USD, EUR));
    }

    @Test
    void testTooManyRequests() throws FetchException {
        stubPropsAndOptions();

        when(responseMock.ok()).thenReturn(false);
        when(responseMock.getStatus()).thenReturn(429);
        when(fetch.get(any(), any())).thenReturn(responseMock);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        assertThrows(RateLimitException.class, () -> provider.getRate(USD, EUR));
    }

    @Test
    void testUnprocessableEntityEmptyResponse() throws FetchException {
        stubPropsAndOptions();

        when(responseMock.ok()).thenReturn(false);
        when(responseMock.json(any())).thenReturn(null);
        when(responseMock.getStatus()).thenReturn(422);
        when(fetch.get(any(), any())).thenReturn(responseMock);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        assertThrows(ValidationErrorException.class, () -> provider.getRate(USD, EUR));
    }

    @Test
    void testUnprocessableEntityInvalidBaseCurrency() throws FetchException {
        stubPropsAndOptions();

        final var response = new ValidationErrorResponse("message", Map.of("base_currency", new String[] {}));

        when(responseMock.ok()).thenReturn(false);
        when(responseMock.json(any())).thenReturn(response);
        when(responseMock.getStatus()).thenReturn(422);
        when(fetch.get(any(), any())).thenReturn(responseMock);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        assertThrows(InvalidBaseCurrencyException.class, () -> provider.getRate(ARS, EUR));
    }

    @Test
    void testUnprocessableEntityInvalidCurrencies() throws FetchException {
        stubPropsAndOptions();

        final var response = new ValidationErrorResponse("message", Map.of("currencies", new String[] {}));

        when(responseMock.ok()).thenReturn(false);
        when(responseMock.json(any())).thenReturn(response);
        when(responseMock.getStatus()).thenReturn(422);
        when(fetch.get(any(), any())).thenReturn(responseMock);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        assertThrows(InvalidCurrenciesException.class, () -> provider.getRate(USD, ARS));
    }

    @Test
    void testUnprocessableEntityInvalidDate() throws FetchException {
        stubPropsAndOptions();

        final var response = new ValidationErrorResponse("message", Map.of("date", new String[] {}));

        when(responseMock.ok()).thenReturn(false);
        when(responseMock.json(any())).thenReturn(response);
        when(responseMock.getStatus()).thenReturn(422);
        when(fetch.get(any(), any())).thenReturn(responseMock);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        assertThrows(InvalidDateException.class, () -> provider.getRate(USD, List.of(EUR), DATE));
    }

    @Test
    void testUnprocessableEntityInvalid() throws FetchException {
        stubPropsAndOptions();

        final var response = new ValidationErrorResponse("message", Map.of());

        when(responseMock.ok()).thenReturn(false);
        when(responseMock.json(any())).thenReturn(response);
        when(responseMock.getStatus()).thenReturn(422);
        when(fetch.get(any(), any())).thenReturn(responseMock);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        assertThrows(ValidationErrorException.class, () -> provider.getRate(USD, EUR));
    }

    @Test
    void testConnectionFailure() throws FetchException {
        stubPropsAndOptions();

        when(fetch.get(any(), any())).thenThrow(new FetchException(null));

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        assertThrows(CurrencyConnectionException.class, () -> provider.getRate(USD, EUR));
    }

    @Test
    void testInvalidUrl() {
        when(props.get(eq("FREE_CURRENCY_EXCHANGE_API_BASE_URL"))).thenReturn("base url");

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props);

        assertThrows(CurrencyConnectionException.class, () -> provider.getRate(USD, EUR));
    }
}
