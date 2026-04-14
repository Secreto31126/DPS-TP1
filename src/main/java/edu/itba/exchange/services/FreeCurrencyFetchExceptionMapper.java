package edu.itba.exchange.services;

import edu.itba.exchange.interfaces.Fetch;
import edu.itba.exchange.interfaces.HttpStatus;

import java.util.Map;
import java.util.function.Function;

import edu.itba.exchange.exceptions.CurrencyException;
import edu.itba.exchange.exceptions.FetchException;
import edu.itba.exchange.exceptions.freecurrency.*;
import edu.itba.exchange.interfaces.FetchExceptionMapper;
import edu.itba.exchange.interfaces.JSON;
import edu.itba.exchange.services.dto.ValidationErrorResponse;
import lombok.AllArgsConstructor;

public class FreeCurrencyFetchExceptionMapper implements FetchExceptionMapper<CurrencyException> {

    private final Map<Integer, Function<Fetch.Response, CurrencyException>> mapper = Map.of(
            HttpStatus.UNAUTHORIZED, _ -> new InvalidCredentialsException(),
            HttpStatus.FORBIDDEN, _ -> new ForbiddenException(),
            HttpStatus.NOT_FOUND, _ -> new EndpointNotFoundException(),
            HttpStatus.UNPROCESSABLE_ENTITY, this::unprocessableExceptionMapper,
            HttpStatus.TOO_MANY_REQUESTS, _ -> new RateLimitException());

    @Override
    public CurrencyException translate(Fetch.Response response) {
        final var status = response.getStatus();
        return mapper.getOrDefault(status, _ -> new InternalServerErrorException()).apply(response);
    }

    private CurrencyException unprocessableExceptionMapper(final Fetch.Response response) {
        final ValidationErrorResponse parsed = response.json(ValidationErrorResponse.class);
        final var errors = parsed != null ? parsed.getErrors() : null;
        return ValidationErrorException.fromErrors(errors);
    }
}
