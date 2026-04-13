package edu.itba.exchange.services.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;

@Data
public class HistoricalExchangeRateResponse {
    private Map<LocalDate, Map<Currency, BigDecimal>> data;
}
