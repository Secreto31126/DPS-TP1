package edu.itba.exchange.exceptions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ApiErrorTest {

    @Test
    void shouldCreateNetworkError() {
        // Given
        final var message = "Connection failed";

        // When
        final var error = ApiError.networkError(message);

        // Then
        assertThat(error.category(), is(ApiErrorCategory.NETWORK_ERROR));
        assertThat(error.message(), is(message));
    }

    @ParameterizedTest
    @CsvSource({
            "400, CLIENT_ERROR",
            "404, CLIENT_ERROR",
            "499, CLIENT_ERROR",
            "500, SERVER_ERROR",
            "503, SERVER_ERROR",
            "599, SERVER_ERROR",
            "199, UNKNOWN_ERROR",
            "200, UNKNOWN_ERROR",
            "302, UNKNOWN_ERROR"
    })
    void shouldClassifyStatusByClass(int status, ApiErrorCategory expected) {
        // When
        final var error = ApiError.fromHttpStatus(status, "msg");

        // Then
        assertThat(error.category(), is(expected));
        assertThat(error.message(), containsString(String.valueOf(status)));
    }

    @Test
    void shouldCreateInvalidResponseError() {
        // When
        final var error = ApiError.invalidResponseError();

        // Then
        assertThat(error.category(), is(ApiErrorCategory.INVALID_RESPONSE_ERROR));
        assertThat(error.message(), containsString("broken"));
    }
}
