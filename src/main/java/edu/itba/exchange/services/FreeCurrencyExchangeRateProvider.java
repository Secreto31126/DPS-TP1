package edu.itba.exchange.services;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

import org.apache.http.NameValuePair;
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
    public List<Currency> getAvailableCurrencies() {
        return getAvailableCurrencies(List.of());
    }

    @Override
    public List<Currency> getAvailableCurrencies(final List<String> currencyCodes) {
        return this.formCurrencyResponse(currencyCodes);
    }

    @Override
    public Rate getRate(final Currency from, final Currency to) {
        return this.getRate(from, List.of(to)).getFirst();
    }

    @Override
    public List<Rate> getRate(final Currency from, final List<Currency> to) {
        return this.formRateResponse(from, to, null);
    }

    @Override
    public List<Rate> getRate(final Currency from, final List<Currency> to, final LocalDate rateDate) {
        try {
            return this.formRateResponse(from, to, rateDate);
        } catch (final Exception e) {
            throw new ExternalServiceException(e.getMessage());
        }
    }

    private List<Rate> formRateResponse(final Currency from, final List<Currency> to, final LocalDate rateDate){
        final var url = this.buildRateUrl(from, to, rateDate);

        final var options = this.getOptions();

        final ExchangeRateResponse response = fetch.getJson(url, options, ExchangeRateResponse.class);
        return response.getData().entrySet().stream()
                .map(entry -> new Rate(from, entry.getKey(), entry.getValue()))
                .toList();
    }



    private List<Currency> formCurrencyResponse(final List<String> currencyCodes){
        try {
            final var url = this.buildCurrenciesUrl(currencyCodes);
            final var options = this.getOptions();

            final ExchangeCurrenciesResponse response = fetch.getJson(url, options, ExchangeCurrenciesResponse.class);

            return response.getData().keySet().stream()
                    .map(Currency::getInstance)
                    .toList();
        } catch (final IllegalArgumentException e) {
            throw new CurrencyNotFoundException(e.getMessage());
        }
    }

    private URL buildCurrenciesUrl(final List<String> currencyCodes){
        NameValuePair currencies = new BasicNameValuePair("currencies", String.join(",", currencyCodes));
        return this.getUrl("/currencies",List.of(currencies));
    }

    private URL buildRateUrl(final Currency from, final List<Currency> to, final LocalDate rateDate){
        final var currencyCodesList = to.stream().map(Currency::getCurrencyCode).toList();
        final var currencies = String.join(",", currencyCodesList);
        return rateDate!=null ? buildHistoricalRateUrl(from,currencies,rateDate) : this.buildLatestRateUrl(from, currencies);
    }

    private URL buildHistoricalRateUrl(final Currency from, final String currencies, final LocalDate rateDate){
        final var path="/v1/historical";
        final List<NameValuePair> queries= List.of(
                new BasicNameValuePair("base_currency", from.getCurrencyCode()),
                new BasicNameValuePair("currencies", currencies),
                new BasicNameValuePair("rate_date", rateDate.toString())
        );
        return this.getUrl(path,queries);
    }
    private URL buildLatestRateUrl(final Currency from, final String currencies){
        final var path="/v1/latest";
        final List<NameValuePair> queries = List.of(new BasicNameValuePair("base_currency", from.getCurrencyCode()), new BasicNameValuePair("currencies", currencies));
        return this.getUrl(path,queries);
    }

    private URL getUrl(final String path, final List<NameValuePair> query) {
        final var filteredQuery =query.stream().filter(pairs -> !pairs.getValue().isBlank()).toList();
        try {
            return new URIBuilder(this.getApiBaseUrl())
                    .setPath(path)
                    .setParameters(filteredQuery)
                    .build().toURL();
        } catch (final MalformedURLException | URISyntaxException e) {
            throw new ExternalServiceException("Internal error building API URL");
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
