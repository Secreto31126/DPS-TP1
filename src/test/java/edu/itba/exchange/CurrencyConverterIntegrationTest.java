package edu.itba.exchange;

import static org.junit.jupiter.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
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
        PropertiesProvider testProperties = key -> {
            if ("FREE_CURRENCY_EXCHANGE_API_BASE_URL".equals(key)) return wireMock.baseUrl() + "/v1";
            if ("FREE_CURRENCY_EXCHANGE_API_TOKEN".equals(key)) return "test-token";
            return null;
        };

        Fetch fetch = new UnirestFetch(new GsonJSON());
        var provider = new FreeCurrencyExchangeRateProvider(fetch, testProperties);

        this.converter = new CurrencyConverter(provider);
    }

    @Test
    void shouldConvertSingleCurrency() {
        // arrange
        String mockJson = ExchangeRateApiFixtures.latest(Map.of("USD", 1.2));
        stubLiveRateSuccess("EUR", "USD", mockJson);
        Money amountToConvert = new Money(new BigDecimal("100"), EUR);

        // act
        ConversionResult result = converter.convert(amountToConvert, USD);

        // assert
        assertInstanceOf(ConversionResult.Success.class, result, "Expected a successful conversion result");
        var success = (ConversionResult.Success) result;

        assertEquals(0, new BigDecimal("120.0").compareTo(success.money().amount()));
        assertEquals("USD", success.money().currency().getCurrencyCode());
    }

    @Test
    void shouldConvertMultipleCurrencies() {
        var mockJson = ExchangeRateApiFixtures.latest(Map.of("USD", 1.2, "CAD", 1.5));
        stubLiveRateSuccess("EUR", "USD,CAD", mockJson);
        Money amountToConvert = new Money(new BigDecimal("100"), EUR);

        List<ConversionResult> results = converter.convert(amountToConvert, List.of(USD, CAD));

        assertEquals(2, results.size(), "Should return exactly two conversion results");
        assertInstanceOf(ConversionResult.Success.class, results.get(0));
        assertInstanceOf(ConversionResult.Success.class, results.get(1));
    }

    @Test
    void shouldConvertHistoricalRate() {
        String historicalDate = "2024-01-01";
        String mockNestedJson = ExchangeRateApiFixtures.historical(historicalDate, Map.of("USD", 1.1));
        stubHistoricalRateSuccess("EUR", "USD", historicalDate, mockNestedJson);

        Money amountToConvert = new Money(new BigDecimal("100"), EUR);

        ConversionResult result = converter.convert(amountToConvert, USD, LocalDate.parse(historicalDate));

        assertInstanceOf(ConversionResult.Success.class, result);
        var success = (ConversionResult.Success) result;
        assertEquals(0, new BigDecimal("110.0").compareTo(success.money().amount()));
    }

    @Test
    void shouldReturnAvailableCurrencies() {
        List<String> codes = List.of("USD", "EUR");
        String mockJson = ExchangeRateApiFixtures.currencies(codes);
        stubAvailableCurrenciesSuccess("USD,EUR", mockJson);

        AvailableCurrenciesResult result = converter.getAvailableCurrencies(codes);

        assertInstanceOf(AvailableCurrenciesResult.Success.class, result, "Expected a successful currencies fetch");
        var success = (AvailableCurrenciesResult.Success) result;
        assertEquals(2, success.currencies().size());
    }

    @Test
    void shouldHandleInvalidCurrencyRejection() {
        String mockJson = ExchangeRateApiFixtures.error("Invalid currency code");
        stubEndpointWithError(LATEST_ENDPOINT, 422, mockJson);
        Money money = new Money(new BigDecimal("100"), EUR);

        List<ConversionResult> results = converter.convert(money, List.of(USD));

        assertInstanceOf(ConversionResult.Failure.class, results.getFirst());
    }

    @Test
    void shouldHandleInternalApiError() {
        String mockErrorBody = """
                Internal Server Error
                """;
        stubEndpointWithError(LATEST_ENDPOINT, 500, mockErrorBody);
        Money money = new Money(new BigDecimal("100"), EUR);

        List<ConversionResult> results = converter.convert(money, List.of(USD));

        assertInstanceOf(ConversionResult.Failure.class, results.getFirst(), "Should gracefully return a Failure object on 500 errors");
    }

    @Test
    void shouldHandleCurrencyFetchFailure() {
        String mockErrorBody = """
                Server Down
                """;
        stubEndpointWithError(CURRENCIES_ENDPOINT, 500, mockErrorBody);

        AvailableCurrenciesResult result = converter.getAvailableCurrencies(List.of("USD"));

        assertInstanceOf(AvailableCurrenciesResult.Failure.class, result, "Should return a Failure result when the API crashes");
    }

    private void stubLiveRateSuccess(final String base, final String targets, final String jsonResponse) {
        wireMock.stubFor(get(urlPathEqualTo(LATEST_ENDPOINT))
                .withQueryParam("base_currency", equalTo(base))
                .withQueryParam("currencies", equalTo(targets))
                .willReturn(okJson(jsonResponse)));
    }

    private void stubHistoricalRateSuccess(final String base, final String targets, final String date, final String jsonResponse) {
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
        wireMock.stubFor(get(urlPathEqualTo(CURRENCIES_ENDPOINT))
                .withQueryParam("currencies", equalTo(requestedCurrencies))
                .willReturn(okJson(jsonResponse)));
    }
}