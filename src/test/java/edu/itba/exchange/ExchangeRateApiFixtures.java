package edu.itba.exchange;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExchangeRateApiFixtures {
    private static final Gson gson = new Gson();

    /**
     * Generates a "latest rates" response.
     * Example: {@code { "data": { "USD": 1.2, "CAD": 1.5 } }}
     *
     * @param rates A map of currency codes to their respective exchange rates.
     * @return A JSON string formatted for the /v1/latest endpoint.
     */
    public static String latest(Map<String, Double> rates) {
        return gson.toJson(Map.of("data", rates));
    }

    /**
     * Generates a "historical rates" response, nested by date.
     * Example: {@code { "data": { "2024-01-01": { "USD": 1.1 } } }}
     *
     * @param date  The date string (YYYY-MM-DD).
     * @param rates A map of currency codes to their historical exchange rates.
     * @return A JSON string formatted for the /v1/historical endpoint.
     */
    public static String historical(String date, Map<String, Double> rates) {
        return gson.toJson(Map.of("data", Map.of(date, rates)));
    }

    /**
     * Generates a response for the supported currencies list.
     * Example: {@code { "data": { "USD": { "code": "USD" }, "EUR": { "code": "EUR" } } }}
     *
     * @param codes A list of currency ISO codes.
     * @return A JSON string formatted for the /v1/currencies endpoint.
     */
    public static String currencies(List<Currency> codes) {
        Map<String, Map<String, String>> data = codes.stream()
                .map(Currency::getCurrencyCode)
                .collect(Collectors.toMap(
                        code -> code,
                        code -> Map.of("code", code)
                ));
        return gson.toJson(Map.of("data", data));
    }

    /**
     * Generates a standard API error response.
     * Example: {@code { "message": "The given currency code is not supported" } }
     *
     * @param message The error message to be returned by the mock.
     * @return A JSON string containing the error message.
     */
    public static String error(String message) {
        return gson.toJson(Map.of("message", message));
    }

    /**
     * Generates a success response envelope with an empty data object.
     *
     * @return A JSON string: {@code { "data": {} }}
     */
    public static String emptyData() {
        return gson.toJson(Map.of("data", Collections.emptyMap()));
    }
}