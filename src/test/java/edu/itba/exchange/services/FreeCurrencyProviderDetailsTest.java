package edu.itba.exchange.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import edu.itba.exchange.exceptions.CurrencyException;
import edu.itba.exchange.exceptions.CurrencyNotFoundException;
import edu.itba.exchange.exceptions.ExternalServiceException;
import edu.itba.exchange.exceptions.FetchException;
import edu.itba.exchange.exceptions.freecurrency.InternalServerErrorException;
import edu.itba.exchange.exceptions.freecurrency.ValidationErrorException;
import edu.itba.exchange.exceptions.freecurrency.validation.InvalidBaseCurrencyException;
import edu.itba.exchange.exceptions.freecurrency.validation.InvalidCurrenciesException;
import edu.itba.exchange.exceptions.freecurrency.validation.InvalidDateException;
import edu.itba.exchange.interfaces.Fetch;
import edu.itba.exchange.interfaces.FetchExceptionMapper;
import edu.itba.exchange.interfaces.JSON;
import edu.itba.exchange.interfaces.PropertiesProvider;
import edu.itba.exchange.services.dto.ExchangeCurrenciesResponse;
import edu.itba.exchange.services.dto.ExchangeRateResponse;
import edu.itba.exchange.services.dto.HistoricalExchangeRateResponse;
import edu.itba.exchange.services.dto.ValidationErrorResponse;

import java.util.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FreeCurrencyProviderDetailsTest {
    private static final LocalDate DATE = LocalDate.parse("2024-01-01");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Mock
    private Fetch fetch;

    @Mock
    private PropertiesProvider props;

    @Mock
    private JSON json;

    @Test
    void testResponseDtos() {
        var resp = new ExchangeRateResponse();
        resp.setData(Map.of("USD", BigDecimal.ONE));
        assertEquals(BigDecimal.ONE, resp.getData().get("USD"));

        var hist = new HistoricalExchangeRateResponse();
        hist.setData(Map.of(DATE, Map.of(USD, BigDecimal.ONE)));
        assertEquals(BigDecimal.ONE, hist.getData().get(DATE).get(USD));

        var cur = new ExchangeCurrenciesResponse();
        var data = new ExchangeCurrenciesResponse.ExchangeCurrencyData(
                "$", "Dollar", "$", 2, 0, "USD", "Dollars");
        cur.setData(Map.of("USD", data));
        assertEquals(data, cur.getData().get("USD"));

        assertEquals("$", data.symbolNative());
        assertEquals(2, data.decimalDigits());
        assertEquals("Dollars", data.namePlural());
    }

    @Test
    void shouldThrowExternalServiceExceptionWhenUrlIsMalformed() {
        when(props.get(anyString())).thenReturn("http://invalid url.com");

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);

        assertThrows(ExternalServiceException.class, () -> provider.getRate(USD, List.of(EUR)));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            {"message": "", "errors": {"base_currency": [""]}} | base_currency
            {"message": "", "errors": {"currencies": [""]}} | currencies
            {"message": "", "errors": {"date": [""]}} | date
            {"message": "", "errors": {"unknown": [""]}} | unknown
            """)
    void shouldHandleIllegalArgumentExceptionInFormCurrencyResponse(final String body, final String type)
            throws FetchException {
        final var exceptionMapper = Map.of(
                "base_currency", InvalidBaseCurrencyException.class,
                "currencies", InvalidCurrenciesException.class,
                "date", InvalidDateException.class,
                "unknown", ValidationErrorException.class);

        final var jsonMapper = new ValidationErrorResponse("", Map.of(type, new String[] { "" }));
        when(json.parse(body, ValidationErrorResponse.class)).thenReturn(jsonMapper);

        when(fetch.getOptions()).thenReturn(mock(Fetch.Options.class));
        when(fetch.getJson(any(), any(), any())).thenThrow(new FetchException(422, body));
        when(props.get(anyString())).thenReturn("http://localhost");

        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);
        assertThrows(exceptionMapper.get(type), () -> provider.getAvailableCurrencies(List.of("XYZ")));
    }

    @Test
    void shouldGetAvailableCurrenciesWithNoFilter() throws Exception {
        Fetch mockFetch = mock(Fetch.class);
        PropertiesProvider props = key -> "http://localhost";
        when(mockFetch.getOptions()).thenReturn(mock(Fetch.Options.class));

        var curResp = new ExchangeCurrenciesResponse();
        curResp.setData(Map.of("USD", new ExchangeCurrenciesResponse.ExchangeCurrencyData(
                "$", "Dollar", "$", 2, 0, "USD", "Dollars")));
        when(mockFetch.getJson(any(), any(), eq(ExchangeCurrenciesResponse.class))).thenReturn(curResp);

        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(mockFetch, props,
                json);
        assertEquals(List.of(Currency.getInstance("USD")), provider.getAvailableCurrencies());
    }

    @Test
    void shouldGetRateForSingleCurrency() throws Exception {
        Fetch mockFetch = mock(Fetch.class);
        PropertiesProvider props = key -> "http://localhost";
        when(mockFetch.getOptions()).thenReturn(mock(Fetch.Options.class));

        var rateResp = new ExchangeRateResponse();
        rateResp.setData(Map.of("EUR", BigDecimal.TEN));
        when(mockFetch.getJson(any(), any(), eq(ExchangeRateResponse.class))).thenReturn(rateResp);

        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(mockFetch, props,
                json);
        var rate = provider.getRate(Currency.getInstance("USD"), Currency.getInstance("EUR"));
        assertEquals(Currency.getInstance("USD"), rate.from());
        assertEquals(Currency.getInstance("EUR"), rate.to());
        assertEquals(BigDecimal.TEN, rate.value());
    }

    @Test
    void shouldThrowWhenHistoricalRateFetchFails() throws FetchException {
        final var histResp = new HistoricalExchangeRateResponse();
        histResp.setData(Map.of());

        when(props.get(anyString())).thenReturn("http://localhost");
        when(fetch.getOptions()).thenReturn(mock(Fetch.Options.class));
        when(fetch.getJson(any(), any(), eq(HistoricalExchangeRateResponse.class)))
                .thenThrow(new FetchException(500, "Server Error"));

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);

        assertThrows(InternalServerErrorException.class, () -> provider.getRate(USD, List.of(EUR), DATE));
    }

    @Test
    void shouldReturnEmptyListWhenHistoricalDateNotInResponse() throws Exception {
        final var histResp = new HistoricalExchangeRateResponse();
        histResp.setData(Map.of());

        when(props.get(anyString())).thenReturn("http://localhost");
        when(fetch.getOptions()).thenReturn(mock(Fetch.Options.class));
        when(fetch.getJson(any(), any(), eq(HistoricalExchangeRateResponse.class))).thenReturn(histResp);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, props, json);

        assertThrows(CurrencyNotFoundException.class, () -> provider.getRate(USD, List.of(EUR), DATE));
    }
}
