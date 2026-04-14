package edu.itba.exchange.services;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import edu.itba.exchange.exceptions.FetchException;
import edu.itba.exchange.interfaces.Fetch;
import edu.itba.exchange.interfaces.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
public class UnirestFetch implements Fetch {
    private final JSON jsonService;

    @Override
    public Fetch.Response get(final URL target, final Options options) throws FetchException {
        return this.fetchRequest(target, options, Unirest::get);
    }



    @Override
    public Fetch.Options getOptions() {
        return new UnirestOptions();
    }

    private Fetch.Response fetchRequest(final URL target, final Options options,
            final Function<String, ? extends HttpRequest> request) throws FetchException {
        try {
            final var response = request.apply(target.toString()).headers(options.getHeaders());
            return new Response(response.asString());

        } catch (UnirestException e) {
            throw new FetchException(e);
        }
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
    @Data
    @AllArgsConstructor
    public class Response implements Fetch.Response {
        private String body;
        private int status;
        protected Response(final HttpResponse<?> response) {
            this(response.getBody().toString(), response.getStatus());
        }

        public boolean ok() {
            return this.status / 100 == 2;
        }

        public <E> E json(Type type) {
            return jsonService.parse(this.body, type);
        }
    }
}
