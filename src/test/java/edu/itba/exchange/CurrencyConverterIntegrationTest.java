package edu.itba.exchange;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import edu.itba.exchange.exceptions.CurrencyException;
import edu.itba.exchange.interfaces.FetchExceptionMapper;
import edu.itba.exchange.interfaces.JSON;
import edu.itba.exchange.models.AvailableCurrenciesResult;
import edu.itba.exchange.models.ConversionResult;
import edu.itba.exchange.services.FreeCurrencyFetchExceptionMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import edu.itba.exchange.interfaces.PropertiesProvider;
import edu.itba.exchange.models.Money;
import edu.itba.exchange.services.FreeCurrencyExchangeRateProvider;
import edu.itba.exchange.interfaces.Fetch;
import edu.itba.exchange.services.UnirestFetch;
import edu.itba.exchange.services.GsonJSON;

class CurrencyConverterIntegrationTest {
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    private CurrencyConverter converter;

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency CAD = Currency.getInstance("CAD");

    private static final String LATEST_ENDPOINT = "/v1/latest";
    private static final String HISTORICAL_ENDPOINT = "/v1/historical";
    private static final String CURRENCIES_ENDPOINT = "/v1/currencies";

    @BeforeEach
    void setUp() {
        final PropertiesProvider testProperties = key -> {
            if ("FREE_CURRENCY_EXCHANGE_API_BASE_URL".equals(key))
                return wireMock.baseUrl() + "/v1";
            if ("FREE_CURRENCY_EXCHANGE_API_TOKEN".equals(key))
                return "test-token";
            return null;
        };

        final var json = new GsonJSON();
        final var fetch = new UnirestFetch(json);

        final var provider = new FreeCurrencyExchangeRateProvider(fetch, testProperties, json);

        this.converter = new CurrencyConverter(provider);
    }

    @Test
    void shouldConvertSingleCurrency() {
        // arrange
        final var mockJson = ExchangeRateApiFixtures.latest(Map.of("USD", 1.2));
        stubLiveRateSuccess("EUR", "USD", mockJson);
        final var amountToConvert = new Money(new BigDecimal("100.0"), EUR);

        // act
        final var result = converter.convert(amountToConvert, USD);

        // assert
        assertThat(result, is(instanceOf(ConversionResult.Success.class)));

        final var success = (ConversionResult.Success) result;
        assertThat(success.money().amount(), is(new BigDecimal("120.00")));
        assertThat(success.money().currency(), is(USD));
    }

    @Test
    void shouldConvertMultipleCurrencies() {
        final var mockJson = ExchangeRateApiFixtures.latest(Map.of("USD", 1.2, "CAD", 1.5));
        stubLiveRateSuccess("EUR", "USD,CAD", mockJson);
        final var amountToConvert = new Money(new BigDecimal("100.0"), EUR);

        final var results = converter.convert(amountToConvert, List.of(USD, CAD));

        assertThat(results.size(), is(2));
        assertThat(results.get(0), is(instanceOf(ConversionResult.Success.class)));
        assertThat(results.get(1), is(instanceOf(ConversionResult.Success.class)));

        final var success = results.stream()
                .map(e -> (ConversionResult.Success) e)
                .filter(e -> e.money().currency().equals(CAD))
                .findAny()
                .orElseThrow();

        assertThat(success.money().amount(), is(new BigDecimal("150.00")));
    }

    @Test
    void shouldConvertHistoricalRate() {
        final var historicalDate = "2024-01-01";
        final var mockNestedJson = ExchangeRateApiFixtures.historical(historicalDate, Map.of("USD", 1.1));
        stubHistoricalRateSuccess("EUR", "USD", historicalDate, mockNestedJson);

        final var amountToConvert = new Money(new BigDecimal("100.0"), EUR);

        final var result = converter.convert(amountToConvert, USD, LocalDate.parse(historicalDate));

        assertThat(result, is(instanceOf(ConversionResult.Success.class)));

        final var success = (ConversionResult.Success) result;
        assertThat(success.money().amount(), is(new BigDecimal("110.00")));
    }

    @Test
    void shouldReturnAvailableCurrencies() {
        final var codes = List.of("USD", "EUR");
        final var mockJson = ExchangeRateApiFixtures.currencies(codes);
        stubAvailableCurrenciesSuccess("USD,EUR", mockJson);

        final var result = converter.getAvailableCurrencies(codes);

        assertThat(result, is(instanceOf(AvailableCurrenciesResult.Success.class)));

        final var success = (AvailableCurrenciesResult.Success) result;
        assertThat(success.currencies().size(), is(2));
    }

    @Test
    void shouldHandleInvalidCurrencyRejection() {
        final var mockJson = ExchangeRateApiFixtures.error("Invalid currency code");
        stubEndpointWithError(LATEST_ENDPOINT, 422, mockJson);
        final var money = new Money(new BigDecimal("100"), EUR);

        final var results = converter.convert(money, List.of(USD));

        assertThat(results.getFirst(), is(instanceOf(ConversionResult.Failure.class)));
    }

    @Test
    void shouldHandleInternalApiError() {
        final var mockErrorBody = """
                Internal Server Error
                """;
        stubEndpointWithError(LATEST_ENDPOINT, 500, mockErrorBody);
        final var money = new Money(new BigDecimal("100"), EUR);

        final var result = converter.convert(money, USD);

        assertThat(result, is(instanceOf(ConversionResult.Failure.class)));
    }

    @Test
    void shouldHandleCurrencyFetchFailure() {
        final var mockErrorBody = """
                Server Down
                """;
        stubEndpointWithError(CURRENCIES_ENDPOINT, 500, mockErrorBody);

        final var result = converter.getAvailableCurrencies(List.of("USD"));

        assertThat(result, is(instanceOf(AvailableCurrenciesResult.Failure.class)));
    }

    @Test
    void shouldReturnAllAvailableCurrencies() {
        final var mockJson = ExchangeRateApiFixtures.currencies(List.of("USD", "EUR"));
        stubAvailableCurrenciesSuccess("", mockJson);

        final var result = converter.getAvailableCurrencies();

        assertThat(result, is(instanceOf(AvailableCurrenciesResult.Success.class)));

        final var success = (AvailableCurrenciesResult.Success) result;
        assertThat(success.currencies().size(), is(2));
    }

    private void stubLiveRateSuccess(final String base, final String targets, final String jsonResponse) {
        wireMock.stubFor(get(urlPathEqualTo(LATEST_ENDPOINT))
                .withQueryParam("base_currency", equalTo(base))
                .withQueryParam("currencies", equalTo(targets))
                .willReturn(okJson(jsonResponse)));
    }

    private void stubHistoricalRateSuccess(final String base, final String targets, final String date,
            final String jsonResponse) {
        wireMock.stubFor(get(urlPathEqualTo(HISTORICAL_ENDPOINT))
                .withQueryParam("base_currency", equalTo(base))
                .withQueryParam("currencies", equalTo(targets))
                .withQueryParam("date", equalTo(date))
                .willReturn(okJson(jsonResponse)));
    }

    private void stubEndpointWithError(final String path, int statusCode, final String jsonBody) {
        wireMock.stubFor(get(urlPathEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withBody(jsonBody)));
    }

    private void stubAvailableCurrenciesSuccess(final String requestedCurrencies, final String jsonResponse) {
        var mappingBuilder = get(urlPathEqualTo(CURRENCIES_ENDPOINT));
        if (requestedCurrencies != null && !requestedCurrencies.isEmpty()) {
            mappingBuilder.withQueryParam("currencies", equalTo(requestedCurrencies));
        }
        wireMock.stubFor(mappingBuilder.willReturn(okJson(jsonResponse)));
    }
}
