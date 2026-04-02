package edu.itba.exchange.interfaces;

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
     */
    <E> E getJson(final URL target, final Options options, Type clazz);

    /**
     * POST an endpoint
     *
     * @param target  The url to fetch
     * @param options The request modifiers
     * @return The response body
     */
    Response post(final URL target, final Options options);

    Options getOptions();

    interface Options {
        Options addHeader(final String key, final String value);

        Map<String, String> getHeaders();
    }

    interface Response {
        String body();

        int status();
    }
}
