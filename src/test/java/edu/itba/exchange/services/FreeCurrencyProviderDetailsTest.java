package edu.itba.exchange.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import edu.itba.exchange.interfaces.Fetch;
import edu.itba.exchange.interfaces.PropertiesProvider;
import edu.itba.exchange.exceptions.CurrencyNotFoundException;
import edu.itba.exchange.exceptions.ExternalServiceException;
import edu.itba.exchange.exceptions.FetchException;
import java.util.Currency;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import org.junit.jupiter.api.Test;

class FreeCurrencyProviderDetailsTest {

    @Test
    void testInnerClasses() {
        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(null, null);
        
        var resp = provider.new ExchangeRateResponse();
        resp.setData(Map.of("USD", BigDecimal.ONE));
        assertEquals(BigDecimal.ONE, resp.getData().get("USD"));

        var hist = provider.new HistoricalExchangeRateResponse();
        hist.setData(Map.of("2024-01-01", Map.of("USD", BigDecimal.ONE)));
        assertEquals(BigDecimal.ONE, hist.getData().get("2024-01-01").get("USD"));

        var cur = provider.new ExchangeCurrenciesResponse();
        var data = new FreeCurrencyExchangeRateProvider.ExchangeCurrenciesResponse.ExchangeCurrencyData(
            "$", "Dollar", "$", 2, 0, "USD", "Dollars"
        );
        cur.setData(Map.of("USD", data));
        assertEquals(data, cur.getData().get("USD"));
        
        assertEquals("$", data.symbolNative());
        assertEquals(2, data.decimalDigits());
        assertEquals("Dollars", data.namePlural());
    }

    @Test
    void shouldThrowExternalServiceExceptionWhenUrlIsMalformed() {
        PropertiesProvider badProps = key -> "http://invalid url.com";
        Fetch mockFetch = mock(Fetch.class);
        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(mockFetch, badProps);
        assertThrows(ExternalServiceException.class, () -> provider.getRate(Currency.getInstance("USD"), List.of(Currency.getInstance("EUR"))));
    }

    @Test
    void shouldHandleIllegalArgumentExceptionInFormCurrencyResponse() throws FetchException {
        Fetch mockFetch = mock(Fetch.class);
        when(mockFetch.getOptions()).thenReturn(mock(Fetch.Options.class));
        when(mockFetch.getJson(any(), any(), any())).thenThrow(new IllegalArgumentException("Invalid code"));
        
        PropertiesProvider props = key -> "http://localhost";
        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(mockFetch, props);
        assertThrows(CurrencyNotFoundException.class, () -> provider.getAvailableCurrencies(List.of("XYZ")));
    }

    @Test
    void shouldGetAvailableCurrenciesWithNoFilter() throws Exception {
        Fetch mockFetch = mock(Fetch.class);
        PropertiesProvider props = key -> "http://localhost";
        when(mockFetch.getOptions()).thenReturn(mock(Fetch.Options.class));

        FreeCurrencyExchangeRateProvider dummy = new FreeCurrencyExchangeRateProvider(null, null);
        var curResp = dummy.new ExchangeCurrenciesResponse();
        curResp.setData(Map.of("USD", new FreeCurrencyExchangeRateProvider.ExchangeCurrenciesResponse.ExchangeCurrencyData(
            "$", "Dollar", "$", 2, 0, "USD", "Dollars"
        )));
        when(mockFetch.getJson(any(), any(), eq(FreeCurrencyExchangeRateProvider.ExchangeCurrenciesResponse.class))).thenReturn(curResp);

        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(mockFetch, props);
        assertEquals(List.of(Currency.getInstance("USD")), provider.getAvailableCurrencies());
    }

    @Test
    void shouldGetRateForSingleCurrency() throws Exception {
        Fetch mockFetch = mock(Fetch.class);
        PropertiesProvider props = key -> "http://localhost";
        when(mockFetch.getOptions()).thenReturn(mock(Fetch.Options.class));

        FreeCurrencyExchangeRateProvider dummy = new FreeCurrencyExchangeRateProvider(null, null);
        var rateResp = dummy.new ExchangeRateResponse();
        rateResp.setData(Map.of("EUR", BigDecimal.TEN));
        when(mockFetch.getJson(any(), any(), eq(FreeCurrencyExchangeRateProvider.ExchangeRateResponse.class))).thenReturn(rateResp);

        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(mockFetch, props);
        var rate = provider.getRate(Currency.getInstance("USD"), Currency.getInstance("EUR"));
        assertEquals(Currency.getInstance("USD"), rate.from());
        assertEquals(Currency.getInstance("EUR"), rate.to());
        assertEquals(BigDecimal.TEN, rate.value());
    }

    @Test
    void shouldThrowWhenHistoricalRateFetchFails() throws FetchException {
        Fetch mockFetch = mock(Fetch.class);
        PropertiesProvider props = key -> "http://localhost";
        when(mockFetch.getOptions()).thenReturn(mock(Fetch.Options.class));
        when(mockFetch.getJson(any(), any(), eq(FreeCurrencyExchangeRateProvider.HistoricalExchangeRateResponse.class)))
            .thenThrow(new FetchException(500, "Server Error"));

        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(mockFetch, props);
        assertThrows(ExternalServiceException.class,
            () -> provider.getRate(Currency.getInstance("USD"), List.of(Currency.getInstance("EUR")), java.time.LocalDate.of(2024, 1, 1)));
    }

    @Test
    void shouldReturnEmptyListWhenHistoricalDateNotInResponse() throws Exception {
        Fetch mockFetch = mock(Fetch.class);
        PropertiesProvider props = key -> "http://localhost";
        when(mockFetch.getOptions()).thenReturn(mock(Fetch.Options.class));

        FreeCurrencyExchangeRateProvider dummy = new FreeCurrencyExchangeRateProvider(null, null);
        var histResp = dummy.new HistoricalExchangeRateResponse();
        histResp.setData(Map.of("2020-01-01", Map.of("USD", BigDecimal.ONE)));
        when(mockFetch.getJson(any(), any(), eq(FreeCurrencyExchangeRateProvider.HistoricalExchangeRateResponse.class))).thenReturn(histResp);

        FreeCurrencyExchangeRateProvider provider = new FreeCurrencyExchangeRateProvider(mockFetch, props);
        assertTrue(provider.getRate(Currency.getInstance("USD"), List.of(Currency.getInstance("EUR")), java.time.LocalDate.of(2024, 1, 1)).isEmpty());
    }
}