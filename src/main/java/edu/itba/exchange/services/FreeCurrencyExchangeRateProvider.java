package edu.itba.exchange.services;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import edu.itba.exchange.exceptions.CurrencyNotFoundException;
import edu.itba.exchange.exceptions.ExternalServiceException;
import edu.itba.exchange.interfaces.ExchangeRateProvider;
import edu.itba.exchange.interfaces.Fetch;
import edu.itba.exchange.interfaces.PropertiesProvider;
import edu.itba.exchange.models.Rate;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public class FreeCurrencyExchangeRateProvider implements ExchangeRateProvider {
    private final Fetch fetch;
    private final PropertiesProvider propertiesProvider;

    @Override
    public Rate getRate(final Currency from, final Currency to) {
        return this.getRate(from, List.of(to)).getFirst();
    }

    @Override
    public List<Rate> getRate(final Currency from, final List<Currency> to) {
        final var url = this.getLatestRatesUrl(from, to);
        final var options = this.getOptions();
        final ExchangeRateResponse response = fetch.getJson(url, options, ExchangeRateResponse.class);
        final var ratesBySymbol = response.getData();

        return ratesBySymbol
                .entrySet()
                .stream()
                .map(entry -> 
                    new Rate(
                        from,
                        Currency.getInstance(entry.getKey()),
                        entry.getValue().toString()
                    )
                )
                .toList();
    }

    @Override
    public List<Rate> getRate(final Currency from, final List<Currency> to, final LocalDate rateDate) {
        // TODO: Todo
        return null;
    }

    @Override
    public List<Currency> getAvailableCurrencies() {
        return getAvailableCurrencies(List.of());
    }

    @Override
    public List<Currency> getAvailableCurrencies(final List<String> currencyCodes) {
        try {
            final var currencies = new BasicNameValuePair("currencies", String.join(",", currencyCodes));

            final var url = this.getUrl("/currencies", currencies);
            final var options = this.getOptions();

            final ExchangeCurrenciesResponse response = fetch.getJson(url, options, ExchangeCurrenciesResponse.class);

            return response.getData().keySet().stream()
                    .map(Currency::getInstance)
                    .toList();
        } catch (final IllegalArgumentException e) {
            throw new CurrencyNotFoundException(e.getMessage());
        }
    }

    private URL getLatestRatesUrl(final Currency from, final List<Currency> to) {
        final var currencyCodesList = to.stream().map(Currency::getCurrencyCode).toList();
        final var currencies = String.join(",", currencyCodesList);

        return this.getUrl("/v1/latest",
                new BasicNameValuePair("base_currency", from.getCurrencyCode()),
                new BasicNameValuePair("currencies", currencies));
    }

    private URL getUrl(final String path, final BasicNameValuePair... query) {
        try {
            return this.getApiUriBuilder(path)
                    .setParameters(query)
                    .build().toURL();
        } catch (final MalformedURLException | URISyntaxException e) {
            throw new ExternalServiceException("Internal error building API URL");
        }
    }

    private URIBuilder getApiUriBuilder(final String path) {
        try {
            return new URIBuilder(this.getApiBaseUrl()).setPath(path);
        } catch (final URISyntaxException e) {
            throw new RuntimeException("Internal error building API URL");
        }
    }

    private String getApiBaseUrl() {
        return this.propertiesProvider.get("FREE_CURRENCY_EXCHANGE_API_BASE_URL");
    }

    private String getApiToken() {
        return this.propertiesProvider.get("FREE_CURRENCY_EXCHANGE_API_TOKEN");
    }

    private Fetch.Options getOptions() {
        return this.fetch.getOptions().addHeader("apikey", this.getApiToken());
    }

    @Data
    public class ExchangeRateResponse {
        private Map<String, BigDecimal> data;
    }

    @Data
    public class ExchangeCurrenciesResponse {
        private Map<String, ExchangeCurrencyData> data;

        public record ExchangeCurrencyData(String symbol, String name, String symbol_native, long decimal_digits,
                long rounding, String code, String name_plural) {
            public String symbolNative() {
                return this.symbol_native;
            }

            public long decimalDigits() {
                return this.decimal_digits;
            }

            public String namePlural() {
                return this.name_plural;
            }
        }
    }
}
