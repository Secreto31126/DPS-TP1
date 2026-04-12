package edu.itba.exchange.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import edu.itba.exchange.exceptions.ExternalServiceException;
import edu.itba.exchange.interfaces.Fetch;
import java.net.URL;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class UnirestFetchTest {

    @Test
    void testUnirestOptions() {
        UnirestFetch fetch = new UnirestFetch(new GsonJSON());
        UnirestFetch.UnirestOptions options = (UnirestFetch.UnirestOptions) fetch.getOptions();
        Fetch.Options returned = options.addHeader("key", "value");
        assertSame(options, returned);
        assertNotNull(options.getHeaders());
        assertEquals("value", options.getHeaders().get("key"));
    }

    @Test
    void testResponseRecord() {
        UnirestFetch.Response resp = new UnirestFetch.Response("body", 200);
        assertEquals("body", resp.body());
        assertEquals(200, resp.status());
        assertTrue(resp.ok());
    }

    @Test
    void testResponseConstructor() {
        HttpResponse<String> mockHttpResponse = mock(HttpResponse.class);
        when(mockHttpResponse.getBody()).thenReturn("body");
        when(mockHttpResponse.getStatus()).thenReturn(200);

        UnirestFetch.Response response = new UnirestFetch.Response(mockHttpResponse);
        assertEquals("body", response.body());
        assertEquals(200, response.status());
        assertTrue(response.ok());
    }

    @Test
    void testResponseNotOk() {
        UnirestFetch.Response response = new UnirestFetch.Response("error", 500);
        assertFalse(response.ok());
    }

    @Test
    void testGetJsonFailure() throws Exception {
        UnirestFetch spyFetch = spy(new UnirestFetch(new GsonJSON()));
        Fetch.Response mockResponse = mock(Fetch.Response.class);
        
        when(mockResponse.ok()).thenReturn(false);
        when(mockResponse.status()).thenReturn(500);
        when(mockResponse.body()).thenReturn("Error");
        
        doReturn(mockResponse).when(spyFetch).get(any(), any());
        
        assertThrows(edu.itba.exchange.exceptions.FetchException.class, 
            () -> spyFetch.getJson(new URL("http://localhost"), mock(Fetch.Options.class), String.class));
    }

    @Test
    void testGetJsonBlankBody() throws Exception {
        UnirestFetch spyFetch = spy(new UnirestFetch(new GsonJSON()));
        Fetch.Response mockResponse = mock(Fetch.Response.class);
        
        when(mockResponse.ok()).thenReturn(true);
        when(mockResponse.body()).thenReturn("   "); // Blank body
        
        doReturn(mockResponse).when(spyFetch).get(any(), any());
        
        assertThrows(edu.itba.exchange.exceptions.FetchException.class, 
            () -> spyFetch.getJson(new URL("http://localhost"), mock(Fetch.Options.class), String.class));
    }

    @Test
    void testFetchRequestException() throws Exception {
        UnirestFetch fetch = new UnirestFetch(new GsonJSON());
        URL target = new URL("http://localhost");
        
        try (MockedStatic<Unirest> mockedUnirest = mockStatic(Unirest.class)) {
            GetRequest mockRequest = mock(GetRequest.class);
            mockedUnirest.when(() -> Unirest.get(target.toString())).thenReturn(mockRequest);
            when(mockRequest.headers(anyMap())).thenReturn(mockRequest);
            when(mockRequest.asString()).thenThrow(new UnirestException("Failed"));
            
            assertThrows(ExternalServiceException.class, () -> fetch.get(target, fetch.getOptions()));
        }
    }

    @Test
    void testPostMethodException() throws Exception {
        UnirestFetch fetch = new UnirestFetch(new GsonJSON());
        URL target = new URL("http://localhost");

        try (MockedStatic<Unirest> mockedUnirest = mockStatic(Unirest.class)) {
            HttpRequestWithBody mockRequest = mock(HttpRequestWithBody.class);
            mockedUnirest.when(() -> Unirest.post(target.toString())).thenReturn(mockRequest);
            when(mockRequest.headers(anyMap())).thenReturn(mockRequest);
            when(mockRequest.asString()).thenThrow(new UnirestException("Failed"));

            assertThrows(ExternalServiceException.class, () -> fetch.post(target, fetch.getOptions()));
        }
    }

    @Test
    void testPostMethodSuccess() throws Exception {
        UnirestFetch fetch = new UnirestFetch(new GsonJSON());
        URL target = new URL("http://localhost");

        try (MockedStatic<Unirest> mockedUnirest = mockStatic(Unirest.class)) {
            HttpRequestWithBody mockRequest = mock(HttpRequestWithBody.class);
            HttpResponse<String> mockHttpResponse = mock(HttpResponse.class);
            mockedUnirest.when(() -> Unirest.post(target.toString())).thenReturn(mockRequest);
            when(mockRequest.headers(anyMap())).thenReturn(mockRequest);
            when(mockRequest.asString()).thenReturn(mockHttpResponse);
            when(mockHttpResponse.getBody()).thenReturn("ok");
            when(mockHttpResponse.getStatus()).thenReturn(200);

            Fetch.Response response = fetch.post(target, fetch.getOptions());
            assertTrue(response.ok());
            assertEquals("ok", response.body());
        }
    }

}