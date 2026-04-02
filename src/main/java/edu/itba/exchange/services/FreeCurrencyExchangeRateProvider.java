package edu.itba.exchange.services;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Currency;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;

import edu.itba.exchange.interfaces.ExchangeRateProvider;
import edu.itba.exchange.interfaces.Fetch;
import edu.itba.exchange.models.Rate;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public class FreeCurrencyExchangeRateProvider implements ExchangeRateProvider {
    private final Fetch fetch;

    private static final String API_BASE_URL = "https://api.freecurrencyapi.com";
    // This shouldn't be published (store in an application.properties)
    private static final String API_TOKEN = "fca_live_tMQ4oYRmk8T587mrTdOFbTREYXjqCLRkXwJUS4C6";

    @Override
    public Rate getRate(final Currency from, final Currency to) {
        final var url = this.getUrl(from, to);
        final var options = this.getOptions();
        final var response = (ExchangeRateResponse) fetch.getJson(url, options, ExchangeRateResponse.class);
        return new Rate(from, to, response.getData().get(to.getCurrencyCode()));
    }

    private URL getUrl(final Currency from, final Currency to) {
        try {
            return new URIBuilder(FreeCurrencyExchangeRateProvider.API_BASE_URL)
                    .setPath("/v1/latest")
                    .addParameter("base_currency", from.getCurrencyCode())
                    .addParameter("currencies", to.getCurrencyCode())
                    .build().toURL();
        } catch (final URISyntaxException | MalformedURLException e) {
            throw new RuntimeException("Internal error building API URL");
        }
    }

    private Fetch.Options getOptions() {
        return fetch.getOptions().addHeader("apikey", FreeCurrencyExchangeRateProvider.API_TOKEN);
    }

    @Data
    public class ExchangeRateResponse {
        private Map<String, BigDecimal> data;
    }
}
