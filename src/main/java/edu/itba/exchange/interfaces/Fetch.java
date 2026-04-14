package edu.itba.exchange.interfaces;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.Map;

import edu.itba.exchange.exceptions.FetchException;

public interface Fetch {
    /**
     * Fetch an endpoint
     *
     * @param target  The url to fetch
     * @param options The request modifiers
     * @return The response body
     */
    Response get(final URL target, final Options options) throws FetchException;

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
        String getBody();

        /**
         * The response status number
         *
         * @return HTTP response status
         */
        int getStatus();

        /**
         * Checks the return status
         *
         * @return True if the status code is 2XX
         */
        boolean ok();

        /**
         * Read JSON body
         *
         * @param <E>   The response json
         * @param clazz The json structure
         * @return The json response parsed
         * @throws IllegalArgumentException on invalid input or type
         */
        <E> E json(Type clazz);
    }
}
