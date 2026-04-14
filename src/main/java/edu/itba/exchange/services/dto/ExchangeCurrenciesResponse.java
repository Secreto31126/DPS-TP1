package edu.itba.exchange.services.dto;

import java.util.Map;

import lombok.Data;

@Data
public class ExchangeCurrenciesResponse {
    private Map<String, ExchangeCurrenciesResponse.ExchangeCurrencyData> data;

    public record ExchangeCurrencyData(String symbol, String name, String symbol_native, long decimal_digits,
            long rounding, String code, String name_plural) {
    }
}
