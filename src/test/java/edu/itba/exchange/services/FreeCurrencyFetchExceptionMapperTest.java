package edu.itba.exchange.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import edu.itba.exchange.exceptions.CurrencyException;
import edu.itba.exchange.exceptions.FetchException;
import edu.itba.exchange.exceptions.freecurrency.*;
import edu.itba.exchange.exceptions.freecurrency.validation.InvalidBaseCurrencyException;
import edu.itba.exchange.interfaces.JSON;
import edu.itba.exchange.services.dto.ValidationErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class FreeCurrencyFetchExceptionMapperTest {

    @Mock
    private JSON json;

    @Test
    void shouldTranslate401ToInvalidCredentialsException() {
        var mapper = new FreeCurrencyFetchExceptionMapper(json);
        var fe = new FetchException(401, "Unauthorized");

        CurrencyException result = mapper.translate(fe);

        assertInstanceOf(InvalidCredentialsException.class, result);
    }

    @Test
    void shouldTranslate403ToForbiddenException() {
        var mapper = new FreeCurrencyFetchExceptionMapper(json);
        var fe = new FetchException(403, "Forbidden");

        CurrencyException result = mapper.translate(fe);

        assertInstanceOf(ForbiddenException.class, result);
    }

    @Test
    void shouldTranslate404ToEndpointNotFoundException() {
        var mapper = new FreeCurrencyFetchExceptionMapper(json);
        var fe = new FetchException(404, "Not Found");

        CurrencyException result = mapper.translate(fe);

        assertInstanceOf(EndpointNotFoundException.class, result);
    }

    @Test
    void shouldTranslate422ToValidationErrorException() {
        var mapper = new FreeCurrencyFetchExceptionMapper(json);
        var body = "{\"message\":\"Validation\",\"errors\":{\"base_currency\":[\"invalid\"]}}";
        var fe = new FetchException(422, body);

        var response = new ValidationErrorResponse();
        response.setErrors(Map.of("base_currency", new String[]{"invalid"}));
        when(json.parse(eq(body), any())).thenReturn(response);

        CurrencyException result = mapper.translate(fe);

        assertInstanceOf(InvalidBaseCurrencyException.class, result);
    }

    @Test
    void shouldTranslate429ToRateLimitException() {
        var mapper = new FreeCurrencyFetchExceptionMapper(json);
        var fe = new FetchException(429, "Too Many Requests");

        CurrencyException result = mapper.translate(fe);

        assertInstanceOf(RateLimitException.class, result);
    }

    @Test
    void shouldTranslateUnknownStatusToInternalServerErrorException() {
        var mapper = new FreeCurrencyFetchExceptionMapper(json);
        var fe = new FetchException(500, "Internal Server Error");

        CurrencyException result = mapper.translate(fe);

        assertInstanceOf(InternalServerErrorException.class, result);
    }
}
