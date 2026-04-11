package edu.itba.exchange;

import static org.junit.jupiter.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

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
        String mockJson = """
            {
              "data": {
                "USD": 1.2
              }
            }
            """;

        stubLiveRateSuccess("EUR", "USD", mockJson);
        Money amountToConvert = new Money(new BigDecimal("100"), EUR);

        // act
        ConversionResult result = converter.convert(amountToConvert, USD);

        // assert
        assertTrue(result instanceof ConversionResult.Success, "Expected a successful conversion result");
        var success = (ConversionResult.Success) result;

        assertEquals(0, new BigDecimal("120.0").compareTo(success.money().amount()));
        assertEquals("USD", success.money().currency().getCurrencyCode());
    }

    @Test
    void shouldConvertMultipleCurrencies() {
        String mockJson = """
                {
                    "data": {
                        "USD": 1.2,
                        "CAD": 1.5
                    }
                }
                """;
        stubLiveRateSuccess("EUR", "USD,CAD", mockJson);
        Money amountToConvert = new Money(new BigDecimal("100"), EUR);

        List<ConversionResult> results = converter.convert(amountToConvert, List.of(USD, CAD));

        assertEquals(2, results.size(), "Should return exactly two conversion results");
        assertTrue(results.get(0) instanceof ConversionResult.Success);
        assertTrue(results.get(1) instanceof ConversionResult.Success);
    }

    @Test
    void shouldConvertHistoricalRate() {
        String historicalDate = "2024-01-01";
        String mockNestedJson = """
                {
                    "data": {
                        "%s": {
                            "USD": 1.1
                        }
                    }
                }
                """.formatted(historicalDate);
        stubHistoricalRateSuccess("EUR", "USD", historicalDate, mockNestedJson);

        Money amountToConvert = new Money(new BigDecimal("100"), EUR);

        ConversionResult result = converter.convert(amountToConvert, USD, LocalDate.parse(historicalDate));

        assertTrue(result instanceof ConversionResult.Success);
        var success = (ConversionResult.Success) result;
        assertEquals(0, new BigDecimal("110.0").compareTo(success.money().amount()));
    }

    @Test
    void shouldHandleInvalidCurrencyRejection() {
        String mockJson = """
                {
                    "message": "Invalid currency code"
                }
                """;
        stubEndpointWithError("/v1/latest", 422, mockJson);
        Money money = new Money(new BigDecimal("100"), EUR);

        List<ConversionResult> results = converter.convert(money, List.of(USD));

        assertTrue(results.get(0) instanceof ConversionResult.Failure);
        var failure = (ConversionResult.Failure) results.get(0);
        assertTrue(failure.errorMessage().contains("Failed to fetch JSON"));
    }

    @Test
    void shouldHandleInternalApiError() {
        String mockErrorBody = """
                Internal Server Error
                """;
        stubEndpointWithError("/v1/latest", 500, mockErrorBody);
        Money money = new Money(new BigDecimal("100"), EUR);

        List<ConversionResult> results = converter.convert(money, List.of(USD));

        assertTrue(results.get(0) instanceof ConversionResult.Failure, "Should gracefully return a Failure object on 500 errors");
    }

    @Test
    void shouldReturnAvailableCurrencies() {
        String mockJson = """
                {
                    "data": {
                        "USD": {"code": "USD"},
                        "EUR": {"code": "EUR"}
                    }
                }
                """;
        stubAvailableCurrenciesSuccess("USD,EUR", mockJson);

        AvailableCurrenciesResult result = converter.getAvailableCurrencies(List.of("USD", "EUR"));

        assertTrue(result instanceof AvailableCurrenciesResult.Success, "Expected a successful currencies fetch");
        var success = (AvailableCurrenciesResult.Success) result;
        assertEquals(2, success.currencies().size());
    }

    @Test
    void shouldHandleCurrencyFetchFailure() {
        String mockErrorBody = """
                Server Down
                """;
        stubEndpointWithError("/v1/currencies", 500, mockErrorBody);

        AvailableCurrenciesResult result = converter.getAvailableCurrencies(List.of("USD"));

        assertTrue(result instanceof AvailableCurrenciesResult.Failure, "Should return a Failure result when the API crashes");
    }

    private void stubLiveRateSuccess(final String base, final String targets, final String jsonResponse) {
        wireMock.stubFor(get(urlPathEqualTo("/v1/latest"))
                .withQueryParam("base_currency", equalTo(base))
                .withQueryParam("currencies", equalTo(targets))
                .willReturn(okJson(jsonResponse)));
    }

    private void stubHistoricalRateSuccess(final String base, final String targets, final String date, final String jsonResponse) {
        wireMock.stubFor(get(urlPathEqualTo("/v1/historical"))
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
        wireMock.stubFor(get(urlPathEqualTo("/v1/currencies"))
                .withQueryParam("currencies", equalTo(requestedCurrencies))
                .willReturn(okJson(jsonResponse)));
    }
}