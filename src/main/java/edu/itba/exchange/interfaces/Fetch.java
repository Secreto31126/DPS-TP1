package edu.itba.exchange.interfaces;

import edu.itba.exchange.exceptions.FetchException;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.Map;

public interface Fetch {
    /**
     * Fetch an endpoint
     *
     * @param target  The url to fetch
     * @param options The request modifiers
     * @return The response body
     */
    Response get(final URL target, final Options options);

    /**
     * Fetch JSON from an endpoint
     *
     * @param <E>     The response json
     * @param target  The url to fetch
     * @param options The request modifiers ("Accept: application/json" is added)
     * @param clazz   The json structure
     * @return The json response parsed
     * @throws FetchException on 4NN and 5NN status code responses
     */
    <E> E getJson(final URL target, final Options options, Type clazz) throws FetchException;

    /**
     * POST an endpoint
     *
     * @param target  The url to fetch
     * @param options The request modifiers
     * @return The response body
     */
    Response post(final URL target, final Options options);

    /**
     * Provides an options object
     *
     * @apiNote I don't know how to feel about this
     * @return The options 'builder' (?)
     */
    Options getOptions();

    /**
     * HTTP options object
     */
    interface Options {
        /**
         * Append an HTTP header
         *
         * @apiNote If the headers exists, it overwrites them
         * @param key   The header key
         * @param value The header value
         * @return this, for chaining
         */
        Options addHeader(final String key, final String value);

        /**
         * Get the headers
         *
         * @return Map of headers key to value
         */
        Map<String, String> getHeaders();
    }

    /**
     * HTTP response object
     */
    interface Response {
        /**
         * The response text
         *
         * @return HTTP response body
         */
        String body();

        /**
         * The response status number
         *
         * @return HTTP response status
         */
        int status();

        /**
         * Checks the return status
         *
         * @return True if the status code is 2XX
         */
        boolean ok();
    }
}
