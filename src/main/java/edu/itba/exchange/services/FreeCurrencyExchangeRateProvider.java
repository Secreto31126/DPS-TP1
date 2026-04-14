package edu.itba.exchange.services;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

import edu.itba.exchange.exceptions.ApiError;
import edu.itba.exchange.exceptions.ApiErrorCategory;
import edu.itba.exchange.exceptions.CurrencyException;
import edu.itba.exchange.exceptions.CurrencyNotFoundException;
import edu.itba.exchange.exceptions.ExternalServiceException;
import edu.itba.exchange.exceptions.FetchException;
import edu.itba.exchange.interfaces.ExchangeRateProvider;
import edu.itba.exchange.interfaces.FetchExceptionMapper;
import edu.itba.exchange.interfaces.JSON;
import edu.itba.exchange.services.dto.ExchangeCurrenciesResponse;
import edu.itba.exchange.services.dto.ExchangeRateResponse;
import edu.itba.exchange.services.dto.HistoricalExchangeRateResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import edu.itba.exchange.interfaces.Fetch;
import edu.itba.exchange.interfaces.PropertiesProvider;
import edu.itba.exchange.models.Rate;

public class FreeCurrencyExchangeRateProvider implements ExchangeRateProvider {
    private final Fetch fetch;
    private final PropertiesProvider props;
    private final FreeCurrencyFetchExceptionMapper mapper;

    public FreeCurrencyExchangeRateProvider(final Fetch fetch, final PropertiesProvider props, final JSON json) {
        this.fetch = fetch;
        this.props = props;
        this.mapper = new FreeCurrencyFetchExceptionMapper(json);
    }

    @Override
    public List<Currency> getAvailableCurrencies() {
        return this.getAvailableCurrencies(List.of());
    }

    @Override
    public List<Currency> getAvailableCurrencies(final List<String> currencyCodes) {
        final var url = this.buildCurrenciesUrl(currencyCodes);
        final ExchangeCurrenciesResponse response = this.fetchApi(url, ExchangeCurrenciesResponse.class);

        return response.getData().keySet().stream()
                .map(Currency::getInstance)
                .toList();
    }

    @Override
    public Rate getRate(final Currency from, final Currency to) {
        return this.getRate(from, List.of(to)).getFirst();
    }

    @Override
    public List<Rate> getRate(final Currency from, final List<Currency> to) {
        final var url = this.buildRateUrl(from, to);
        final ExchangeRateResponse response = this.fetchApi(url, ExchangeRateResponse.class);

        return response.getData().entrySet().stream()
                .map(entry -> new Rate(from, entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public List<Rate> getRate(final Currency from, final List<Currency> to, final LocalDate date) {
        final var url = this.buildHistoricalRateUrl(from, to, date);
        final HistoricalExchangeRateResponse response = this.fetchApi(url, HistoricalExchangeRateResponse.class);

        if (!response.getData().containsKey(date)) {
            throw new CurrencyNotFoundException(ApiError.invalidResponseError());
        }

        return response.getData().get(date).entrySet().stream()
                .map(entry -> new Rate(from, entry.getKey(), entry.getValue(), date))
                .toList();
    }

    private <T> T fetchApi(URL target, Type clazz) {
        try {
            return this.fetch.getJson(target, this.getOptions(), clazz);
        } catch (final FetchException e) {
            throw this.mapper.translate(e);
        }
    }

    private URL buildCurrenciesUrl(final List<String> currencyCodes) {
        final var currencies = new BasicNameValuePair("currencies", String.join(",", currencyCodes));
        return this.getUrl("/v1/currencies", List.of(currencies));
    }

    private URL buildRateUrl(final Currency from, final List<Currency> to) {
        final var currencyCodes = to.stream().map(Currency::getCurrencyCode).toList();
        final var currencies = String.join(",", currencyCodes);

        final var path = "/v1/latest";
        final List<NameValuePair> queries = List.of(
                new BasicNameValuePair("base_currency", from.getCurrencyCode()),
                new BasicNameValuePair("currencies", currencies));

        return this.getUrl(path, queries);
    }

    private URL buildHistoricalRateUrl(final Currency from, final List<Currency> to, final LocalDate rateDate) {
        final var currencyCodes = to.stream().map(Currency::getCurrencyCode).toList();
        final var currencies = String.join(",", currencyCodes);

        final var path = "/v1/historical";
        final List<NameValuePair> queries = List.of(
                new BasicNameValuePair("base_currency", from.getCurrencyCode()),
                new BasicNameValuePair("currencies", currencies),
                new BasicNameValuePair("date", rateDate.toString()));

        return this.getUrl(path, queries);
    }

    private URL getUrl(final String path, final List<NameValuePair> query) {
        try {
            final var filteredQuery = query.stream().filter(pairs -> !pairs.getValue().isBlank()).toList();

            return new URIBuilder(this.getApiBaseUrl())
                    .setPath(path)
                    .setParameters(filteredQuery)
                    .build().toURL();
        } catch (final MalformedURLException | URISyntaxException e) {
            throw new ExternalServiceException(
                    new ApiError(ApiErrorCategory.CLIENT_ERROR, "Internal error building API URL"), e);
        }
    }

    private String getApiBaseUrl() {
        return this.props.get("FREE_CURRENCY_EXCHANGE_API_BASE_URL");
    }

    private String getApiToken() {
        return this.props.get("FREE_CURRENCY_EXCHANGE_API_TOKEN");
    }

    private Fetch.Options getOptions() {
        return this.fetch.getOptions().addHeader("apikey", this.getApiToken());
    }
}
