package edu.itba.exchange.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import edu.itba.exchange.ApiError;
import org.junit.jupiter.api.Test;

class ExceptionCoverageTest {
    private static final ApiError ERROR = ApiError.networkError("error");

    @Test
    void testCurrencyExceptionThroughSubclass() {
        var ex = new CurrencyNotFoundException(ERROR);
        assertEquals(ERROR, ex.getApiError());
        assertEquals(ERROR.message(), ex.getMessage());
        
        var cause = new RuntimeException();
        var exWithCause = new CurrencyNotFoundException(ERROR, cause);
        assertEquals(cause, exWithCause.getCause());
    }

    @Test
    void testExternalServiceException() {
        var cause = new RuntimeException();
        var ex = new ExternalServiceException(ERROR, cause);
        assertEquals(ERROR, ex.getApiError());
        assertEquals(ERROR.message(), ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
    
    @Test
    void testFetchException() {
        var ex = new FetchException(500, "message");
        assertEquals(500, ex.getStatus());
        assertEquals("message", ex.getMessage());
    }
}