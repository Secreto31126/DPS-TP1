package edu.itba.exchange.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ApiErrorTest {

    @Test
    void shouldCreateNetworkError() {
        ApiError error = ApiError.networkError("Connection failed");
        assertEquals(ApiErrorCategory.NETWORK_ERROR, error.category());
        assertEquals("Connection failed", error.message());
    }

    @Test
    void shouldMapClientErrorClass() {
        ApiError error = ApiError.fromHttpStatus(404, "Not Found");
        assertEquals(ApiErrorCategory.CLIENT_ERROR, error.category());
        assertTrue(error.message().contains("404"));
    }

    @Test
    void shouldMapServerErrorClass() {
        ApiError error = ApiError.fromHttpStatus(500, "Internal Server Error");
        assertEquals(ApiErrorCategory.SERVER_ERROR, error.category());
        assertTrue(error.message().contains("500"));
    }

    @Test
    void shouldMapUnknownErrorClass() {
        ApiError error = ApiError.fromHttpStatus(302, "Redirect");
        assertEquals(ApiErrorCategory.UNKNOWN_ERROR, error.category());
        assertTrue(error.message().contains("302"));
    }
}