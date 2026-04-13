package edu.itba.exchange.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;

import edu.itba.exchange.exceptions.ExternalServiceException;
import edu.itba.exchange.exceptions.FetchException;

@ExtendWith(MockitoExtension.class)
class UnirestFetchTest {
    private static final UnirestFetch.Response SUCCESS = new UnirestFetch.Response("{}", 200);
    private static final UnirestFetch.Response FAILURE = new UnirestFetch.Response("{}", 500);
    private static final UnirestFetch.Response INVALID = new UnirestFetch.Response("  ", 200);

    @Mock
    private HttpResponse<String> http;

    @Mock
    private GetRequest request;

    @Spy
    private UnirestFetch fetch = new UnirestFetch(new GsonJSON());

    @Test
    void testUnirestOptions() {
        final var options = fetch.getOptions();

        final var returned = options.addHeader("key", "value");

        assertThat(returned, is(options));
        assertThat(options.getHeaders(), not(nullValue()));
        assertThat(options.getHeaders().get("key"), is("value"));
    }

    @Test
    void testResponseOk() {
        final var body = "body";
        final var status = 200;

        final var resp = new UnirestFetch.Response(body, status);

        assertThat(resp.body(), is(body));
        assertThat(resp.status(), is(status));
        assertThat(resp.ok(), is(true));
    }

    @Test
    void testResponseErr() {
        final var body = "error";
        final var status = 500;

        final var resp = new UnirestFetch.Response(body, status);

        assertThat(resp.status(), is(status));
        assertThat(resp.ok(), is(false));
    }

    @Test
    void testResponseFromHttpResponse() {
        when(http.getBody()).thenReturn("body");
        when(http.getStatus()).thenReturn(200);

        final var response = new UnirestFetch.Response(http);

        assertThat(response.body(), is("body"));
        assertThat(response.status(), is(200));
        assertThat(response.ok(), is(true));
    }

    @Test
    void testGetJsonNotOk() throws Exception {
        final var url = URI.create("http://localhost").toURL();
        final var opt = fetch.getOptions();
        doReturn(FAILURE).when(fetch).get(eq(url), any());

        assertThrows(FetchException.class, () -> fetch.getJson(url, opt, Map.class));
    }

    @Test
    void testGetJsonBlankBody() throws Exception {
        final var url = URI.create("http://localhost").toURL();
        final var opt = fetch.getOptions();
        doReturn(INVALID).when(fetch).get(eq(url), any());

        assertThrows(FetchException.class, () -> fetch.getJson(url, opt, Map.class));
    }

    @Test
    void testFetchRequestException() throws Exception {
        final var url = URI.create("http://localhost").toURL();

        try (final var unirest = mockStatic(Unirest.class)) {
            unirest.when(() -> Unirest.get(url.toString())).thenReturn(request);
            when(request.headers(anyMap())).thenReturn(request);
            when(request.asString()).thenThrow(new UnirestException("Failed"));

            assertThrows(ExternalServiceException.class, () -> fetch.get(url, fetch.getOptions()));
        }
    }

    @Test
    void testGetMethodSuccess() throws Exception {
        final var url = URI.create("http://localhost").toURL();

        try (final var unirest = mockStatic(Unirest.class)) {
            unirest.when(() -> Unirest.get(url.toString())).thenReturn(request);
            when(request.headers(anyMap())).thenReturn(request);
            when(request.asString()).thenReturn(http);
            when(http.getBody()).thenReturn("ok");
            when(http.getStatus()).thenReturn(200);

            final var response = fetch.get(url, fetch.getOptions());

            assertThat(response.ok(), is(true));
            assertThat(response.body(), is("ok"));
        }
    }
}
