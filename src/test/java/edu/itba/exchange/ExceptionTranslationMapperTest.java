package edu.itba.exchange;

import static org.junit.jupiter.api.Assertions.*;

import edu.itba.exchange.exceptions.CurrencyException;
import edu.itba.exchange.exceptions.CurrencyNotFoundException;
import edu.itba.exchange.exceptions.ExternalServiceException;
import edu.itba.exchange.exceptions.FetchException;
import org.junit.jupiter.api.Test;

class ExceptionTranslationMapperTest {

    @Test
    void shouldTranslate422ToCurrencyNotFoundException() {
        FetchException fe = new FetchException(422, "Unsupported currency");
        CurrencyException translated = ExceptionTranslationMapper.translate(fe);

        assertInstanceOf(CurrencyNotFoundException.class, translated);
        assertEquals(ApiErrorCategory.CLIENT_ERROR, translated.getApiError().category());
        assertEquals(fe, translated.getCause());
    }

    @Test
    void shouldTranslateGeneric4xxToExternalServiceException() {
        FetchException fe = new FetchException(400, "Bad Request");
        CurrencyException translated = ExceptionTranslationMapper.translate(fe);

        assertInstanceOf(ExternalServiceException.class, translated);
        assertEquals(ApiErrorCategory.CLIENT_ERROR, translated.getApiError().category());
        assertEquals(fe, translated.getCause());
    }

    @Test
    void shouldTranslate5xxToExternalServiceException() {
        FetchException fe = new FetchException(503, "Service Unavailable");
        CurrencyException translated = ExceptionTranslationMapper.translate(fe);

        assertInstanceOf(ExternalServiceException.class, translated);
        assertEquals(ApiErrorCategory.SERVER_ERROR, translated.getApiError().category());
        assertEquals(fe, translated.getCause());
    }

    @Test
    void shouldTranslateUnknownStatusToExternalServiceException() {
        FetchException fe = new FetchException(300, "Redirect");
        CurrencyException translated = ExceptionTranslationMapper.translate(fe);

        assertInstanceOf(ExternalServiceException.class, translated);
        assertEquals(ApiErrorCategory.UNKNOWN_ERROR, translated.getApiError().category());
        assertEquals(fe, translated.getCause());
    }
}