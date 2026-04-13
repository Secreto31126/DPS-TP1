package edu.itba.exchange.services.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ExchangeCurrenciesResponse {
    private Map<String, ExchangeCurrenciesResponse.ExchangeCurrencyData> data;

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
