package edu.itba.exchange.services;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import edu.itba.exchange.interfaces.Fetch;
import edu.itba.exchange.interfaces.JSON;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class UnirestFetch implements Fetch {
    private final JSON json;

    @Override
    public Fetch.Response get(final URL target, final Options options) {
        try {
            final var response = Unirest.get(target.toString()).headers(options.getHeaders()).asJson();
            return new Response(response);
        } catch (UnirestException e) {
            throw new RuntimeException("Failed to get");
        }
    }

    @Override
    public <E> E getJson(URL target, Options options, Type clazz) {
        options.addHeader("Accept", "application/json");
        final var response = this.get(target, options);

        if (response.status() / 100 != 2 || response.body().isBlank()) {
            throw new IllegalStateException("Failed to fetch JSON");
        }

        return this.json.parse(response.body(), clazz);
    }

    @Override
    public Fetch.Response post(final URL target, final Options options) {
        try {
            final var response = Unirest.post(target.toString()).headers(options.getHeaders()).asJson();
            return new Response(response);
        } catch (UnirestException e) {
            throw new RuntimeException("Failed to post");
        }
    }

    @Override
    public Fetch.Options getOptions() {
        return new UnirestOptions();
    }

    @Getter
    public class UnirestOptions implements Fetch.Options {
        private final Map<String, String> headers = new HashMap<>();

        @Override
        public Options addHeader(String key, String value) {
            headers.put(key, value);
            return this;
        }
    }

    public record Response(String body, int status) implements Fetch.Response {
        protected Response(final HttpResponse<?> response) {
            this(response.getBody().toString(), response.getStatus());
        }
    }
}
