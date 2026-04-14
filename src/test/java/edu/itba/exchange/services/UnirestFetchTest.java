package edu.itba.exchange.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;

import edu.itba.exchange.exceptions.FetchException;
import edu.itba.exchange.interfaces.JSON;

@ExtendWith(MockitoExtension.class)
class UnirestFetchTest {

    @Mock
    private JSON json;

    @Mock
    private HttpResponse<String> http;

    @Mock
    private GetRequest request;

    private UnirestFetch fetch;

    @BeforeEach
    void setUp() {
        fetch = spy(new UnirestFetch(json));
    }

    @Test
    void shouldReportOkForSuccessStatus() {
        // Given
        final UnirestFetch.Response response = fetch.new Response("body", 200);

        // When / Then
        assertThat(response.ok(), is(true));
    }

    @Test
    void shouldReportNotOkForErrorStatus() {
        // Given
        final var response = fetch.new Response("error", 500);

        // When / Then
        assertThat(response.ok(), is(false));
    }

    @Test
    void shouldReportOkForAllTwoHundredRange() {
        // Given
        final var response = fetch.new Response("body", 299);

        // When / Then
        assertThat(response.ok(), is(true));
    }

    @Test
    void shouldReportNotOkForBorderStatus() {
        // Given
        final var response = fetch.new Response("body", 300);

        // When / Then
        assertThat(response.ok(), is(false));
    }

    @Test
    void shouldCreateResponseFromHttpResponse() {
        // Given
        when(http.getBody()).thenReturn("payload");
        when(http.getStatus()).thenReturn(201);

        // When
        final var response = fetch.new Response(http);

        // Then
        assertThat(response.getBody(), is("payload"));
        assertThat(response.getStatus(), is(201));
        assertThat(response.ok(), is(true));
    }

    @Test
    void shouldReturnSelfOnAddHeader() {
        // Given
        final var options = fetch.getOptions();

        // When
        final var returned = options.addHeader("X-Key", "value");

        // Then
        assertThat(returned, is(sameInstance(options)));
    }

    @Test
    void shouldStoreHeaders() {
        // Given
        final var options = fetch.getOptions();

        // When
        options.addHeader("Authorization", "Bearer token");

        // Then
        assertThat(options.getHeaders().get("Authorization"), is("Bearer token"));
    }

    @Test
    void shouldReturnResponseOnSuccessfulGet() throws Exception {
        // Given
        final var url = URI.create("http://localhost").toURL();

        try (var unirest = mockStatic(Unirest.class)) {
            unirest.when(() -> Unirest.get(url.toString())).thenReturn(request);
            when(request.headers(anyMap())).thenReturn(request);
            when(request.asString()).thenReturn(http);
            when(http.getBody()).thenReturn("ok");
            when(http.getStatus()).thenReturn(200);

            // When
            final var response = fetch.get(url, fetch.getOptions());

            // Then
            assertThat(response.ok(), is(true));
            assertThat(response.getBody(), is("ok"));
        }
    }

    @Test
    void shouldThrowExternalServiceExceptionOnUnirestFailure() throws Exception {
        // Given
        final var url = URI.create("http://localhost").toURL();

        try (var unirest = mockStatic(Unirest.class)) {
            unirest.when(() -> Unirest.get(url.toString())).thenReturn(request);
            when(request.headers(anyMap())).thenReturn(request);
            when(request.asString()).thenThrow(new UnirestException("network error"));

            // When / Then
            assertThrows(FetchException.class, () -> fetch.get(url, fetch.getOptions()));
        }
    }
}
