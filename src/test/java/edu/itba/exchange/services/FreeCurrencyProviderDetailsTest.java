package edu.itba.exchange.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import edu.itba.exchange.exceptions.CurrencyException;
import edu.itba.exchange.exceptions.CurrencyNotFoundException;
import edu.itba.exchange.exceptions.ExternalServiceException;
import edu.itba.exchange.exceptions.FetchException;
import edu.itba.exchange.exceptions.freecurrency.InternalServerErrorException;
import edu.itba.exchange.interfaces.Fetch;
import edu.itba.exchange.interfaces.FetchExceptionMapper;
import edu.itba.exchange.interfaces.PropertiesProvider;
import edu.itba.exchange.services.dto.ExchangeCurrenciesResponse;
import edu.itba.exchange.services.dto.ExchangeRateResponse;
import edu.itba.exchange.services.dto.HistoricalExchangeRateResponse;
import java.util.Currency;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import org.junit.jupiter.api.Test;

class FreeCurrencyProviderDetailsTest {

    @Test
    void testResponseDtos() {
        var resp = new ExchangeRateResponse();
        resp.setData(Map.of("USD", BigDecimal.ONE));
        assertEquals(BigDecimal.ONE, resp.getData().get("USD"));

        var hist = new HistoricalExchangeRateResponse();
        hist.setData(Map.of("2024-01-01", Map.of("USD", BigDecimal.ONE)));
        assertEquals(BigDecimal.ONE, hist.getData().get("2024-01-01").get("USD"));

        var cur = new ExchangeCurrenciesResponse();
        var data = new ExchangeCurrenciesResponse.ExchangeCurrencyData(
            "$", "Dollar", "$", 2, 0, "USD", "Dollars"
        );
        cur.setData(Map.of("USD", data));
        assertEquals(data, cur.getData().get("USD"));

        assertEquals("$", data.symbolNative());
        assertEquals(2, data.decimalDigits());
        assertEquals("Dollars", data.namePlural());
    }

    @SuppressWarnings("unchecked")
    private static FetchExceptionMapper<CurrencyException> mockMapper() {
        return mock(FetchExceptionMapper.class);
    }

    @Test
    void shouldThrowExternalServiceExceptionWhenUrlIsMalformed() {
        PropertiesProvider badProps = key -> "http://invalid url.com";
        Fetch mockFetch = mock(Fetch.class);
        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(mockFetch, badProps, mockMapper());
        assertThrows(ExternalServiceException.class, () -> provider.getRate(Currency.getInstance("USD"), List.of(Currency.getInstance("EUR"))));
    }

    @Test
    void shouldHandleIllegalArgumentExceptionInFormCurrencyResponse() throws FetchException {
        Fetch mockFetch = mock(Fetch.class);
        when(mockFetch.getOptions()).thenReturn(mock(Fetch.Options.class));
        when(mockFetch.getJson(any(), any(), any())).thenThrow(new IllegalArgumentException("Invalid code"));

        PropertiesProvider props = key -> "http://localhost";
        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(mockFetch, props, mockMapper());
        assertThrows(CurrencyNotFoundException.class, () -> provider.getAvailableCurrencies(List.of("XYZ")));
    }

    @Test
    void shouldGetAvailableCurrenciesWithNoFilter() throws Exception {
        Fetch mockFetch = mock(Fetch.class);
        PropertiesProvider props = key -> "http://localhost";
        when(mockFetch.getOptions()).thenReturn(mock(Fetch.Options.class));

        var curResp = new ExchangeCurrenciesResponse();
        curResp.setData(Map.of("USD", new ExchangeCurrenciesResponse.ExchangeCurrencyData(
            "$", "Dollar", "$", 2, 0, "USD", "Dollars"
        )));
        when(mockFetch.getJson(any(), any(), eq(ExchangeCurrenciesResponse.class))).thenReturn(curResp);

        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(mockFetch, props, mockMapper());
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

        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(mockFetch, props, mockMapper());
        var rate = provider.getRate(Currency.getInstance("USD"), Currency.getInstance("EUR"));
        assertEquals(Currency.getInstance("USD"), rate.from());
        assertEquals(Currency.getInstance("EUR"), rate.to());
        assertEquals(BigDecimal.TEN, rate.value());
    }

    @Test
    void shouldThrowWhenHistoricalRateFetchFails() throws FetchException {
        Fetch mockFetch = mock(Fetch.class);
        PropertiesProvider props = key -> "http://localhost";
        FetchExceptionMapper<CurrencyException> mapper = mockMapper();
        when(mockFetch.getOptions()).thenReturn(mock(Fetch.Options.class));
        when(mockFetch.getJson(any(), any(), eq(HistoricalExchangeRateResponse.class)))
            .thenThrow(new FetchException(500, "Server Error"));
        when(mapper.translate(any())).thenReturn(new InternalServerErrorException());

        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(mockFetch, props, mapper);
        assertThrows(InternalServerErrorException.class,
            () -> provider.getRate(Currency.getInstance("USD"), List.of(Currency.getInstance("EUR")), java.time.LocalDate.of(2024, 1, 1)));
    }

    @Test
    void shouldReturnEmptyListWhenHistoricalDateNotInResponse() throws Exception {
        Fetch mockFetch = mock(Fetch.class);
        PropertiesProvider props = key -> "http://localhost";
        when(mockFetch.getOptions()).thenReturn(mock(Fetch.Options.class));

        var histResp = new HistoricalExchangeRateResponse();
        histResp.setData(Map.of("2020-01-01", Map.of("USD", BigDecimal.ONE)));
        when(mockFetch.getJson(any(), any(), eq(HistoricalExchangeRateResponse.class))).thenReturn(histResp);

        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(mockFetch, props, mockMapper());
        assertTrue(provider.getRate(Currency.getInstance("USD"), List.of(Currency.getInstance("EUR")), java.time.LocalDate.of(2024, 1, 1)).isEmpty());
    }
}