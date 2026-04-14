//package edu.itba.exchange.services;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.instanceOf;
//import static org.hamcrest.Matchers.is;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//
//import java.util.Map;
//
//import edu.itba.exchange.exceptions.CurrencyException;
//import edu.itba.exchange.exceptions.FetchException;
//import edu.itba.exchange.exceptions.freecurrency.*;
//import edu.itba.exchange.exceptions.freecurrency.validation.InvalidBaseCurrencyException;
//import edu.itba.exchange.exceptions.freecurrency.validation.InvalidCurrenciesException;
//import edu.itba.exchange.exceptions.freecurrency.validation.InvalidDateException;
//import edu.itba.exchange.interfaces.JSON;
//import edu.itba.exchange.services.dto.ValidationErrorResponse;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//class FreeCurrencyFetchExceptionMapperTest {
//
//    @Mock
//    private JSON json;
//
//    // --- Status code mapping ---
//
//    @Test
//    void shouldTranslate401ToInvalidCredentialsException() {
//        // Given
//        final var mapper = new FreeCurrencyFetchExceptionMapper(json);
//        final var fe = new FetchException(401, "Unauthorized");
//
//        // When
//        final var result = mapper.translate(fe);
//
//        // Then
//        assertThat(result, is(instanceOf(InvalidCredentialsException.class)));
//    }
//
//    @Test
//    void shouldTranslate403ToForbiddenException() {
//        // Given
//        final var mapper = new FreeCurrencyFetchExceptionMapper(json);
//        final var fe = new FetchException(403, "Forbidden");
//
//        // When
//        final var result = mapper.translate(fe);
//
//        // Then
//        assertThat(result, is(instanceOf(ForbiddenException.class)));
//    }
//
//    @Test
//    void shouldTranslate404ToEndpointNotFoundException() {
//        // Given
//        final var mapper = new FreeCurrencyFetchExceptionMapper(json);
//        final var fe = new FetchException(404, "Not Found");
//
//        // When
//        final var result = mapper.translate(fe);
//
//        // Then
//        assertThat(result, is(instanceOf(EndpointNotFoundException.class)));
//    }
//
//    @Test
//    void shouldTranslate429ToRateLimitException() {
//        // Given
//        final var mapper = new FreeCurrencyFetchExceptionMapper(json);
//        final var fe = new FetchException(429, "Too Many Requests");
//
//        // When
//        final var result = mapper.translate(fe);
//
//        // Then
//        assertThat(result, is(instanceOf(RateLimitException.class)));
//    }
//
//    @Test
//    void shouldTranslate500ToInternalServerErrorException() {
//        // Given
//        final var mapper = new FreeCurrencyFetchExceptionMapper(json);
//        final var fe = new FetchException(500, "Internal Server Error");
//
//        // When
//        final var result = mapper.translate(fe);
//
//        // Then
//        assertThat(result, is(instanceOf(InternalServerErrorException.class)));
//    }
//
//    @Test
//    void shouldTranslateUnknownStatusToInternalServerErrorException() {
//        // Given
//        final var mapper = new FreeCurrencyFetchExceptionMapper(json);
//        final var fe = new FetchException(503, "Service Unavailable");
//
//        // When
//        final var result = mapper.translate(fe);
//
//        // Then
//        assertThat(result, is(instanceOf(InternalServerErrorException.class)));
//    }
//
//    // --- 422 validation error sub-cases ---
//
//    @Test
//    void shouldTranslate422WithBaseCurrencyError() {
//        // Given
//        final var mapper = new FreeCurrencyFetchExceptionMapper(json);
//        final var body = "{\"errors\":{\"base_currency\":[\"invalid\"]}}";
//        final var fe = new FetchException(422, body);
//
//        final var response = new ValidationErrorResponse();
//        response.setErrors(Map.of("base_currency", new String[]{"invalid"}));
//        when(json.parse(eq(body), any())).thenReturn(response);
//
//        // When
//        final CurrencyException result = mapper.translate(fe);
//
//        // Then
//        assertThat(result, is(instanceOf(InvalidBaseCurrencyException.class)));
//    }
//
//    @Test
//    void shouldTranslate422WithCurrenciesError() {
//        // Given
//        final var mapper = new FreeCurrencyFetchExceptionMapper(json);
//        final var body = "{\"errors\":{\"currencies\":[\"invalid\"]}}";
//        final var fe = new FetchException(422, body);
//
//        final var response = new ValidationErrorResponse();
//        response.setErrors(Map.of("currencies", new String[]{"invalid"}));
//        when(json.parse(eq(body), any())).thenReturn(response);
//
//        // When
//        final CurrencyException result = mapper.translate(fe);
//
//        // Then
//        assertThat(result, is(instanceOf(InvalidCurrenciesException.class)));
//    }
//
//    @Test
//    void shouldTranslate422WithDateError() {
//        // Given
//        final var mapper = new FreeCurrencyFetchExceptionMapper(json);
//        final var body = "{\"errors\":{\"date\":[\"invalid\"]}}";
//        final var fe = new FetchException(422, body);
//
//        final var response = new ValidationErrorResponse();
//        response.setErrors(Map.of("date", new String[]{"invalid"}));
//        when(json.parse(eq(body), any())).thenReturn(response);
//
//        // When
//        final CurrencyException result = mapper.translate(fe);
//
//        // Then
//        assertThat(result, is(instanceOf(InvalidDateException.class)));
//    }
//
//    @Test
//    void shouldTranslate422WithUnknownErrorKey() {
//        // Given
//        final var mapper = new FreeCurrencyFetchExceptionMapper(json);
//        final var body = "{\"errors\":{\"unknown\":[\"error\"]}}";
//        final var fe = new FetchException(422, body);
//
//        final var response = new ValidationErrorResponse();
//        response.setErrors(Map.of("unknown", new String[]{"error"}));
//        when(json.parse(eq(body), any())).thenReturn(response);
//
//        // When
//        final CurrencyException result = mapper.translate(fe);
//
//        // Then
//        assertThat(result.getClass(), is(ValidationErrorException.class));
//    }
//
//
//
//
//}
