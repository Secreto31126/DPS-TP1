package edu.itba.exchange;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExchangeRateApiFixtures {
    private static final Gson gson = new Gson();

    public static String latest(Map<String, Double> rates) {
        return gson.toJson(Map.of("data", rates));
    }

    public static String historical(String date, Map<String, Double> rates) {
        return gson.toJson(Map.of("data", Map.of(date, rates)));
    }

    public static String currencies(List<String> codes) {
        Map<String, Map<String, String>> data = codes.stream()
                .collect(Collectors.toMap(
                        code -> code,
                        code -> Map.of("code", code)
                ));
        return gson.toJson(Map.of("data", data));
    }

    public static String error(String message) {
        return gson.toJson(Map.of("message", message));
    }

    public static String emptyData() {
        return gson.toJson(Map.of("data", Collections.emptyMap()));
    }
}