package edu.itba.exchange;

import edu.itba.exchange.exceptions.CurrencyException;
import edu.itba.exchange.exceptions.CurrencyNotFoundException;
import edu.itba.exchange.exceptions.ExternalServiceException;
import edu.itba.exchange.exceptions.FetchException;

public class ExceptionTranslationMapper {
    private final static int UNSUPPORTED_CURRENCY_STATUS_CODE = 422;

    public static CurrencyException translate(FetchException e) {
        final ApiError apiError = ApiError.fromHttpStatus(e.getStatus(), e.getMessage());
        if (e.getStatus() == UNSUPPORTED_CURRENCY_STATUS_CODE) {
            return new CurrencyNotFoundException(apiError, e);
        }
        return new ExternalServiceException(apiError, e);
    }
}
