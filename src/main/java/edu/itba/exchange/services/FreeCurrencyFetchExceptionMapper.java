package edu.itba.exchange.services;

import edu.itba.exchange.interfaces.HttpStatus;
import edu.itba.exchange.exceptions.CurrencyException;
import edu.itba.exchange.exceptions.FetchException;
import edu.itba.exchange.exceptions.freecurrency.*;
import edu.itba.exchange.interfaces.FetchExceptionMapper;
import edu.itba.exchange.interfaces.JSON;
import edu.itba.exchange.services.dto.ValidationErrorResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FreeCurrencyFetchExceptionMapper implements FetchExceptionMapper<CurrencyException> {
    private final JSON json;

    @Override
    public CurrencyException translate(FetchException e) {
        var status = e.getStatus();
        return switch (status) {
            case HttpStatus.UNAUTHORIZED: yield new InvalidCredentialsException();
            case HttpStatus.FORBIDDEN: yield new ForbiddenException();
            case HttpStatus.NOT_FOUND: yield new EndpointNotFoundException();
            case HttpStatus.UNPROCESSABLE_ENTITY: {
                final var body = e.getBody();
                final ValidationErrorResponse parsed = this.json.parse(body, ValidationErrorResponse.class);
                final var errors = parsed != null ? parsed.getErrors() : null;
                yield ValidationErrorException.fromErrors(errors);
            }
            case HttpStatus.TOO_MANY_REQUESTS: yield new RateLimitException();
            default: yield new InternalServerErrorException();
        };
    }
}
