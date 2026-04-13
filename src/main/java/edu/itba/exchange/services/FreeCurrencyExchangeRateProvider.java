package edu.itba.exchange.services;

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
import edu.itba.exchange.services.dto.ExchangeCurrenciesResponse;
import edu.itba.exchange.services.dto.ExchangeRateResponse;
import edu.itba.exchange.services.dto.HistoricalExchangeRateResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import edu.itba.exchange.interfaces.Fetch;
import edu.itba.exchange.interfaces.PropertiesProvider;
import edu.itba.exchange.models.Rate;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FreeCurrencyExchangeRateProvider implements ExchangeRateProvider {
    private final Fetch fetch;
    private final PropertiesProvider propertiesProvider;
    private final FetchExceptionMapper<CurrencyException> fetchExceptionMapper;

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
        return this.formRateResponse(from, to, rateDate);
    }

    private List<Rate> formRateResponse(final Currency from, final List<Currency> to, final LocalDate rateDate) {
        final var url = this.buildRateUrl(from, to, rateDate);
        final var options = this.getOptions();

        if (rateDate != null) {
            return getHistoricalRateList(from, url, options, rateDate);
        }

        return getLatestRateList(from, url, options);
    }

    private List<Rate> getLatestRateList(final Currency from, final URL url, final Fetch.Options options) {
        try {
            final ExchangeRateResponse response = fetch.getJson(url, options, ExchangeRateResponse.class);

            return response.getData().entrySet().stream()
                    .map(entry -> new Rate(from, entry.getKey(), entry.getValue()))
                    .toList();
        } catch (final FetchException e) {
            throw this.fetchExceptionMapper.translate(e);
        }
    }



    private List<Rate> getHistoricalRateList(final Currency from, final URL url, final Fetch.Options options, final LocalDate rateDate) {
        try {
            final HistoricalExchangeRateResponse response = fetch.getJson(url, options, HistoricalExchangeRateResponse.class);

            final Map<String, BigDecimal> dailyRates = response.getData().getOrDefault(rateDate.toString(), Map.of());

            return dailyRates.entrySet().stream()
                    .map(entry -> new Rate(from, Currency.getInstance(entry.getKey()), entry.getValue()))
                    .toList();
        } catch (final FetchException e) {
            throw this.fetchExceptionMapper.translate(e);
        }
    }

    private List<Currency> formCurrencyResponse(final List<String> currencyCodes) {
        try {
            final var url = this.buildCurrenciesUrl(currencyCodes);
            final var options = this.getOptions();

            final ExchangeCurrenciesResponse response = fetch.getJson(url, options, ExchangeCurrenciesResponse.class);

            return response.getData().keySet().stream()
                    .map(Currency::getInstance)
                    .toList();
        } catch (final FetchException e) {
            throw this.fetchExceptionMapper.translate(e);
        } catch (final IllegalArgumentException e) {
            throw new CurrencyNotFoundException(new ApiError(ApiErrorCategory.CLIENT_ERROR, e.getMessage()));
        }
    }

    private URL buildCurrenciesUrl(final List<String> currencyCodes) {
        final var currencies = new BasicNameValuePair("currencies", String.join(",", currencyCodes));
        return this.getUrl("/v1/currencies", List.of(currencies));
    }

    private URL buildRateUrl(final Currency from, final List<Currency> to, final LocalDate rateDate) {
        final var currencyCodesList = to.stream().map(Currency::getCurrencyCode).toList();
        final var currencies = String.join(",", currencyCodesList);

        return rateDate != null
                ? this.buildHistoricalRateUrl(from, currencies, rateDate)
                : this.buildLatestRateUrl(from, currencies);
    }

    private URL buildHistoricalRateUrl(final Currency from, final String currencies, final LocalDate rateDate) {
        final var path = "/v1/historical";
        final List<NameValuePair> queries = List.of(
                new BasicNameValuePair("base_currency", from.getCurrencyCode()),
                new BasicNameValuePair("currencies", currencies),
                new BasicNameValuePair("date", rateDate.toString()));

        return this.getUrl(path, queries);
    }

    private URL buildLatestRateUrl(final Currency from, final String currencies) {
        final var path = "/v1/latest";
        final List<NameValuePair> queries = List.of(
                new BasicNameValuePair("base_currency", from.getCurrencyCode()),
                new BasicNameValuePair("currencies", currencies));

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
                    new ApiError(ApiErrorCategory.CLIENT_ERROR, "Internal error building API URL"), e
            );
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
}
